package genetic;

import game.Game;
import game.Results;
import game.State;
import game.TFrame;

import java.util.Arrays;

public class Tetris_gen extends Game {

    private State state;
    private boolean visualise_game = false;

    public Tetris_gen() {
        // Define game specific variables.
        // num_states : top-row boolean for every figure (7)
        // num_actions: each column (10) with each orientation (4)
        // Start super game class.
        state = new State();
        if (visualise_game) activateVisualisation();
    }

    @Override
    // Results of a (random) initial game state.
    public Results initial(){
        //Currently not needed!
        return new Results(reward(), state(), terminal());
    }


    @Override
    // Execute an given action in game and return its
    // reward, the following state and if the game has terminated.
    // @param[in]   action      1D-representation of action.
    public Results step(final int action_index) {
        //This is where we ultimately MAKE!! the move
        int[][]all_moves = this.actions();
        int[]des_move = all_moves[action_index];

        state.makeMove(des_move[0],des_move[1]);
        // Draw new state and next piece.
        if (visualise_game){
            state.draw();
            state.drawNext(0,0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Results(state.getRowsCleared(), state(), terminal());
    }


    @Override
    // Restart game, i.e. closing old and starting new game.
    public Game restart() {
        return new Tetris_gen();
    }

    @Override
    // Terminal test of game, i.e. boolean if game has terminated yet.
    protected boolean terminal() {
        return state.hasLost();
    }

    @Override
    // Reward function, i.e. scalar value evaluating the last action
    // in dependence of current (internal) state.
    protected double reward() {
        return 0;
    }

    @Override
    public int toScalarState(final int[] state){
        //not needed!!!
        return 0;
    }


    @Override
    // Internal state, i.e. environment describing integer array.
    public int[] state() {
        // This function returns the current state!
        //consists of the filed followd by the current stone
        int stone = state.getNextPiece();
        //int stone = 0;
        int[][] field = state.getField();
        //System.out.println(Arrays.deepToString(field));
        int height = field.length; //height of field
        int width = field[0].length; //width of field
        //System.out.printf("size is %d x %d%n", len1, len2);

        //building final state array:
        //state_arr[0] == position where the stone information is -> before there is the field!
        //state_arr[1] == width of field
        //state_arr[2] == height of field
        //state_arr[state_arr[0]]==stone info!!! in between there is the field!!
        int[] state_arr = new int [height*width+4]; //4=pos_stone+height+width+numberofstone

        state_arr[0] = (height*width+4)-1; //this info could also be computed from others but i thought that would make
        //it easier!
        state_arr[1] = width;
        state_arr[2] = height;
        state_arr[state_arr[0]] = stone;

        for (int h=0; h<height; h++){
            for (int w=0; w<width; w++){
                state_arr[h*width+w+3]= (field[h][w]!=0) ? 1 : 0;
            }
        }
        //System.out.println(Arrays.toString(state_arr));
        //System.out.printf("size is %d", state_arr.length);
        return state_arr;
    }

    // Definition of game properties.
    @Override
    public int[][] actions() {
        int i = 0;
        int[][]actions = new int [numActions()][2];
        for (int orient = 0; orient < 4; ++orient) {
            for (int slot = 0; slot < State.COLS; ++slot) {
                actions[i] = new int[]{orient,slot};
                i++;
            }
        }
        return actions;
    }

    @Override
    public boolean checkAction(final int action_index) {
        final int[][] valid     = state.legalMoves();
        int[][] poss_moves = actions();
        int[] wanted_action = poss_moves[action_index];
        int num_all_moves = valid.length;
        for (int i=0; i<num_all_moves; i++){
            if(valid[i][0]==wanted_action[0] && valid[i][1]==wanted_action[1]){
                return true;
            }
        }
        return false;
    }
    public int numStates(){
        //not neede
        return 100;
    }
    public int numActions(){
        return 40;
    }

    // for genetic algorithm
    //input: current state which includes the action
    //output: state and whether terminated, reward == num rows cleared!!!
    public Results virtual_move(int[] own_state, int action_index){

        Results outcome = new Results(0, new int[own_state.length],false);

        //find out orientation and slot of wanted move
        int[][] all_moves = this.actions();
        int orient = all_moves[action_index][0];
        int slot = all_moves[action_index][1];

        //reconstruct the field:
        int width_field = own_state[1];
        int height_field = own_state[2];
        outcome.state[1] = own_state[1];
        outcome.state[2] = own_state[2];
        int[][]field = new int[height_field][width_field];
        for (int h=0; h<height_field;h++ ) {
            for (int w = 0; w < width_field; w++) {
                field[h][w] = own_state[3 + h * width_field + w];
            }
        }
        //next piece
        int nextPiece = own_state[own_state[0]];



        //Reading in the needed values and make a copy of the arrays
        //without copy: pointer issues!!!!!!!!
        int[]top_ = state.getTop();
        int[] top = top_.clone();
        int[][][]pBottom_ = state.getpBottom();
        int[][][] pBottom = pBottom_.clone();
        int[][] pWidth_ = state.getpWidth();
        int[][] pWidth = pWidth_.clone();
        int[][] pHeight_ = state.getpHeight();
        int[][] pHeight = pHeight_.clone();
        int ROWS = state.ROWS;
        int COLS = state.COLS;
        int[][][]pTop_ = state.getpTop();
        int[][][] pTop = pTop_.clone();




        //height if the first column makes contact
        int height = top[slot]-pBottom[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for(int c = 1; c < pWidth[nextPiece][orient];c++) {
            height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
        }

        //check if game ended
        if(height+pHeight[nextPiece][orient] >= ROWS) {
            outcome.terminated = true;
            return outcome;
        }


        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
                //System.out.println(h);
                //System.out.println(i+slot);
                field[h][i+slot] = 1; //fill every field with 1!!!
            }
        }

        //adjust top
        for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot+c]=height+pTop[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for(int c = 0; c < COLS; c++) {
                if(field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if(full) {
                rowsCleared++;
                //for each column
                for(int c = 0; c < COLS; c++) {

                    //slide down all bricks
                    for(int i = r; i < top[c]; i++) {
                        field[i][c] = field[i+1][c];
                    }
                    //lower the top
                    top[c]--;
                    while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
                }
            }
        }
        outcome.reward = rowsCleared;

        //deconstruct the field to the new virtual state!
        for (int h=0; h<height_field; h++){
            for (int w=0; w<width_field; w++){
                outcome.state[h*width_field+w+3]= (field[h][w]!=0) ? 1 : 0;
            }
        }

        return outcome;
    }

    //input: virtual state
    //output: array of features!
    public double[] features (Results virtual_state_res){
        int[] virtual_state = virtual_state_res.state;
        double num_cleared_rows = virtual_state_res.reward;
        int field_width = virtual_state[1];
        int field_height = virtual_state[2];

        // calc number of holes/check for holes
        int num_holes = 0;
        for (int i =0; i<field_width*(field_height-1)-1;i++){
            int calc = virtual_state[3+i]-virtual_state[3+field_width+i]; //lower - upper
            //if above there is one but below not -> will yield to -1!!!
            if (calc<0){
                num_holes++;
            }
        }

        //calc height of each column
        int[]height_map = new int[field_width];
        for (int i =0; i<field_width;i++){
            for (int j=0; j<field_height;j++){ //go over all possible heights!
                if (virtual_state[3+i+j*field_width]!=0){
                    height_map[i]=j;
                }
            }
        }

        //from there extract: aggregate height:
        int aggregate_height = 0;
        for (int j=0; j<field_width; j++){
            aggregate_height = aggregate_height + height_map[j];
        }

        //from there extract: bumpieness:
        int bumpieness = 0;
        for (int j=1; j<field_width; j++){
            bumpieness = bumpieness + Math.abs(height_map[j]-height_map[j-1]);
        }

        return new double[]{num_holes,num_cleared_rows,aggregate_height,bumpieness};
    }

    //return number of features
    public int numFeatures(){
        //add +1 since rows cleared is already available after the virtual move!!!
        //TODO: adapt to function above
        return (3+1);
    }

    @Override
    public void activateVisualisation() {
        visualise_game = true;
        new TFrame(state);
    }

}

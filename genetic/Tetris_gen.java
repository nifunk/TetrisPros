package genetic;

import game.Game;
import game.Results;
import game.State;
import game.TFrame;

import java.util.Arrays;

public class Tetris_gen extends Game {

    private State state;

    public Tetris_gen() {
        // Define game specific variables.
        // num_states : top-row boolean for every figure (7)
        // num_actions: each column (10) with each orientation (4)
        // Start super game class.
        state = new State();
        new TFrame(state);
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
             int[][]tryout = this.actions();
        boolean YOLO = this.checkAction(0);

        state.makeMove(0,0);
        // Draw new state and next piece.
        state.draw();
        state.drawNext(0,0);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Results(reward(), state(), terminal());
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
    protected int[] state() {
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
    //output: state and whether terminated, reward does not matter
    public Results virtual_move(int[] state){
        //TODO define correctly!
        return new Results(reward(), state(), terminal());
    }

    //input: virtual state
    //output: array of features!
    public double[] features (int[] virtual_state){
        //TODO: define!!
        return new double[]{0};
    }

    //return number of features
    public int numfeatures(){
        //TODO: adapt to function above
        return 0;
    }


}

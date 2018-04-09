package game;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Integer.min;
import static java.lang.Math.abs;

public class TetrisInterface extends Game
{

    private State _state;
    private boolean _visualise_game = false;

    public TetrisInterface()
    {
        // Define game specific variables.
        // num__states : top-row boolean for every figure (7)
        // num_actions: each column (10) with each orientation (4)
        // Start super game class.
        _state = new State();
        if (_visualise_game) activateVisualisation();
    }

    @Override
    // Results of a (random) initial game _state.
    public Results initial()
    {
        int[] init__state = new int[200];
        Arrays.fill(init__state, 0);
        init__state[200 - 1] = _state.getNextPiece();
        return new Results(0.0, init__state, false);
    }


    @Override
    // Execute an given action in game and return its
    // reward, the following _state and if the game has terminated.
    // @param[in]   action      1D-representation of action.
    public Results step(final int action_index)
    {
        //This is where we ultimately MAKE!! the move
        int[][]all_moves = this.actions();
        int[]des_move = all_moves[action_index];

        _state.makeMove(des_move[0],des_move[1]);
        // Draw new _state and next piece.
        if (_visualise_game){
            _state.draw();
            _state.drawNext(0,0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Results(_state.getRowsCleared(), state(), terminal());
    }


    @Override
    // Restart game, i.e. closing old and starting new game.
    public Game restart()
    {
        TetrisInterface new_game = new TetrisInterface();
        if (encoder.encoderReady())
            {
                new_game.encoder = encoder;
            }
        return new_game;
    }

    @Override
    // Terminal test of game, i.e. boolean if game has terminated yet.
    protected boolean terminal()
    {
        return _state.hasLost();
    }

    @Override
    // Reward function, i.e. scalar value evaluating the last action
    // in dependence of current (internal) _state.
    protected double reward()
    {
        return _state.getRowsCleared();
    }

    @Override
    // Internal _state, i.e. environment describing integer array.
    public int[] state()
    {
        // This function returns the current _state!
        //consists of the filed followed by the current stone
        int stone = _state.getNextPiece();
        //int stone = 0;
        int[][] field = _state.getField();
        //System.out.println(Arrays.deepToString(field));
        int height = field.length; //height of field
        //System.out.println(height);
        int width = field[0].length; //width of field
        //System.out.println(width);
        //System.out.printf("size is %d x %d%n", len1, len2);

        //building final _state array:
        //_state_arr[0] == position where the stone information is -> before there is the field!
        //_state_arr[1] == width of field
        //_state_arr[2] == height of field
        //_state_arr[_state_arr[0]]==stone info!!! in between there is the field!!
        int[] _state_arr = new int[height*width+4]; //4=pos_stone+height+width+numberofstone

        _state_arr[0] = (height*width+4)-1; //this info could also be computed from others but i thought that would make
        //it easier!
        _state_arr[1] = width;
        _state_arr[2] = height;
        _state_arr[_state_arr[0]] = stone;

        for (int h=0; h<height; h++){
            for (int w=0; w<width; w++){
                _state_arr[h*width+w+3]= (field[h][w]!=0) ? 1 : 0;
            }
        }
        //System.out.println(Arrays.toString(_state_arr));
        //System.out.printf("size is %d", _state_arr.length);
        return _state_arr;
    }

    // Definition of game properties.
    @Override
    public int[][] actions()
    {
        int i = 0;
        int[][]actions = new int [numActions()][2];
        for (int orient = 0; orient < 4; ++orient)
        {
            for (int slot = 0; slot < _state.COLS; ++slot)
            {
                actions[i] = new int[]{orient,slot};
                i++;
            }
        }
        return actions;
    }

    @Override
    public boolean checkAction(final int action_index)
    {
        final int[][] valid     = _state.legalMoves();
        int[][] poss_moves = actions();
        int[] wanted_action = poss_moves[action_index];
        int num_all_moves = valid.length;
        for (int i=0; i<num_all_moves; i++)
        {
            if(valid[i][0]==wanted_action[0] && valid[i][1]==wanted_action[1])
            {
                return true;
            }
        }
        return false;
    }

    public int numStates(){ return _state.ROWS*_state.COLS; }

    public int numActions(){ return (_state.COLS*4); }

    // for genetic algorithm
    //input: current _state which includes the action
    //output: _state and whether terminated, reward == num rows cleared!!!
    public Results virtual_move(int[] own__state, int action_index)
    {

        Results outcome = new Results(0, new int[own__state.length],false);

        //find out orientation and slot of wanted move
        int[][] all_moves = this.actions();
        int orient = all_moves[action_index][0];
        int slot = all_moves[action_index][1];

        //reconstruct the field:
        int width_field = own__state[1];
        int height_field = own__state[2];
        outcome.state[1] = own__state[1];
        outcome.state[2] = own__state[2];
        int[][] field = new int[height_field][width_field];
        for (int h=0; h<height_field;h++ )
        {
            for (int w = 0; w < width_field; w++)
            {
                field[h][w] = own__state[3 + h * width_field + w];
            }
        }
        //next piece
        int nextPiece = own__state[own__state[0]];



        //Reading in the needed values and make a copy of the arrays
        //without copy: pointer issues!!!!!!!!
        int[]top_ = _state.getTop();
        int[] top = top_.clone();
        int[][][]pBottom_ = _state.getpBottom();
        int[][][] pBottom = pBottom_.clone();
        int[][] pWidth_ = _state.getpWidth();
        int[][] pWidth = pWidth_.clone();
        int[][] pHeight_ = _state.getpHeight();
        int[][] pHeight = pHeight_.clone();
        int ROWS = _state.ROWS;
        int COLS = _state.COLS;
        int[][][]pTop_ = _state.getpTop();
        int[][][] pTop = pTop_.clone();




        //height if the first column makes contact
        int height = top[slot]-pBottom[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for(int c = 1; c < pWidth[nextPiece][orient];c++)
        {
            height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
        }

        //check if game ended
        if(height+pHeight[nextPiece][orient] >= ROWS)
        {
            outcome.terminated = true;
            return outcome;
        }


        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < pWidth[nextPiece][orient]; i++)
        {

            //from bottom to top of brick
            for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++)
            {
                //System.out.println(h);
                //System.out.println(i+slot);
                field[h][i+slot] = 1; //fill every field with 1!!!
            }
        }

        //adjust top
        for(int c = 0; c < pWidth[nextPiece][orient]; c++)
        {
            top[slot+c]=height+pTop[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--)
        {
            //check all columns in the row
            boolean full = true;
            for(int c = 0; c < COLS; c++)
            {
                if(field[r][c] == 0)
                {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if(full)
            {
                rowsCleared++;
                //for each column
                for(int c = 0; c < COLS; c++)
                {

                    //slide down all bricks
                    for(int i = r; i < top[c]; i++)
                    {
                        field[i][c] = field[i+1][c];
                    }
                    //lower the top
                    top[c]--;
                    while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
                }
            }
        }
        outcome.reward = rowsCleared;

        //deconstruct the field to the new virtual _state!
        for (int h=0; h<height_field; h++)
        {
            for (int w=0; w<width_field; w++)
            {
                outcome.state[h*width_field+w+3]= (field[h][w]!=0) ? 1 : 0;
            }
        }

        return outcome;
    }

    //input: virtual _state
    //output: array of features!
    public double[] features (Results virtual__state_res)
    {
        if(encoder.encoderReady()){
            double[] state_double = new double[this.numStates()];
            for(int i = 0; i < this.numStates(); i++)
                state_double[i] = virtual__state_res.state[i+3];

            return encoder.encoding(convolve(state_double));
            //return encoder.encoding((state_double));
        }
        else{
            int[] virtual__state = virtual__state_res.state;
            int field_width = virtual__state[1];
            int field_height = virtual__state[2];


            double num_cleared_rows = virtual__state_res.reward;                 //FEATURE!

            //calc height of each column
            int[]height_map = new int[field_width];
            //calc number of all blocks!
            int num_blocks = 0;                                                //FEATURE!
            //calc number of weighted sum of blocks!
            int weighted_blocks = 0;                                           //FEATURE!
            //FOR LOOP OVER ENTIRE FIELD!!
            for (int i =0; i<field_width;i++)
            {
                for (int j=0; j<field_height;j++)
                { //go over all possible heights!
                    if (virtual__state[3+i+j*field_width]!=0){
                        height_map[i]=j;
                        num_blocks = num_blocks+1;
                        weighted_blocks = weighted_blocks + (j+1);
                    }
                }
            }

            int max_pile_height = height_map[0];                               //FEATURE!
            for (int i =1; i<field_width;i++){
                if(height_map[i]>max_pile_height){max_pile_height=height_map[i];}
            }

            int min_pile_height = height_map[0];
            for (int i =1; i<field_width;i++){
                if(height_map[i]<min_pile_height){min_pile_height=height_map[i];}
            }

            int max_altitude_difference = max_pile_height - min_pile_height;           //FEATURE!

            //calculate a map of "wells":
            int[]wells_map = new int[field_width];
            for (int i =0; i<field_width;i++){
                if(i==0){
                    if(height_map[1]>height_map[0]){wells_map[0] = height_map[1]-height_map[0];}
                    else {wells_map[0]=0;}
                }
                else if(i==(field_width-1)){
                    if(height_map[field_width-2]>height_map[field_width-1]){wells_map[field_width-1] = height_map[field_width-2]-height_map[field_width-1];}
                    else {wells_map[field_width-1]=0;}
                }
                else {
                    if (height_map[i-1]>height_map[i] & height_map[i+1]>height_map[i]){
                        wells_map[i]= min(height_map[i-1]-height_map[i],height_map[i+1]-height_map[i]);
                    }
                    else {wells_map[i]=0;}
                }
            }

            int max_well_depth = wells_map[0];                                         //FEATURE!
            int sum_of_wells = 0;                                                      //FEATURE!
            for (int i =1; i<field_width;i++){
                sum_of_wells = sum_of_wells + wells_map[i];
                if(wells_map[i]>max_well_depth){max_well_depth=wells_map[i];}
            }




            // calc connected number of holes and all holes
            int conn_num_holes = 0;                                                   //FEATURE!
            int total_num_holes = 0;                                                  //FEATURE!
            for (int i =0; i<field_width*(field_height-1)-1;i++)
            {
                int calc = virtual__state[3+i]-virtual__state[3+field_width+i]; //lower - upper
                //if above there is one but below not -> will yield to -1!!!
                if (calc<0) {conn_num_holes++;}
                if((virtual__state[3+i]==0) & ((i/field_width)<height_map[i%field_width])){total_num_holes++;}
            }

            //calculate horizontal transitions: (slightly other defined than paper)
            int hor_transitions = 0;                                                //FEATURE!
            //FOR LOOP OVER ENTIRE FIELD!!
            for (int i =0; i<field_height;i++)
            {
                for (int j=0; j<(field_width-1);j++)
                {
                    hor_transitions = hor_transitions + abs(virtual__state[3+j+i*field_width]-virtual__state[3+j+1+i*field_width]);
                }
            }

            //calculate vertical transitions: (slightly other defined than paper)
            int ver_transitions = 0;                                              //FEATURE
            //FOR LOOP OVER ENTIRE FIELD!!
            for (int i =0; i<field_width;i++)
            {
                for (int j=0; j<(field_height-1);j++)
                {
                    ver_transitions = ver_transitions + abs(virtual__state[3+i+j*field_width]-virtual__state[3+i+(j+1)*field_width]);
                }
            }


            //from there extract: aggregate height:
            int aggregate_height = 0;                                         //FEATURE
            for (int j=0; j<field_width; j++){
                aggregate_height = aggregate_height + height_map[j];
            }

            //from there extract: bumpieness:
            int bumpieness = 0;                                            //FEATURE!!
            for (int j=1; j<field_width; j++)
            {
                bumpieness = bumpieness + abs(height_map[j]-height_map[j-1]);
            }

            //return new double[]{total_num_holes,num_cleared_rows,aggregate_height,bumpieness};
            //new: 11 features from paper!!!
            return new double[]{max_pile_height,total_num_holes,conn_num_holes,num_cleared_rows,max_altitude_difference,max_well_depth,sum_of_wells,num_blocks,weighted_blocks,hor_transitions,ver_transitions};
        }
    }

    public double[][] trainingStates(final int num_samples)
    {
        final Random generator = new Random();
        double[][] samples = new double[num_samples][this.numStates()];
        for (int k = 0; k < num_samples; ++k) {
            for (int i = 0; i < 200; ++i) {
                samples[k][i] = generator.nextBoolean() ? 1 : 0;
            }
        }

        for (int k = 0; k < num_samples; ++k){
            samples[k] = convolve(samples[k]);
        }
        return samples;
    }

    //return number of features
    public int numFeatures()
    {
        //add +1 since rows cleared is already available after the virtual move!!!
        //TODO: adapt to function above

        return encoder.encoderReady() ? encoder.getEncoderSize() : (10+1);
    }

    @Override
    public void activateVisualisation()
    {
        _visualise_game = true;
        new TFrame(_state);
    }

    private double[]convolve(double[] state_double)
    {
        //3x3 CONVOLUTEN!!!!
        int ROWS = _state.ROWS;
        int COLS = _state.COLS;
        double[] state_double_ = new double[this.numStates()];
        // horizontal addition
        for (int j=0;j<this.numStates();j++){
            if ((j%COLS)==0){state_double_[j] = 2*state_double[j]+state_double[j+1];}
            else if ((j%COLS)==(COLS-1)) {state_double_[j] = 2*state_double[j]+state_double[j-1];}
            else {state_double_[j] = state_double[j-1]+state_double[j]+state_double[j+1];}
        }
        // vertical addition
        for (int j=0;j<this.numStates();j++){
            if ((j/COLS)==0){state_double[j] = 2*state_double_[j]+state_double_[j+COLS];}
            else if ((j/COLS)==(ROWS-1)) {state_double[j] = 2*state_double_[j]+state_double_[j-COLS];}
            else {state_double[j]= state_double_[j-COLS]+state_double_[j]+state_double_[j+COLS];}
        }
        for (int j=0;j<this.numStates();j++){
            state_double[j] = state_double[j]/9;
        }
        return state_double;
    }

}

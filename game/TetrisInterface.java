package game;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.min;
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
        int[] init__state = new int[State.COLS*State.ROWS-1];
        Arrays.fill(init__state, 0);
        init__state[State.COLS*State.ROWS-1 - 1] = _state.getNextPiece();
        return new Results(0.0, init__state, false);
    }


    @Override
    // Execute an given action in game and return its
    // reward, the following _state and if the game has terminated.
    // @param[in]   action      1D-representation of action.
    public Results step(final int action_index)
    {
        assert(encoder != null && encoder.encoderReady());
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
        new_game.encoder = encoder;
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
            for (int slot = 0; slot < State.COLS; ++slot)
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
        for (int i = 0; i < num_all_moves; i++)
        {
            if(valid[i][0]==wanted_action[0] && valid[i][1]==wanted_action[1])
            {
                return true;
            }
        }
        return false;
    }

    public int numStates(){ return State.ROWS*State.COLS; }

    public int numActions(){ return (State.COLS*4); }

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
        int[][][]pBottom_ = State.getpBottom();
        int[][][] pBottom = pBottom_.clone();
        int[][] pWidth_ = State.getpWidth();
        int[][] pWidth = pWidth_.clone();
        int[][] pHeight_ = State.getpHeight();
        int[][] pHeight = pHeight_.clone();
        int ROWS = State.ROWS;
        int COLS = State.COLS;
        int[][][]pTop_ = State.getpTop();
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
        // Calculate field features.
        double[] features = encoder.encoding(virtual__state_res.state);
        // Add number of rows cleared at 3rd position.
        for(int i = features.length-1; i > 2; i--)
        {
            features[i] = features[i-1];
        }
        features[2] = virtual__state_res.reward;
        return features;
    }

    public double[][] trainingStates(final int num_samples)
    {
        final Random generator = new Random();
        double[][] samples = new double[num_samples][this.numStates()];
        for (int k = 0; k < num_samples; ++k)
        {
            for (int i = 0; i < 200; ++i)
            {
                samples[k][i] = generator.nextBoolean() ? 1 : 0;
            }
        }
        return samples;
    }

    //return number of features
    public int numFeatures()
    {
        // Field features plus number of cleared rows.
        return encoder.encoderSize() + 1;
    }

    @Override
    public void activateVisualisation()
    {
        _visualise_game = true;
        new TFrame(_state);
    }

}

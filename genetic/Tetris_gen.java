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
        num_states  = (int)Math.pow(2, 10)*7;
        num_actions = 40;
        actions     = new int[num_actions];
        for(int orient = 0; orient < 10; ++orient) {
            for(int slot = 0; slot < 4; ++slot) {
                final int action_index = orient*4+slot;
                actions[action_index]  = action_index;
            }
        }
        // Start super game class.
        state = new State();
        new TFrame(state);
    }

    @Override
    public Results step(final int action_index) {
        //This is where we ultimately MAKE!! the move

        int[]tryout = this.state();

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
    protected boolean terminal() {
        return state.hasLost();
    }
    //This one returns whether the game is over or not!!!

    @Override
    protected double reward() {
        return 0;
    }
    //This function gets a state and computes the reward!

    @Override
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
                state_arr[h*width+w+3]=field[h][w];
            }
        }
        //System.out.println(Arrays.toString(state_arr));
        //System.out.printf("size is %d", state_arr.length);
        return state_arr;
    }

    @Override
    public Game restart() {
        //just to restart!!!
        return new Tetris_gen();
    }

    //TODO!!: Function which gives the possible moves!!!

    //TODO!!! FUnction which makes a "virtual move"!!!

}

package genetic;

import game.Game;
import game.Results;
import game.State;
import game.TFrame;

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
        return new int[] {-1};
    }

    @Override
    public Game restart() {
        //just to restart!!!
        return new Tetris_gen();
    }

    //TODO!!: Function which gives the possible moves!!!

    //TODO!!! FUnction which makes a "virtual move"!!!

}

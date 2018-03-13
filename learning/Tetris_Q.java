package learning;

import game.Game;
import game.Results;
import game.State;

public class Tetris_Q extends Game {

    private State state;

    public Tetris_Q() {
        // Define game specific variables.
        // num_states : top-row boolean for every figure (7)
        // num_actions: each column (10) with each orientation (4)
        //num_states = (int) Math.pow(2, 10) * 7;
        //num_actions = 40;
        //actions = new int[num_actions];
        //for (int orient = 0; orient < 10; ++orient) {
        //    for (int slot = 0; slot < 4; ++slot) {
        //        final int action_index = orient * 4 + slot;
        //        actions[action_index] = action_index;
        //    }
        //}
        // Start super game class.
        state = new State();
    }

    @Override
    public Results step(final int action_index) {
        state.makeMove(0);
        // Draw new state and next piece.
        state.draw();
        state.drawNext(0, 0);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Results(reward(), state(), terminal());
    }

    @Override
    public Results initial(){return new Results(reward(), state(), terminal());}

    @Override
    protected boolean terminal() {
        return state.hasLost();
    }

    @Override
    protected double reward() {
        return 0;
    }

    @Override
    public int[] state() {
        return new int[]{-1};
    }

    @Override
    public Game restart() {
        return new Tetris_Q();
    }

    @Override
    public int[][] actions() {
        return new int[1][1];
    }

    @Override
    public boolean checkAction(final int action_index) {
        return true;
    }

    @Override
    public int numStates() {
        return (int) Math.pow(2, 10) * 7;
    }

    @Override
    public int numActions() {
        return 40;
    }

    @Override
    public Results virtual_move(int[] own_state, int action_index) {
        return new Results(reward(), state(), terminal());
    }

    @Override
    public double[] features(Results virtual_state_res) {
        return new double[]{0};
    }

    @Override
    public int numfeatures() {
        return 0;
    }

    @Override
    public void activateVisualisation() {
        int a = 0;
    }//do noting

    @Override
    public int toScalarState(final int[] state){return 0;}
}

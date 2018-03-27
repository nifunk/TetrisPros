package learning;

import game.Game;
import game.Results;
import game.State;
import game.TFrame;

import java.util.Arrays;
import java.util.HashMap;

public class Tetris_Q extends Game
{

    private State state;
    private HashMap<String, Integer> state_map = new HashMap<>();

    private static int N_STATE   = State.COLS + 1;
    private static int N_ACTIONS = State.COLS * 4;

    private boolean visualise_game = false;

    public Tetris_Q()
    {
        // Fill map of states.
        int state_index = 0;
        int[] ex_state  = new int[N_STATE];
        for (int i = 0; i < Math.pow(2, State.COLS); i++)
        {
            for (int k = State.COLS - 1; k >= 0; k--)
                ex_state[k] = (i & (1 << k)) != 0 ? 1 : 0;
            for (int piece = 0; piece < State.N_PIECES; piece++)
            {
                ex_state[N_STATE - 1] = piece;
                state_map.put(Arrays.toString(ex_state), state_index);
                state_index++;
            }
        }
        // Start tetris game.
        state = new State();
        if (visualise_game) activateVisualisation();
    }

    @Override
    public Results initial()
    {
        int[] init_state = new int[N_STATE];
        Arrays.fill(init_state, 0);
        init_state[N_STATE - 1] = state.getNextPiece();
        return new Results(0.0, init_state, false);
    }

    @Override
    public Results step(final int action_index)
    {
        final int orient = action_index / State.COLS;
        final int slot   = action_index % State.COLS;
        state.makeMove(orient, slot);
        if (visualise_game)
        {
            state.draw(); state.drawNext(0,0);
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Results(reward(), state(), terminal());
    }

    @Override
    protected boolean terminal() {
        return state.hasLost();
    }

    @Override
    protected double reward() {
        return - getHighest();
    }

    @Override
    public int[] state()
    {
        int[] current      = Arrays.copyOf(getHighestRow(), N_STATE);
        current[N_STATE-1] = state.getNextPiece();
        return current;
    }

    @Override
    public int toScalarState(final int[] trafo_state) {
        return state_map.get(Arrays.toString(trafo_state));
    }

    private int[] getHighestRow()
    {
        final int highest_row = getHighest();
        int[] return_row = new int[State.COLS];
        int[][] field    = state.getField();
        for (int k = 0; k < return_row.length; ++k)
            return_row[k] = field[State.ROWS - highest_row][k] != 0 ? 1 : 0;
        return return_row;
    }

    private int getHighest()
    {
        final int[] top = state.getTop();
        int highest = -1;
        for (int height : top)
            if (height > highest) highest = height;
        return highest;
    }

    @Override
    public Game restart() { return new Tetris_Q(); }

    @Override
    public int[][] actions()
    {
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
    public boolean checkAction(final int action_index)
    {
        final int[][] valid     = state.legalMoves();
        final int action_orient = action_index / State.COLS;
        final int action_slot   = action_index % State.COLS;
        for (int[] valid_action : valid)
            if(valid_action[0] == action_orient && valid_action[1] == action_slot)
            {
                return true;
            }
        return false;
    }

    @Override
    public int numStates(){return state_map.size(); }

    @Override
    public int numActions() { return N_ACTIONS; }

    @Override
    public void activateVisualisation()
    {
        visualise_game = true;
        new TFrame(state);
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
    public int numFeatures() {
        return 0;
    }

}

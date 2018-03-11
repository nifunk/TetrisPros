package learning;

import game.Game;
import game.Results;
import game.State;
import game.TFrame;

import java.util.Arrays;
import java.util.HashMap;

public class Tetris_Q extends Game {

    private State state;
    private HashMap<String, Integer> state_map = new HashMap<>();

    private static int N_STATE   = State.COLS + 1;
    private static int N_ACTIONS = State.COLS * 4;

    public Tetris_Q() {
        // Fill map of states.
        int state_index = 0;
        int[] ex_state  = new int[N_STATE];
        for (int i = 0; i < Math.pow(2, State.COLS); i++) {
            for (int k = State.COLS - 1; k >= 0; k--) {
                ex_state[k] = (i & (1 << k)) != 0 ? 1 : 0;
            }
            for (int piece = 0; piece < State.N_PIECES; piece++) {
                ex_state[N_STATE - 1] = piece;
                state_map.put(Arrays.toString(ex_state), state_index);
                state_index++;
            }
        }
        // Start tetris game.
        state = new State();
        new TFrame(state);
    }

    @Override
    public Results initial() {
        int[] init_state = new int[N_STATE];
        Arrays.fill(init_state, 0);
        init_state[N_STATE - 1] = state.getNextPiece();
        return new Results(0.0, init_state, false);
    }

    @Override
    public Results step(final int action_index) {
        final int orient = action_index / State.COLS;
        final int slot   = action_index % State.COLS;
        state.makeMove(orient, slot);
        // Draw new state and next piece.
        state.draw(); state.drawNext(0,0);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Results(reward(), state(), terminal());
    }

    @Override
    protected boolean terminal() {
        return state.hasLost();
    }

    @Override
    protected double reward() {
        final int[] highest_row = getHighestRow();
        int stone_count         = 0;
        for (int stone : highest_row) {
            if (stone != 0) stone_count++;
        }
        return - stone_count; 
    }

    @Override
    protected int[] state() {
        int[] current      = Arrays.copyOf(getHighestRow(), N_STATE);
        current[N_STATE-1] = state.getNextPiece();
        return current;
    }

    @Override
    public int toScalarState(final int[] trafo_state) {
        return state_map.get(Arrays.toString(trafo_state));
    }

    private int[] getHighestRow() {
        final int[] top = state.getTop();
        int highest_row = -1;
        for (int height : top) {
            if (height > highest_row) highest_row = height;
        }
        int[] return_row = new int[State.COLS];
        int[][] field    = state.getField();
        for (int k = 0; k < return_row.length; ++k) {
            return_row[k] = field[State.ROWS - highest_row][k] != 0 ? 1 : 0;
        }
        return return_row;
    }

    @Override
    public Game restart() {
        return new Tetris_Q();
    }

    @Override
    public int[] actions() {
        int[] actions = new int[numActions()];
        for(int orient = 0; orient < 4; ++orient) {
            for(int slot = 0; slot < State.COLS; ++slot) {
                final int action_index = orient*State.COLS+slot;
                actions[action_index]  = action_index;
            }
        }
        return actions;
    }

    @Override
    public boolean checkAction(final int action_index) {
        final int[][] valid     = state.legalMoves();
        final int action_orient = action_index / State.COLS;
        final int action_slot   = action_index % State.COLS;
        for (int[] valid_action : valid) {
            if(valid_action[0] == action_orient && valid_action[1] == action_slot) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int numStates() {
        return state_map.size();
    }

    @Override
    public int numActions() {
        return N_ACTIONS;
    }

}

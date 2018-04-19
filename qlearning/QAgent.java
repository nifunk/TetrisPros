/*package qlearning;

//import autoencoder.StateEncoder;
import game.Game;
import game.Results;

import java.util.Arrays;

public class QAgent
{
    private Game _game;
    private QLearning _qkb;
    private StateEncoder _encoder;

    private final int Q_TRAINING_ITERATIONS = 10000;
    private final int ENC_TRAINING_SAMPLES  = 100000;

    public QAgent(Game game)
    {
        _game    = game;
        _encoder = new StateEncoder();
        _qkb     = new QLearning(_encoder.getEncoderSize(), _game.numActions());
    }

    public void perform()
    {
        game.Results results  = _game.initial();
        int terminal_counter  = 0;
        while (terminal_counter < 10)
        {
            final int flat_state = flattenState(results.state);
            final int action = _qkb.act(flat_state, false);
            results = new Results(-1000.0, results.state, true);
            if (_game.checkAction(action)) results = _game.step(action);
            if (results.terminated)
            {
                _game = _game.restart();
                terminal_counter++;
            }
        }
    }

    public void adapt()
    {
        adaptEncoder();
        adaptQLearner();
    }

    private void adaptQLearner()
    {
        System.out.printf("\nQAgent: Adapting q matrix in %d iterations\n",
                           Q_TRAINING_ITERATIONS);
        // Training periods - Adapt q matrix by exploration.
        game.Results current = _game.initial(); game.Results next;
        for(int k = 0; k < Q_TRAINING_ITERATIONS; ++k)
        {
            // Determine and perform action.
            final int current_flat = flattenState(current.state);
            final int action = _qkb.act(current_flat, true);
            next = new Results(-1000.0, current.state, true);
            if (_game.checkAction(action)) next = _game.step(action);
            // Adapt QKB and allocate next to current state.
            final int next_flat = flattenState(next.state);
            _qkb.adapt(current_flat, next_flat, action,
                       next.reward, k, Q_TRAINING_ITERATIONS);
            current = next;
            // Restart game as necessary.
            if (current.terminated) _game = _game.restart();
            System.out.printf("Finished iteration (%d)\n", k);
        }
    }

    private void adaptEncoder()
    {
        System.out.println("\nQLearner: Adapting auto encoder");
        _encoder.buildNetwork(_game.numStates());
        _encoder.adapt(_game.trainingStates(ENC_TRAINING_SAMPLES));
    }

    // IDEA: Maybe replace simple maximum index by k-means clustering or SOM.
    private int flattenState(final int[] state)
    {
        assert(_encoder.encoderReady());
        // Encode state to smaller state's size.
        double[] state_double = new double[state.length];
        for(int i = 0; i < state.length; i++)
            state_double[i] = state[i];
        final double[] state_encoded = _encoder.encoding(state_double);
        // Return maximal index as flat state (flatten to 1D).
        int max_index = 0;
        for (int i = 0; i < state_encoded.length; i++)
        {
            max_index = state_encoded[i] > state_encoded[max_index] ? i : max_index;
        }

        System.out.printf("Encoding = %s\n", Arrays.toString(state_encoded));
        //System.out.printf("Encoding = %d\n", max_index);

        return max_index;
    }

    public void load(final String q_matrix_file, final String enc_file)
    {
        if(q_matrix_file != null) _qkb.load(q_matrix_file);
        if(enc_file  != null)     _encoder.load(enc_file);
    }

    public void store(final String q_matrix_file, final String enc_file)
    {
        if(q_matrix_file != null) _qkb.store(q_matrix_file);
        if(enc_file != null)      _encoder.store(enc_file);
    }

    public Game getGame() {return _game; }

}*/

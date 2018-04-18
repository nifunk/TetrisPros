package qlearning;

import autoencoder.Encoder;
import autoencoder.NeuralEncoder;
import game.Game;
import game.Results;

import java.util.Arrays;

public class QAgent
{
    private Game _game;
    private QLearning _qkb;

    private final int Q_TRAINING_ITERATIONS = 100000;

    public QAgent(Game game, final Encoder encoder)
    {
        _game         = game;
        _qkb          = new QLearning(_game.numStates(), _game.numActions());
        _game.encoder = encoder;
    }

    public void perform()
    {
        game.Results results  = _game.initial();
        int terminal_counter  = 0;
        while (terminal_counter < 10)
        {
            final int flat_state = _game.encoder.flatten(results.state);
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
        System.out.printf("\nQAgent: Adapting q matrix in %d iterations\n",
                          Q_TRAINING_ITERATIONS);
        // Training periods - Adapt q matrix by exploration.
        game.Results current = _game.initial(); game.Results next;
        for(int k = 0; k < Q_TRAINING_ITERATIONS; ++k)
        {
            // Determine and perform action.
            final int current_flat = _game.encoder.flatten(current.state);
            final int action = _qkb.act(current_flat, true);
            next = new Results(-1000.0, current.state, true);
            if (_game.checkAction(action)) next = _game.step(action);
            // Adapt QKB and allocate next to current state.
            final int next_flat = _game.encoder.flatten(next.state);
            _qkb.adapt(current_flat, next_flat, action,
                       next.reward, k, Q_TRAINING_ITERATIONS);
            current = next;
            // Restart game as necessary.
            if (current.terminated) _game = _game.restart();
            System.out.printf("Finished iteration (%d)\n", k);
        }
    }

    public void load(final String q_matrix_file)
    {
        _qkb.load(q_matrix_file);
    }

    public void store(final String q_matrix_file)
    {
        _qkb.store(q_matrix_file);
    }

    public Game getGame() {return _game; }

}

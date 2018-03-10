package learning;

import game.Game;
import game.Results;
import tests.CTB;

import java.util.Random;

public class QLearning {

    private QMatrix q_matrix;
    private Game game;
    private final double MIN_ALPHA = 0.2;
    private final double GAMMA     = 1.0;
    private final double EPS       = 0.2;

    public QLearning(Game game) {
        this.game = game;
        q_matrix  = new QMatrix(game.num_states, game.num_actions);
    }

    public int perform() {
        int total_reward = 0;
        Results results  = new Results(0, new int[]{0}, false);
        while (! results.terminated) {
            results       = game.step(act(results.state[0]));
            total_reward += results.reward;
        }
        return total_reward;
    }

    public void adapt(final int iterations) {
        // Initialise learning rates as decreasing with time
        // for better adaption.
        double[] alphas = new double[iterations];
        for(int k = 0; k < iterations; ++k) {
            alphas[k] = 1.0 - (1.0 - MIN_ALPHA)/iterations*k;
        }
        // Training periods - Adapt q matrix by exploration.
        Results current = new Results(0, new int[]{0}, false);
        for(int k = 0; k < iterations; ++k) {
            final int action = act(current.state[0]);
            Results next = game.step(action);
            q_matrix.adapt(current.state[0], next.state[0],
                           action, next.reward, alphas[k]);
            current = next;
            if (current.terminated) game = game.restart();

            System.out.printf("Finished iteration (%d)\n", k);
        }
    }

    private int act(final int state) {
        final Random generator = new Random();
        if (generator.nextDouble() < EPS) {
            return generator.nextInt(CTB.num_actions);
        } else {
            return q_matrix.bestAction(state);
        }
    }

    private class QMatrix {

        private double[][] q_matrix;

        private QMatrix(final int num_states, final int num_actions) {
            q_matrix = new double[num_states][num_actions];
        }

        private int bestAction(final int state) {
            final double[] options = q_matrix[state];
            int best_option        = -1;
            double max             = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < options.length; i++) {
                final double elem = options[i];
                if (elem > max) {
                    max = elem;
                    best_option = i;
                }
            }
            return best_option;
        }

        private void adapt(final int state, final int next_state,
                           final int action, final double reward, final double alpha) {
            final double exp_total_reward = reward + GAMMA*bestAction(next_state);
            q_matrix[state][action] += alpha*(exp_total_reward - q_matrix[state][action]);
        }

    }
}

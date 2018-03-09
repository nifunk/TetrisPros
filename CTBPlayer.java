import tests.CTB;

import java.util.Random;

public class CTBPlayer {

    private QMatrix q_matrix    = new QMatrix(CTB.num_states, CTB.num_actions);
    private double min_alpha    = 0.2;
    private double gamma        = 1.0;
    private double eps          = 0.2;

    public static void main(String[] args) {
        CTBPlayer player = new CTBPlayer();
        player.adapt(10000);
        System.exit(0);
    }

    private CTBPlayer() { }

    private int perform() {
        final CTB game      = new CTB();
        int total_reward    = 0;
        CTB.Results results = new CTB.Results();
        while (! results.terminated) {
            results       = game.step(act(results.state));
            total_reward += results.reward;
        }
        return total_reward;
    }

    private void adapt(final int iterations) {
        // Initialise learning rates as decreasing with time
        // for better adaption.
        double[] alphas = new double[iterations];
        for(int k = 0; k < iterations; ++k) {
            alphas[k] = 1.0 - (1.0 - min_alpha)/iterations*k;
        }
        // Training periods - Adapt q matrix by exploration.
        CTB game            = new CTB();
        CTB.Results current = new CTB.Results();
        for(int k = 0; k < iterations; ++k) {
            final int action = act(current.state);

            System.out.printf("Action: %d\n", action);

            CTB.Results next = game.step(action);

            System.out.printf("Next: %d %d\n", next.reward, next.state);
            q_matrix.adapt(current.state, next.state,
                           action, next.reward, alphas[k]);
            current = next;
            if (current.terminated) game = new CTB();

            System.out.printf("Finished iteration (%d)\n", k);
        }
    }

    private int act(final int state) {
        final Random generator = new Random();
        if (generator.nextDouble() < eps) {
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
                           final int action, final int reward, final double alpha) {
            final double exp_total_reward = reward + gamma*bestAction(next_state);
            System.out.println(state); System.out.println(action);
            q_matrix[state][action] += alpha*(exp_total_reward - q_matrix[state][action]);
        }

    }


}

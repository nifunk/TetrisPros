package learning;

import game.Game;
import game.Results;

import java.io.*;
import java.util.Random;

public class QLearning
{

    public QMatrix q_matrix;
    public Game game;
    private final double MIN_ALPHA = 0.2;
    private final double GAMMA     = 1.0;
    private final double EPS       = 0.3;

    private boolean training_mode  = false;

    public QLearning(Game game)
    {
        this.game = game;
        q_matrix  = new QMatrix(game.numStates(), game.numActions());
    }

    public void perform()
    {
        training_mode = false;
        game.Results results  = game.initial();
        int terminal_counter  = 0;
        while (terminal_counter < 10)
        {
            final int action = act(results.state);
            if (game.checkAction(action))
            {
                results = game.step(action);
            } else {
                results = new Results(-1000.0, results.state, results.terminated);
                results.terminated = true;
            }
            if (results.terminated)
            {
                game = game.restart();
                terminal_counter++;
            }
        }
    }

    public void adapt(final int iterations)
    {
        training_mode = true;
        // Initialise learning rates as decreasing with time
        // for better adaption.
        double[] alphas = new double[iterations];
        for(int k = 0; k < iterations; ++k)
            alphas[k] = 1.0 - (1.0 - MIN_ALPHA)/iterations*k;
        // Training periods - Adapt q matrix by exploration.
        Results current = game.initial();
        Results next;
        for(int k = 0; k < iterations; ++k)
        {
            final int action = act(current.state);
            if (game.checkAction(action))
            {
                next = game.step(action);
            } else {
                next = new Results(-1000.0, current.state, current.terminated);
            }
            q_matrix.adapt(game.toScalarState(current.state),
                           game.toScalarState(next.state),
                           action, next.reward, alphas[k]);
            current = next;
            if (current.terminated) game = game.restart();

            System.out.printf("Finished iteration (%d)\n", k);
        }
    }

    private int act(final int[] state)
    {
        final Random generator = new Random();
        if (training_mode && generator.nextDouble() < EPS)
        {
            return generator.nextInt(game.numActions());
        } else {
            return q_matrix.bestAction(game.toScalarState(state));
        }
    }

    public class QMatrix
    {

        private double[][] q_matrix;

        private QMatrix(final int num_states, final int num_actions)
        {
            q_matrix = new double[num_states][num_actions];
        }

        private int bestAction(final int scalar_state)
        {
            final double[] options = q_matrix[scalar_state];
            int best_option        = -1;
            double max             = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < options.length; i++)
            {
                final double elem = options[i];
                if (elem > max)
                {
                    max = elem;
                    best_option = i;
                }
            }
            return best_option;
        }

        private void adapt(final int scalar_state, final int scalar_next_state,
                           final int action, final double reward, final double alpha)
        {
            final double exp_total_reward = reward + GAMMA*bestAction(scalar_next_state);
            q_matrix[scalar_state][action] += alpha*(exp_total_reward - q_matrix[scalar_state][action]);
        }

        public void storeMatrix(final String filename)
        {
            try
            {
                final FileWriter fw = new FileWriter(filename);
                for (double[] action_rewards : q_matrix)
                    for (double reward : action_rewards)
                    {
                        fw.write(reward + ",");
                    }
                    fw.write("\n");
                fw.close();
            } catch (IOException e) { e.printStackTrace(); }
            System.out.println("Stored Q Matrix in " + filename);
        }

        public void loadMatrix(final String filename)
        {
            int x = 0, y;
            try
            {
                final BufferedReader in = new BufferedReader(new FileReader(filename));
                String line;
                while ((line = in.readLine()) != null)
                {
                    final String[] values = line.split(",");
                    y = 0;
                    for (String str : values)
                    {
                        q_matrix[x][y] = Double.parseDouble(str);
                        y++;
                    }
                    x++;
                }
            } catch (IOException e) { e.printStackTrace(); }
            System.out.println("Loaded Q Matrix in " + filename);
        }
    }
}

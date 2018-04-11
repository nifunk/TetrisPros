package qlearning;

import java.io.*;
import java.util.Random;

public class QLearning
{

    private QMatrix _q_matrix;

    private int _num_states;
    private int _num_actions;

    private final double MIN_ALPHA = 0.2;
    private final double GAMMA     = 1.0;
    private final double EPS       = 0.3;

    public QLearning(final int num_states, final int num_actions)
    {
        _num_states = num_states; _num_actions = num_actions;
        _q_matrix  = new QMatrix(_num_states, _num_actions);
    }

    public int act(final int state, final boolean training_mode)
    {
        final Random generator = new Random();
        if (training_mode && generator.nextDouble() < EPS)
        {
            return generator.nextInt(_num_actions);
        } else {
            return _q_matrix.bestAction(state);
        }
    }

    public void adapt(final int state, final int next_state,
                      final int action, final double reward,
                      final int iteration, final int max_iterations)
    {
        // Learning rate decreasing with time for better adaption.
        final double alpha = 1.0 - (1.0 - MIN_ALPHA)/max_iterations*iteration;
        // Adapt internal q matrix with state, next_state, etc.
        _q_matrix.adapt(state, next_state, action, reward, alpha);
    }

    public void store(final String filename) {_q_matrix.storeMatrix(filename);}

    public void load(final String filename) {_q_matrix.loadMatrix(filename);}

    private class QMatrix
    {

        private double[][] _q_matrix;

        private QMatrix(final int num_states, final int num_actions)
        {
            _q_matrix = new double[num_states][num_actions];
        }

        private int bestAction(final int scalar_state)
        {
            final double[] options = _q_matrix[scalar_state];
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

        private void adapt(final int state, final int next_state,
                           final int action, final double reward, final double alpha)
        {
            final double exp_total_reward = reward + GAMMA*bestAction(next_state);
            _q_matrix[state][action] += alpha*(exp_total_reward - _q_matrix[state][action]);
        }

        private void storeMatrix(final String filename)
        {
            try
            {
                final FileWriter fw = new FileWriter(filename);
                for (double[] action_rewards : _q_matrix)
                    for (double reward : action_rewards)
                    {
                        fw.write(reward + ",");
                    }
                    fw.write("\n");
                fw.close();
            } catch (IOException e) { e.printStackTrace(); }
            System.out.println("Stored Q Matrix in " + filename);
        }

        private void loadMatrix(final String filename)
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
                        _q_matrix[x][y] = Double.parseDouble(str);
                        y++;
                    }
                    x++;
                }
            } catch (IOException e) { e.printStackTrace(); }
            System.out.println("Loaded Q Matrix in " + filename);
        }
    }
}

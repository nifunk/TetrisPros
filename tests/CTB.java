package tests;

import game.Game;
import game.Results;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Random;
import javax.swing.*;

import static java.lang.Math.abs;

public class CTB extends Game
{

    private CTBState state = new CTBState();
    private Panel panel    = new Panel();
    private JFrame frame;

    private boolean visualise_game = false;

    public CTB()
    {
        if (visualise_game) activateVisualisation();
    }

    @Override
    public Results initial() { return new Results(0.0, state(), false); }

    @Override
    public Results step(final int action)
    {
        //MAKE THE ACTION
        state.update(actions()[0][action]);
        if (visualise_game) panel.repaint();
        //CALCULATE THE REWARD:
        int[] firstHalf = Arrays.copyOfRange(this.state(), 0, this.numStates()/2);
        int[] secondHalf = Arrays.copyOfRange(this.state(), this.numStates()/2, numStates());
        int brett_pos = 0;
        int ball_pos = 0;
        for (int i=0; i<(numStates()/2);i++){
            if (firstHalf[i]!=0){brett_pos=i;}
            if (secondHalf[i]!=0){ball_pos=i;}
        }
        //Reward is negative distance such that we can later search for maximum!
        int distance = -abs(brett_pos-ball_pos);
        return new Results(distance, state(), terminal());
    }

    @Override
    protected boolean terminal() {
        return state.ball_pos.y > CTBConstants.window_height;
    }

    @Override
    protected double reward() {
        return - (double) abs(state.catcher_pos.x - state.ball_pos.x);
    }

    @Override
    public int[] state()
    {
        int[] state_array  = new int[numStates()];
        state_array[state.catcher_pos.x]                          = 1;
        state_array[CTBConstants.window_width + state.ball_pos.x] = 1;

        System.out.printf("Catcher = %d, Ball = %d\n", state.catcher_pos.x, state.ball_pos.x);

        return state_array;
    }

    @Override
    public Game restart()
    {
        CTB new_game = new CTB();
        if (visualise_game)
        {
            frame.setVisible(false);
            frame.dispose();
            new_game.activateVisualisation();
        }
        return new_game;
    }

    @Override
    public int[][] actions()
    {
        int[][] actions = new int[1][];
        actions[0]    = new int[]{-CTBConstants.catcher_speed, 0, +CTBConstants.catcher_speed};
        return actions;
    }

    @Override
    public boolean checkAction(final int action_index)
    {
        final int x = state.catcher_pos.x;
        return (0 <= action_index && action_index < numActions()) &&
                ((action_index == 1)
                 || (action_index == 2 && x <= CTBConstants.window_width - CTBConstants.catcher_speed*2)
                 || (action_index == 0 && x >= CTBConstants.catcher_speed*2));
    }

    @Override
    public int numStates()
    {
        return 2*CTBConstants.window_width;
    }

    @Override
    public int numActions() { return 3; }

    @Override
    public Results virtual_move(int[] own_state, int action_index)
    {
        //Assumes: Move was checked before already!!!
        int[] temp_state_array  = new int[numStates()];
        //move the slider!
        temp_state_array[state.catcher_pos.x + actions()[0][action_index]] = 1;
        temp_state_array[CTBConstants.window_width + state.ball_pos.x] = 1;
        boolean game_over = ((state.ball_pos.y-CTBConstants.ball_speed) > CTBConstants.window_height);

        return new Results(0, temp_state_array, game_over);
    }

    @Override
    public double[] features(Results virtual_state_res)
    {
        // TODO: HERE HAS TO GO THE AUTOENCODER!!
        int[] firstHalf = Arrays.copyOfRange(virtual_state_res.state, 0, numStates()/2);
        int[] secondHalf = Arrays.copyOfRange(virtual_state_res.state, numStates()/2, numStates());
        int brett_pos = 0;
        int ball_pos = 0;
        for (int i=0; i<(numStates()/2);i++){
            if (firstHalf[i]!=0){brett_pos=i;}
            if (secondHalf[i]!=0){ball_pos=i;}
    }
        int distance = abs(brett_pos-ball_pos);

        return new double[]{distance};
    }

    @Override
    // TODO: apapt them depending how our autoencoder is designed to be!!!
    public int numFeatures() { return 1; }

    private class Panel extends JPanel
    {

        private Panel() {}

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillOval(state.ball_pos.x, state.ball_pos.y - CTBConstants.ball_radius,
                       CTBConstants.ball_radius * 2, CTBConstants.ball_radius * 2);
            g.fillRect(state.catcher_pos.x, state.catcher_pos.y,
                       CTBConstants.ball_radius * 2, 10);
        }

        @Override
        public void repaint()
        {
            super.repaint();
            try {
                Thread.sleep(CTBConstants.frame_delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void activateVisualisation()
    {
        visualise_game = true;
        frame = new JFrame("Catch the Ball !");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(CTBConstants.window_width, CTBConstants.window_height);
        frame.setVisible(true);
    }

    @Override
    public double[][] trainingStates(final int num_samples)
    {
        final Random generator = new Random();
        double[][] samples = new double[num_samples][numStates()];
        for (int k = 0; k < num_samples; ++k)
        {
            final int random_index   = generator.nextInt(numStates());
            samples[k][random_index] = 1.0;
        }
        return samples;
    }
}
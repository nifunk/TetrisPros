package tests;

import game.Game;
import game.Results;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;

public class CTB extends Game {

    private CTBState state = new CTBState();
    private Panel panel    = new Panel();
    private JFrame frame;

    private boolean visualise_game = false;

    public CTB()
    {
        if (visualise_game) activateVisualisation();
    }

    @Override
    public Results initial() {
        return new Results(0.0, new int[]{0}, false);
    }

    @Override
    public Results step(final int action)
    {
        state.update(actions()[0][action]);
        if (visualise_game) panel.repaint();
        return new Results(reward(), state(), terminal());
    }

    @Override
    protected boolean terminal() {
        return state.ball_pos.y > CTBConstants.window_height;
    }

    @Override
    protected double reward() {
        return - (double)Math.abs(state.catcher_pos.x - state.ball_pos.x);
    }

    @Override
    public int[] state() {
        return new int[]{state.catcher_pos.x - state.ball_pos.x + CTBConstants.window_width};
    }

    @Override
    public int toScalarState(final int[] state) {
        return Math.max(state[0], 0);
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
        if (action_index == 2 && x >= CTBConstants.window_width - CTBConstants.catcher_speed*2
            || action_index == 0 && x <= CTBConstants.catcher_speed*2) { return false; }
        return 0 <= action_index && action_index < numActions();
    }

    @Override
    public int numStates() {
        return 2*CTBConstants.window_width;
    }

    @Override
    public int numActions() { return 3; }

    @Override
    public Results virtual_move(int[] own_state, int action_index)
    {
        return new Results(reward(), state(), terminal());
    }

    @Override
    public double[] features(Results virtual_state_res)
    {
        return new double[]{0};
    }

    @Override
    public int numFeatures() { return 0; }

    private class Panel extends JPanel {

        private Panel() {}

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillOval(state.ball_pos.x, state.ball_pos.y - CTBConstants.ball_radius,
                    CTBConstants.ball_radius * 2, CTBConstants.ball_radius * 2);
            g.fillRect(state.catcher_pos.x, state.catcher_pos.y,
                    CTBConstants.ball_radius*2, 10);
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
}
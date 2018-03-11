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

    public CTB() {
        if (visualise_game) activateVisualisation();
    }

    @Override
    public Results initial() {
        return new Results(0.0, new int[]{0}, false);
    }

    @Override
    public Results step(final int action) {
        state.update(actions()[action]);
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
    protected int[] state() {
        return new int[]{state.catcher_pos.x - state.ball_pos.x + CTBConstants.window_width};
    }

    @Override
    public int toScalarState(final int[] state) {
        return state[0];
    }

    @Override
    public Game restart() {
        frame.setVisible(false);
        frame.dispose();
        return new CTB();
    }

    @Override
    public int[] actions() {
        return new int[]{-CTBConstants.catcher_speed, 0, +CTBConstants.catcher_speed};
    }

    @Override
    public boolean checkAction(final int action_index) {
        return 0 <= action_index && action_index < 3;
    }

    @Override
    public int numStates() {
        return 2*CTBConstants.window_width;
    }

    @Override
    public int numActions() {
        return actions().length;
    }

    private class Panel extends JPanel {

        private Panel() {}

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillOval(state.ball_pos.x, state.ball_pos.y - CTBConstants.ball_radius,
                       CTBConstants.ball_radius * 2, CTBConstants.ball_radius * 2);
            g.fillRect(state.catcher_pos.x, state.catcher_pos.y,
                       CTBConstants.ball_radius*2, 10);
        }

        @Override
        public void repaint() {
            super.repaint();
            try {
                Thread.sleep(CTBConstants.frame_delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void activateVisualisation() {
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
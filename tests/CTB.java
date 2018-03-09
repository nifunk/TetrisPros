package tests;

import learning.Game;
import learning.Results;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;

public class CTB extends Game {

    private CTBState state = new CTBState();
    private Panel panel    = new Panel();

    public CTB() {
        // Define game specific variables.
        num_states  = 2*CTBConstants.window_width;
        actions     = new int[]{-CTBConstants.catcher_speed, 0, +CTBConstants.catcher_speed};
        num_actions = 3;
        // Initialise visualisation frame.
        JFrame frame = new JFrame("Catch the Ball !");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(CTBConstants.window_width, CTBConstants.window_height);
        frame.setVisible(true);
    }

    @Override
    public Results step(final int action_index) {
        state.update(actions[action_index]);
        panel.repaint();
        return new Results(reward(), state(), terminal());
    }

    @Override
    protected boolean terminal() {
        return state.ball_pos.y > CTBConstants.window_height;
    }

    @Override
    protected int reward() {
        return - Math.abs(state.catcher_pos.x - state.ball_pos.x);
    }

    @Override
    protected int state() {
        return state.catcher_pos.x - state.ball_pos.x + CTBConstants.window_width;
    }

    @Override
    public Game restart() {
        return new CTB();
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
}
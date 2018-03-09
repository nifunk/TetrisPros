package tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;

public class CTB {

    static public int num_states  = 2*CTBConstants.window_width;
    static public int[] actions   = {-CTBConstants.catcher_speed, 0, +CTBConstants.catcher_speed};
    static public int num_actions = 3;

    private CTBState state = new CTBState();
    private Panel panel    = new Panel();

    public CTB() {
        JFrame frame = new JFrame("Catch the Ball !");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(CTBConstants.window_width, CTBConstants.window_height);
        frame.setVisible(true);
    }

    public Results step(final int action_index) {
        state.update(actions[action_index]);
        panel.repaint();
        return new Results(reward(), state(), terminal());
    }

    private boolean terminal() {
        return state.ball_pos.y > CTBConstants.window_height;
    }

    private int reward() {
        return - Math.abs(state.catcher_pos.x - state.ball_pos.x);
    }

    private int state() {
        return state.catcher_pos.x - state.ball_pos.x + CTBConstants.window_width;
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

    public static class Results {

        public int reward;
        public int state;
        public boolean terminated;

        public Results(final int rew, final int sta, final boolean term) {
            reward = rew; state = sta; terminated = term;
        }

        public Results() {
            reward = 0; state = 0; terminated = false;
        }
    }
}
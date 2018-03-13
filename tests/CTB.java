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

    public CTB() {
        // Define game specific variables.
        //num_states  = 2*CTBConstants.window_width;
        //actions     = new int[]{-CTBConstants.catcher_speed, 0, +CTBConstants.catcher_speed};
        //num_actions = 3;
        // Initialise visualisation frame.
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
    public Results step(final int action_index) {
        //state.update(actions[action_index]);
        panel.repaint();
        return new Results(reward(), state(), terminal());
    }

    @Override
    public Results initial(){return new Results(reward(), state(), terminal());}

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
    public Game restart() {
        frame.setVisible(false);
        frame.dispose();
        return new CTB();
    }

    @Override
    public void activateVisualisation(){
        //nothing,...
    }

    @Override
    public int numStates() {
        return (int) Math.pow(2, 10) * 7;
    }

    @Override
    public int numActions() {
        return 40;
    }

    @Override
    public Results virtual_move(int[] own_state, int action_index) {
        return new Results(reward(), state(), terminal());
    }

    @Override
    public double[] features(Results virtual_state_res) {
        return new double[]{0};
    }

    @Override
    public int numfeatures() {
        return 0;
    }

    @Override
    public boolean checkAction(final int action_index){return false;}

    @Override
    public int[][] actions(){return new int[1][1];}

    @Override
    public int toScalarState(final int[] state){return 0;}

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
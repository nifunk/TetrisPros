package tests;

import java.util.Random;

public class CTBState {

    public Position ball_pos  = new Position(0, 0);
    private Velocity ball_vel = new Velocity(0, CTBConstants.ball_speed);

    public Position catcher_pos  = new Position(0,CTBConstants.window_height - 50);
    private Velocity catcher_vel = new Velocity(0, 0);

    public CTBState() {
        final Random generator = new Random();
        ball_pos.x = generator.nextInt(CTBConstants.window_width - 2*CTBConstants.ball_radius);
        ball_pos.x+= CTBConstants.ball_radius;
        catcher_pos.x = generator.nextInt(CTBConstants.window_width);
        catcher_pos.x = Math.max(0, catcher_pos.x);
        catcher_pos.x = Math.min(catcher_pos.x, CTBConstants.window_width);
    }

    public void update(final int action) {
        ball_pos.update(ball_vel);
        catcher_vel.dx = action;
        catcher_pos.update(catcher_vel);
    }

    public class Position {

        public int x;
        public int y;

        private Position(int pos_x, int pos_y) {
            x = pos_x; y = pos_y;
        }

        private void update(final Velocity vel) {
            x += vel.dx; y += vel.dy;
        }

        public double distance(final Position other) {
            final double dis_sq_x = Math.pow(x - other.x, 2);
            final double dis_sq_y = Math.pow(y - other.y, 2);
            return Math.sqrt(dis_sq_x + dis_sq_y);
        }
    }

    public class Velocity {

        public int dx;
        public int dy;

        private Velocity(int vel_x, int vel_y) {
            dx = vel_x; dy = vel_y;
        }
    }
}
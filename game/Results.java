package game;

public class Results {

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
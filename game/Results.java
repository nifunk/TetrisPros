package game;

public class Results {

    public double reward;
    public int[] state;
    public boolean terminated;

    public Results(final int rew, final int[] sta, final boolean term) {
        reward = rew; state = sta; terminated = term;
    }
}
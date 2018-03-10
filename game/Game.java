package game;

public abstract class Game {

    static public int num_states  = 0;
    static public int[] actions   = {0, 0};
    static public int num_actions = 0;

    abstract public Results step(final int action_index);

    abstract protected boolean terminal();

    abstract protected double reward();

    abstract protected int[] state();

    abstract public Game restart();
}

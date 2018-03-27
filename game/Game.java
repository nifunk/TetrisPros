package game;

public abstract class Game {

    // Results of a (random) initial game state.
    abstract public Results initial();

    // Execute an given action in game and return its
    // reward, the following state and if the game has terminated.
    // @param[in]   action      1D-representation of action.
    abstract public Results step(final int action_index);

    // Restart game, i.e. closing old and starting new game.
    abstract public Game restart();

    // Terminal test of game, i.e. boolean if game has terminated yet.
    abstract protected boolean terminal();

    // Reward function, i.e. scalar value evaluating the last action
    // in dependence of current (internal) state.
    abstract protected double reward();

    // Internal state, i.e. environment describing integer array.
    abstract public int[] state();
    abstract public int toScalarState(final int[] state);

    // Definition of game properties.
    abstract public int[][] actions();

    abstract public boolean checkAction(final int action_index);
    abstract public int numStates();
    abstract public int numActions();

    // for genetic algorithm
    abstract public Results virtual_move(int[] own_state, int action_index);
    abstract public double[] features (Results virtual_state_res);
    abstract public int numFeatures();
  
    // Activate visualisation (as initially deactivated).
    abstract public void activateVisualisation();

}

import learning.Tetris_Q;
import learning.QLearning;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {
        Tetris_Q tetris = new Tetris_Q();
		QLearning agent = new QLearning(tetris);
        // Train agent, i.e. adapt q matrix with experience.
        agent.q_matrix.loadMatrix("../learning/q_matrix/tetris.txt");
		agent.adapt(1000000);
		agent.q_matrix.storeMatrix("../learning/q_matrix/tetris.txt");
		// Perform as demonstration of results.
        agent.game.restart();
        agent.game.activateVisualisation();
        agent.perform();
		System.exit(0);
	}
	
}

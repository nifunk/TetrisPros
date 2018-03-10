import learning.Tetris_Q;
import learning.QLearning;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {
		QLearning agent = new QLearning(new Tetris_Q());
		agent.adapt(10000);
		System.exit(0);
	}
	
}

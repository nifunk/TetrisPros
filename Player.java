import genetic.Gen_Agent;
import genetic.Tetris_gen;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {
		//For Q-learning:
		//QLearning agent = new QLearning(new Tetris_Q()); //for q-learning
		//agent.adapt(10000);

		//for the genetic algorithm:
		Gen_Agent agent = new Gen_Agent(new Tetris_gen());
		//let the player act
		//agent.perform();
		//let the player learn
		agent.do_genetic_learning();
		System.exit(0);
	}
	
}

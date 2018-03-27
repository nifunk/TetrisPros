import genetic.Gen_Agent;
import genetic.Tetris_gen;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {
    //For Q-learning:
    //Tetris_Q tetris = new Tetris_Q();
		//QLearning agent = new QLearning(tetris);
    // Train agent, i.e. adapt q matrix with experience.
    //agent.q_matrix.loadMatrix("../learning/q_matrix/tetris.txt");
		//agent.adapt(1000000);
		//agent.q_matrix.storeMatrix("../learning/q_matrix/tetris.txt");
		// Perform as demonstration of results.
    //agent.game.restart();
    //agent.game.activateVisualisation();
    //agent.perform();

		//For Genetic algorithm:
		Gen_Agent agent = new Gen_Agent(new Tetris_gen());
		//let the player act
		//System.out.println("Simple agent performance was launched...");
		//agent.perform();
		//let the player learn
		System.out.println("Genetic learning was launched...");
		agent.do_genetic_learning();
    
		System.exit(0);
	}
	
}

import game.TetrisInterface;
import genetic.Gen_Agent;
import qlearning.QAgent;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {
    	////For Q-qlearning:
		QAgent agent = new QAgent(new TetrisInterface());
		// Train encoder and agent.
        agent.adapt();
		// Perform as demonstration of results.
        agent.getGame().activateVisualisation();
    	agent.perform();

		//For Genetic algorithm:
		//Gen_Agent agent = new Gen_Agent(new TetrisInterface());
		//let the player act
		//System.out.println("Simple agent performance was launched...");
		//agent.getGame().activateVisualisation();
		//agent.perform();
		//let the player learn
		//System.out.println("Genetic qlearning was launched...");
		//agent.do_genetic_learning();
    
		System.exit(0);
	}
	
}

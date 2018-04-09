import game.TetrisInterface;
import genetic.Gen_Agent;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {
    	////For Q-qlearning:
		//QAgent agent = new QAgent(new TetrisInterface());
		//// Train encoder and agent.
        //agent.adapt();
		//// Perform as demonstration of results.
        ////agent.getGame().activateVisualisation();
    	//agent.perform();

		//For Genetic algorithm: -> HAND CRAFTED FEATURES:
		Gen_Agent agent = new Gen_Agent(new TetrisInterface());
		//agent.loadMatrix("best_4_features.txt");
		agent.loadMatrix("11_feat_paper.txt");
		//let the player act
		System.out.println("Simple agent performance was launched...");
		//agent.getGame().activateVisualisation();
		agent.perform();

		//let the player learn
		//System.out.println("Genetic qlearning was launched...");
		//agent.do_genetic_learning();




		// TRAIN AUTO ENCODER
		//Gen_Agent agent = new Gen_Agent(new TetrisInterface(), "test_tetris.eg", 200000);
		// DO NOT TRAIN AUTOENCODER
		//Gen_Agent agent = new Gen_Agent(new TetrisInterface(), "test_tetris.eg");
		// Let the player learn.
		//agent.do_genetic_learning();
		// Let the player act.
		//agent.loadMatrix("ctb_enc.txt");
		//agent.getGame().activateVisualisation();
		//agent.perform();

    
		System.exit(0);
	}
	
}

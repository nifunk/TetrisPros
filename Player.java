import game.TetrisInterface;
import genetic.Gen_Agent;


public class Player {
	private static int num_generations, population_size, child_heuristic;
	private static double fraction, prop_mutation, fraction_direct_pass;

	public static void main(String[] args) {
		//Arguments to pass for running
		if(args.length == 6)
		{
			num_generations = Integer.parseInt(args[0]);
			population_size = Integer.parseInt(args[1]);
			child_heuristic = Integer.parseInt(args[2]); 
			fraction = Double.parseDouble(args[3]);
	        prop_mutation= Double.parseDouble(args[4]);
	        fraction_direct_pass = Double.parseDouble(args[5]);
		}
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
		boolean want_to_train = true;

		if(!want_to_train){
			//SIMPLY PLAY
			//agent.loadMatrix("best_4_features.txt");
			//FROM PAPER:
			//agent.loadMatrix("11_feat_paper.txt");
			//BEST OWN TRAINED:
			agent.loadMatrix("11_feat_ourbest_1.txt");
			////let the player act
			System.out.println("Simple agent performance was launched...");
			////agent.getGame().activateVisualisation();
			agent.perform();

		}
		else{
			//let the player learn
			System.out.println("Genetic qlearning was launched...");
			agent.do_genetic_learning(num_generations, population_size, child_heuristic, fraction, prop_mutation, fraction_direct_pass);
		}

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

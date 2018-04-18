import game.ManuelFeatures;
import game.TetrisInterface;
import genetic.Gen_Agent;

public class Player {

	public static void main(String[] args) {
		new Player();
	}

	private Player() {

		//For Genetic algorithm: -> HAND CRAFTED FEATURES:
        ManuelFeatures encoder = new ManuelFeatures();
		Gen_Agent agent = new Gen_Agent(new TetrisInterface(), encoder);
		boolean want_to_train = false;

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
			agent.do_genetic_learning();
		}

		System.exit(0);
	}
	
}

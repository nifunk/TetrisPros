import game.TetrisInterface;
import genetic.Gen_Agent;


public class Player {
	private static int num_generations, population_size, child_heuristic;
	private static double fraction, prop_mutation, fraction_direct_pass;
	private static boolean want_to_train;

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
	        want_to_train = true;
		}
		else {want_to_train = false;}
		new Player();
	}

	private Player() {

		TetrisInterface ti = new TetrisInterface();
		Gen_Agent agent = new Gen_Agent(ti);

		if(!want_to_train)
		{
            System.out.println("Simple agent performance was launched...");
			agent.loadMatrix("weights.txt");
            double[] wts = agent.get_weights();
			Gen_Agent.Performer performer = agent.new Performer(wts);
			performer.activateVisualisation();
            performer.run();
            final int cleared_rows = performer.getVal();
            System.out.printf("Number of cleared rows = %d", cleared_rows);

		}
		else
		{
			System.out.println("Genetic learning was launched...");
			agent.do_genetic_learning(num_generations,
                                      population_size,
                                      child_heuristic,
                                      fraction,
                                      prop_mutation,
                                      fraction_direct_pass);
		}
    
		System.exit(0);
	}
	
}

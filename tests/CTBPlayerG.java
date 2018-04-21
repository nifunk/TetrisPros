package tests;

import genetic.Gen_Agent;

public class CTBPlayerG {
    private static int num_generations, population_size, child_heuristic;
    private static double fraction, prop_mutation, fraction_direct_pass;

    public static void main(String[] args)
    {
        new CTBPlayerG();
    }

    private CTBPlayerG()
    {
        // Train auto encoder.
        Gen_Agent agent = new Gen_Agent(new CTB());
        // Let the player learn.
        num_generations = 1;
        population_size = 5;
        child_heuristic = 0;
        fraction = 0.5;
        prop_mutation= 0.5;
        fraction_direct_pass = 0.1;
        agent.do_genetic_learning(num_generations, population_size, child_heuristic, fraction, prop_mutation, fraction_direct_pass);
        // Let the player act.
        agent.loadMatrix("CTB.txt");
        ////let the player act
        double[] wts = agent.get_weights();
        System.out.println("Simple CTB agent performance was launched...");
        ////agent.getGame().activateVisualisation();
        Gen_Agent.Performer performer = agent.new Performer(wts);
        performer.run();
        performer.getVal();

        System.exit(0);
    }
}

package tests;

import genetic.Gen_Agent;
import qlearning.QAgent;

public class CTBPlayer{

    public static void main(String[] args)
    {
        new CTBPlayer();
    }

    private CTBPlayer()
    {
        //QAgent agent = new QAgent(new CTB());
        //// Train encoder and agent.
        //agent.adapt();
        //agent.store("qlearning/q_matrix/ctb.txt",
        //            "qlearning/encoder/ctb.eg");
        //// Perform as demonstration of results.
        //agent.getGame().activateVisualisation();
        //agent.perform();



        //For Genetic algorithm:
        Gen_Agent agent = new Gen_Agent(new CTB());
        //let the player act
        System.out.println("Simple agent performance was launched...");
        agent.getGame().activateVisualisation();
        agent.perform();
        //let the player learn
        //System.out.println("Genetic qlearning was launched...");
        //agent.do_genetic_learning();


        System.exit(0);
    }
}

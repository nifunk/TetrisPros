package tests;

import qlearning.QAgent;

public class CTBPlayer{

    public static void main(String[] args)
    {
        new CTBPlayer();
    }

    private CTBPlayer()
    {
        QAgent agent = new QAgent(new CTB());
        // Train encoder and agent.
        agent.adapt();
        agent.store("qlearning/q_matrix/ctb.txt",
                    "qlearning/encoder/ctb.eg");
        // Perform as demonstration of results.
        agent.getGame().activateVisualisation();
        agent.perform();
        System.exit(0);
    }
}

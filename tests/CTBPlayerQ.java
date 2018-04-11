package tests;

import qlearning.QAgent;

public class CTBPlayerQ {

    public static void main(String[] args)
    {
        new CTBPlayerQ();
    }

    private CTBPlayerQ()
    {
        QAgent agent = new QAgent(new CTB());
        // Train encoder and agent.
        agent.adapt();
        agent.store("resources/q_matrix/ctb.txt",
                    "resources/encoder/ctb.eg");
        // Perform as demonstration of results.
        agent.getGame().activateVisualisation();
        agent.perform();

        System.exit(0);
    }
}

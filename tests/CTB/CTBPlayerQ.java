package tests.CTB;

import qlearning.QAgent;

public class CTBPlayerQ {

    public static void main(String[] args)
    {
        new CTBPlayerQ();
    }

    private CTBPlayerQ()
    {
        // Init encoder and agent.
        ManuelFeaturesCTB encoder = new ManuelFeaturesCTB();
        QAgent agent = new QAgent(new CTB(), encoder);
        // Train encoder and agent.
        agent.adapt();
        // Perform as demonstration of results.
        agent.getGame().activateVisualisation();
        agent.perform();

        System.exit(0);
    }
}

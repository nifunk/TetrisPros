package tests.SmallTetris;

import autoencoder.BinaryFlattening;
import autoencoder.Encoder;
import qlearning.QAgent;

public class TetrisSmallPlayer
{

    public static void main(String[] args)
    {
        new TetrisSmallPlayer();
    }

    private TetrisSmallPlayer()
    {
        // Init encoder and agent.
        Encoder encoder = new BinaryFlattening();
        encoder.build(1, -1);
        QAgent agent = new QAgent(new TetrisInterfaceSmall(), encoder);
        // Train encoder and agent.
        agent.adapt();
        // Perform as demonstration of results.
        agent.getGame().activateVisualisation();
        agent.perform();

        System.exit(0);
    }
}

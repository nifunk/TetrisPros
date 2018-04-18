package tests.SmallTetris;

import autoencoder.BinaryFlattening;
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
        BinaryFlattening encoder = new BinaryFlattening();
        QAgent agent = new QAgent(new TetrisInterfaceSmall(), encoder);
        // Train encoder and agent.
        agent.adapt();
        agent.store("resources/qlearning/tetris_small.txt");
        // Perform as demonstration of results.
        agent.getGame().activateVisualisation();
        agent.perform();

        System.exit(0);
    }
}

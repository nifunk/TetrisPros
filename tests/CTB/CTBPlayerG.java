package tests.CTB;

import autoencoder.Encoder;
import autoencoder.NeuralEncoder;
import game.Game;
import genetic.Gen_Agent;

public class CTBPlayerG {

    public static void main(String[] args)
    {
        new CTBPlayerG();
    }

    private CTBPlayerG()
    {
        // Init encoder, game and agent.
        Game game = new CTB();
        Encoder encoder = new NeuralEncoder();
        encoder.build(20, game.numStates(),
                      game.trainingStates(1000), "resources/encoder/ctb.txt");
        Gen_Agent agent = new Gen_Agent(game, encoder);
        // Let the player learn.
        agent.do_genetic_learning();
        // Let the player act.
        //agent.loadMatrix("ctb_enc.txt");
        agent.getGame().activateVisualisation();
        agent.perform();

        System.exit(0);
    }
}

package tests.CTB;

import genetic.Gen_Agent;

public class CTBPlayerG {

    public static void main(String[] args)
    {
        new CTBPlayerG();
    }

    private CTBPlayerG()
    {
        // Train auto encoder.
        Gen_Agent agent = new Gen_Agent(new CTB(), "test.eg", 200000);
        // Let the player learn.
        agent.do_genetic_learning();
        // Let the player act.
        //agent.loadMatrix("ctb_enc.txt");
        agent.getGame().activateVisualisation();
        agent.perform();

        System.exit(0);
    }
}

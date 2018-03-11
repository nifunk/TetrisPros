import learning.QLearning;
import tests.CTB;

public class CTBPlayer{

    public static void main(String[] args) {
        new CTBPlayer();
    }

    private CTBPlayer() {
        QLearning agent = new QLearning(new CTB());
        agent.adapt(100000);
        agent.q_matrix.storeMatrix("../learning/q_matrix/ctb.txt");
        // Perform as demonstration of results.
        agent.game.restart();
        agent.game.activateVisualisation();
        agent.perform();
        System.exit(0);
    }

}

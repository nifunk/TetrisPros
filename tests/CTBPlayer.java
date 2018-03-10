import learning.QLearning;

import tests.CTB;

public class CTBPlayer{

    public static void main(String[] args) {
        new CTBPlayer();
    }

    private CTBPlayer() {
        QLearning agent = new QLearning(new CTB());
        agent.adapt(10000);
        System.exit(0);
    }

}

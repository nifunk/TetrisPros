package genetic;

import game.Game;
import game.Results;

import java.util.Arrays;
import java.util.Random;

public class Gen_Agent {

    private Game game;

    public Gen_Agent(Game game) {
        this.game = game;
    }

    public int perform() {
        //just the function which really does the performance!
        //have a feature weight vector!
        //init reward!
        double total_reward = 0;
        //STEP1: get all actions
        int all_actions = game.numActions();
        int num_features = game.numfeatures();
        //feature vector: dim1=action idx; dim2= features to this index
        double[] weights = new double[]{-0.51,0.76,-0.3566,-0.18448};
        // Important: have to take measure such that we dont choose illegal move!!

        //Need this?! - doubt it
        Results results  = new Results(0, new int[]{0}, false);

        while (! results.terminated) {
            double[] score = new double[all_actions]; //to eval the moves
            //the higher the score the better so preinit with -10000 so that no illegal moves taken
            Arrays.fill(score, -100000.0);
        //STEP3: if valid action - play perform the actions virtually and compute the features
            for (int move=0; move<all_actions; move++){
                if (game.checkAction(move)){
                    Results outcome = game.virtual_move(game.state(),move);
                    if (outcome.terminated!=true){ //this means game is not over in this drive!
                        //calculate all features!!!
                        double[] features = game.features(outcome);
                        double score_ = 0;
                        //calculate the score
                        for (int k=0; k<num_features;k++){
                            score_ = score_ + weights[k]*features[k];
                        }
                        score[move] = score_;
                    }

                }
            }

        //Step5: choose best move
            double best_score=score[0];
            int best_move=0;
            for(int i=1;i<all_actions;i++){
                if (score[i]>best_score){
                    best_score = score[i];
                    best_move = i;
                }
            }
        //Step6: execute this best move
            results = game.step(best_move);
        //Step7: save reward such that You know how succesfull these weights were!!
            total_reward = results.reward;
        }
        System.out.println("You have completed "+total_reward+" rows.");
        return (int)total_reward;
    }

    public void adapt(final int iterations) {
        // Initialise learning rates as decreasing with time
        // for better adaption.
    }


}

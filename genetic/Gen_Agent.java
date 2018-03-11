package genetic;

import game.Game;
import game.Results;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Gen_Agent {

    private Game game;
    private double[] weights;

    public Gen_Agent(Game game) {
        this.game = game;
        //this.weights = new double[]{-0.51,0.76,-0.3566,-0.18448}; //internet weights
        this.weights = new double[]{-3.459111196332234, 8.798745927744655, -17.509339945947517, -4.244590461223442}; //best trained so far!
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
        double[] weights_ = this.weights;
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
        //System.out.println("You have completed "+total_reward+" rows.");
        game = game.restart();
        return (int)total_reward;
    }

    public double[] do_genetic_learning(){
        //STEP1: make a first random population
        //general assumption: feature 0,2,3 must be penalized
        //feature 1 must be pushed -> positive

        int size_init_population = 1000;
        int num_repetitions = 5;
        Random r = new Random(); //random num generator between 0 and 1;
        double[][]init_population = new double[size_init_population][game.numfeatures()+1]; //1000 init weights,... store weights and score
        double[]weights_lowerbound = new double[]{-25,0,-25,-25};
        double[]weights_upperbound = new double[]{0,25,0,0};
        for (int i=0;i<size_init_population;i++){
            for (int j=0; j<game.numfeatures();j++){
                double random = r.nextDouble();
                init_population[i][j]=weights_lowerbound[j]+random*(weights_upperbound[j]-weights_lowerbound[j]);
            }
        }
        System.out.println("Initial popukation generated....");
        //play with all the 1000 combinations and store the highest
        for (int i=0;i<size_init_population;i++) {
            //set weights in this iteration
            for (int j = 0; j < game.numfeatures(); j++) {
                this.weights[j] = init_population[i][j];
            }
            //play num_repetition times
            double[] store_score = new double[num_repetitions];
            for (int j = 0; j < num_repetitions; j++) {
                store_score[j] = this.perform();
            }
            //store the best value!
            double score_best = store_score[0];
            for (int j = 1; j < num_repetitions; j++) {
                if (store_score[j] > score_best) {
                    score_best = store_score[j];
                }
            }
            init_population[i][game.numfeatures()] = score_best;
        }
        //Sort descending!!
        sortbyColumn(init_population,game.numfeatures());
        System.out.println("Initial popukation succesfull evaluated");

        //STEP2: choose the best ones -> selection
        int tokeep = 50; //must be smaller than init population!!!
        int num_new_generated = 400; //MUST be able to divide by 2!!!
        double[][]selected_population = new double[num_new_generated][game.numfeatures()+1];
        double prop_mutation = 0.1;

        //STEP3: crossover
        for (int i=0;i<num_new_generated;i=i+2) {
            //determine which ones to cross
            double random = r.nextDouble();
            int weightset1 = (int) (random * tokeep);
            random = r.nextDouble();
            int weightset2 = (int) (random * tokeep);
            //determine crossover point which is here!!
            random = r.nextDouble();
            int crossover = (int) (1 + random * (game.numfeatures() - 2));
            for (int k = 0; k < game.numfeatures(); k++) {
                if (k >= crossover) {
                    selected_population[i][k] = init_population[weightset1][k];
                    selected_population[i + 1][k] = init_population[weightset2][k];
                } else {
                    selected_population[i][k] = init_population[weightset2][k];
                    selected_population[i + 1][k] = init_population[weightset2][k];
                }

            }

            //possible mutation:
            random = r.nextDouble();
            if (random < prop_mutation) {
                //determine point of mutation:
                random = r.nextDouble();
                int pos_mutation = (int) (random * (game.numfeatures() - 1));
                //new value
                random = r.nextDouble();
                selected_population[i][pos_mutation] = weights_lowerbound[pos_mutation] + random * (weights_upperbound[pos_mutation] - weights_lowerbound[pos_mutation]);
            }

            //possible mutation:
            random = r.nextDouble();
            if (random < prop_mutation) {
                //determine point of mutation:
                random = r.nextDouble();
                int pos_mutation = (int) (random * (game.numfeatures() - 1));
                //new value
                random = r.nextDouble();
                selected_population[i + 1][pos_mutation] = weights_lowerbound[pos_mutation] + random * (weights_upperbound[pos_mutation] - weights_lowerbound[pos_mutation]);
            }
        }
        System.out.println("Selected population created....");
        //play with all the combinations and store the highest
        for (int i=0;i<num_new_generated;i++) {
            //set weights in this iteration
            for (int j = 0; j < game.numfeatures(); j++) {
                this.weights[j] = selected_population[i][j];
            }
            //play num_repetition times
            double[] store_score = new double[num_repetitions];
            for (int j = 0; j < num_repetitions; j++) {
                store_score[j] = this.perform();
            }
            //store the best value!
            double score_best = store_score[0];
            for (int j = 1; j < num_repetitions; j++) {
                if (store_score[j] > score_best) {
                    score_best = store_score[j];
                }
            }
            selected_population[i][game.numfeatures()] = score_best;
        }
        //Sort descending!!
        sortbyColumn(selected_population,game.numfeatures());
        System.out.println("Selected population succesfully evaluated!");


        //TODO: maybe fuse selected_population and init_population to get the really best!!

        System.out.println("You have completed "+selected_population[0][game.numfeatures()]+" rows.");
        //BEST WEIGHTS:
        System.out.println(Arrays.toString(selected_population[0]));
    return new double[]{1};

    }
    public void adapt(final int iterations) {
        // Initialise learning rates as decreasing with time
        // for better adaption.
    }

    public static void sortbyColumn(double arr[][], int col)
    {
        // Using built-in sort function Arrays.sort
        //sorting descending!!!
        Arrays.sort(arr, new Comparator<double[]>() {

            @Override
            // Compare values according to columns
            public int compare(final double[] entry1,
                               final double[] entry2) {

                // To sort in descending order revert
                // the '>' Operator
                if (entry1[col] > entry2[col])
                    return -1;
                else
                    return 1;
            }
        });  // End of function call sort().
    }

}

package genetic;

import game.Game;
import game.Results;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
        int num_features = game.numFeatures();
        //feature vector: dim1=action idx; dim2= features to this index
        double[] weights_ = this.weights;
        // Important: have to take measure such that we dont choose illegal move!!

        //Need this?! - doubt it
        Results results  = new Results(0, new int[]{0}, false);

        while (! results.terminated) {
            double[] score = new double[all_actions]; //to eval the moves
            //the higher positive!! the score the better so preinit with -10000 so that no illegal moves taken
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

        int size_init_population = 500; //was 1000
        int num_repetitions = 10;
        double[][]init_population = new double[size_init_population][game.numFeatures()+1]; //1000 init weights,... store weights and score
        double[]weights_lowerbound = new double[]{-40,0,-40,-40};
        double[]weights_upperbound = new double[]{0,40,0,0};

        //generate initial population
        for (int i=0;i<size_init_population;i++){
            for (int j = 0; j<game.numFeatures(); j++){
                init_population[i][j]= getRandom(weights_lowerbound[j],weights_upperbound[j]);
            }
        }
        System.out.println("Initial population generated....");
        //play with all the 1000 combinations and store the highest
        init_population = evalPopulation(init_population,num_repetitions);

        System.out.println("Initial population succesfully evaluated");

        //STEP2: choose the best ones -> selection
        double tokeep = 0.05; //percentage of initial population you want to keep, must be smaller than 1!!!
        double size_new_pop = 0.3; //size of new population relative to initial population (should be smaller than 0.5)
        //reason: is twice this percentage later cause cross over is mutual -> real percentace = 2*size_new_pop
        double prop_mutation = 0.1;

        //STEP3: crossover and mutation
        double[][] selected_population = doCrossingandMutation(init_population,tokeep,size_new_pop,prop_mutation,weights_lowerbound,weights_upperbound);
        System.out.println("Selected population created....");

        //play with all the combinations and store the highest
        selected_population = evalPopulation(selected_population,num_repetitions);

        System.out.println("Selected population succesfully evaluated!");


        //Fuse selected_population and init_population to get the really best!!
        int entries = 10; //store the 10 best overall!!!
        double [][]final_result = fuseMatrix(init_population,selected_population,entries);
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
        storeMatrix(fileName,final_result);

        System.out.println("You have completed "+final_result[0][game.numFeatures()]+" rows.");
        //BEST WEIGHTS:
        System.out.println(Arrays.toString(final_result[0]));
    return new double[]{1};

    }


    public void adapt(final int iterations) {
        // Initialise learning rates as decreasing with time
        // for better adaption.
    }

    //function which allows to sort 2D array by one column in descending order!
    public static void sortbyColumn(double arr[][], final int col)
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

    //function which returns a random number inside the interval [lower_bound,upper_bound]
    private double getRandom(double lower_bound,double upper_bound){
        Random r = new Random();
        double random = r.nextDouble();
        return (lower_bound+random*(upper_bound-lower_bound));
    }

    //given a population, execute the game with it and store the results in the resulting array!
    //return: in descending order sorted array!
    private double[][] evalPopulation(double[][] population, int num_repetitions){
        int size_population = population.length;
        for (int i=0;i<size_population;i++) {
            //set weights in this iteration
            for (int j = 0; j < game.numFeatures(); j++) {
                this.weights[j] = population[i][j];
            }
            //play num_repetition times
            double[] store_score = new double[num_repetitions];
            for (int j = 0; j < num_repetitions; j++) {
                store_score[j] = this.perform();
            }

            double score_best = store_score[0];

            //HERE: store BEST SCORE VALUE!
            //for (int j = 1; j < num_repetitions; j++) {
            //    if (store_score[j] > score_best) {
            //        score_best = store_score[j];
            //    }
            //}

            //OR: store the MEAN of all scores:
            for (int j = 1; j < num_repetitions; j++) {
                score_best = score_best + store_score[j];
            }
            score_best = score_best/num_repetitions;


            population[i][game.numFeatures()] = score_best;
        }
        //Sort descending by the score!
        sortbyColumn(population,game.numFeatures());
        return population;
    }

    //expects: population that we want to mutate and cross which is ordered descendingly!!!
    //to_keep = percentage of population we want to use for crossover
    //size_new: how big should new population be in percent of initial!!!
    //prop_mutation = propability for a possible mutation
    //weights intervals needed for the possible mutation!
    //returns: new crossed and mutated array!!
    private double[][] doCrossingandMutation(double[][]input_population,double to_keep,double size_new, double prop_mutation, double[] weigths_lower, double[] weights_upper){
        int size_input = input_population.length;
        int num_new_generated = 2*(int) Math.round(size_input*size_new); //since crossing over is mutual*2
        int last_idx = (int) Math.round(size_input*to_keep); //last index of input array we consider!!!
        double[][]new_population = new double[num_new_generated][game.numFeatures()+1];
        for (int i=0;i<num_new_generated;i=i+2) {
            //determine which ones to cross
            int candidate1 = (int) Math.round(getRandom(0,last_idx)); //round is essential since (int)9.8=9!!!
            int candidate2 = (int) Math.round(getRandom(0,last_idx));
            //determine crossover point
            int crossover = (int) Math.round(getRandom(1,game.numFeatures()-1));
            for (int k = 0; k < game.numFeatures(); k++) {
                if (k >= crossover) {
                    new_population[i][k] = input_population[candidate1][k];
                    new_population[i + 1][k] = input_population[candidate2][k];
                } else {
                    new_population[i][k] = input_population[candidate2][k];
                    new_population[i + 1][k] = input_population[candidate1][k];
                }

            }

            //possible mutation:
            if (getRandom(0,1) < prop_mutation) {
                //determine point of mutation:
                int pos_mutation = (int) Math.round(getRandom(0,game.numFeatures()-1));
                //new value
                new_population[i][pos_mutation] = getRandom(weigths_lower[pos_mutation],weights_upper[pos_mutation]);
            }

            //possible mutation:
            if (getRandom(0,1) < prop_mutation) {
                //determine point of mutation:
                int pos_mutation = (int) Math.round(getRandom(0,game.numFeatures()-1));
                //new value
                new_population[i+1][pos_mutation] = getRandom(weigths_lower[pos_mutation],weights_upper[pos_mutation]);
            }
        }
        return new_population;
    }

    //this method fuses two matrices to get the best results!!!
    //num_entries == number entries the final matrix should have!!
    public double[][]fuseMatrix(double[][]matrix1,double[][]matrix2,int num_entries){
        int lenght1 = Math.min(matrix1.length,num_entries);
        int length2 = Math.min(matrix2.length,num_entries);
        double[][] final_arr = new double[(lenght1+length2)][game.numFeatures()+1];
        for (int i=0; i<lenght1;i++){
            for (int j = 0; j<game.numFeatures()+1; j++){
                final_arr [i][j]=matrix1[i][j];
            }
        }
        for (int i=0; i<length2;i++){
            for (int j = 0; j<game.numFeatures()+1; j++){
                final_arr [i+lenght1][j]=matrix2[i][j];
            }
        }
        sortbyColumn(final_arr,game.numFeatures());
        //shrink to desired size!!
        int des_size = Math.max(lenght1,length2); //only that there are no access errors!!!
        double[][]return_arr = new double[des_size][game.numFeatures()+1];
        for (int i=0; i<des_size;i++){
            for (int j = 0; j<game.numFeatures()+1; j++){
                return_arr[i][j]=final_arr[i][j];
            }
        }
        return return_arr;
    }


    public void storeMatrix(final String filename, double[][]matrix) {
        try {
            final FileWriter fw = new FileWriter(filename);
            fw.write(getCurrentTimeStamp());
            fw.write("\n");
            for (double[] action_rewards : matrix) {
                for (double reward : action_rewards) {
                    fw.write(reward + ",");
                }
                fw.write("\n");
            }
            fw.close();
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println("Stored Best Results in " + filename);
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

}

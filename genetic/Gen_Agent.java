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
            //find first valid move
            for(int i=0;i<all_actions;i++){
                if (game.checkAction(i)){
                    best_score = score[i];
                    best_move = i;
                    break;
                }
            }

            for(int i=(best_move+1);i<all_actions;i++){
                if ((score[i]>best_score)&game.checkAction(i)){
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

        int size_init_population = 50; //was 500
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
        // double tokeep = 0.05; //percentage of initial population you want to keep, must be smaller than 1!!!
        // double size_new_pop = 0.3; //size of new population relative to initial population (should be smaller than 0.5)
        //reason: is twice this percentage later cause cross over is mutual -> real percentace = 2*size_new_pop

        int numGenerations = 10; //100
        double fraction = 0.25;
        int child_heuristic = 0; 
        double prop_mutation= 0.05;
        double fraction_direct_pass = 0.3;

        /*TODO: new function that will use doCrossingMutation and will generate the new generation from the old one in as follows:
            - take 25-30% of the best performing parents as children
            - the rest 70-75% will be generated by crossing and mutation: 
                - for 1 of the parents we choose randomly 30% from the parents and choose the best performing from the subset
                - do same for the second parent
                - pick one or pick avg for generating child weights (implement both and compare!)
                - Mutation: For each member and each weight, check if mutation occurs and if it does then multiply weight with uniform -1.5 and 1.5
                - Fitness function check: check if the child performs as least as good as the worst child so far. If so, remove the child.
                Set a limit of kicked out childs and take the best removed one as a child.*/ 
        

        for (int i = 0; i < numGenerations ; i++) {
             //TODO: train over multiple generations 
            //STEP3: crossover and mutation
            //double[][] selected_population = doCrossingandMutation(init_population,tokeep,size_new_pop,prop_mutation,weights_lowerbound,weights_upperbound);
            
            init_population = doCrossingandMutation(init_population, num_repetitions, fraction, child_heuristic, prop_mutation, fraction_direct_pass);
            System.out.println( (i+1) + " Selected population created....");

        }
       


        //Fuse selected_population and init_population to get the really best!!
        //int entries = 10; //store the 10 best overall!!!
        //double [][]final_result = fuseMatrix(init_population,selected_population,entries);
        init_population = evalPopulation(init_population, num_repetitions);

        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
        storeMatrix(fileName, init_population);

        System.out.println("You have completed "+init_population[0][game.numFeatures()]+" rows.");
        //BEST WEIGHTS:
        System.out.println(Arrays.toString(init_population[0]));
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


    int pickParent(double[][]input_population, double fraction){
        int max_idx = (int) Math.floor(input_population.length*fraction);
        int max_score_idx = 0;
        for (int i = 0; i < max_idx; i++){
            int r = (int) Math.floor(getRandom(0, input_population.length));
            if (input_population[max_score_idx][game.numFeatures()+1] < input_population[r][game.numFeatures()+1]){
                max_score_idx = r; 
            }
        }
        return max_score_idx;
    }


    //expects: population that we want to mutate and cross which is ordered descendingly!!!
    //to_keep = percentage of population we want to use for crossover
    //size_new: how big should new population be in percent of initial!!!
    //prop_mutation = propability for a possible mutation
    //weights intervals needed for the possible mutation!
    //returns: new crossed and mutated array!!
    private double[][] doCrossingandMutation(double[][] input_population, int num_repetitions, double fraction, int child_heuristic, double prop_mutation, double fraction_direct_pass){
        int size_input = input_population.length;
        int bestOfOld = (int) Math.round(fraction_direct_pass * input_population.length);
        double[][]new_population = new double[input_population.length][game.numFeatures()+1];
        //population with which games were played
        double[][]eval_population = new double[input_population.length][game.numFeatures()+1];

        eval_population = evalPopulation(input_population, num_repetitions);


        //take the best 30% of the old generation (init generation)
        for (int i = 0 ; i < bestOfOld ; i++) {
            for (int j = 0; j < game.numFeatures(); j++) {
                new_population[i][j] = eval_population[i][j];
            }
        }


        for (int i = bestOfOld; i < input_population.length ; i++) {
            int parent1 = pickParent(input_population, fraction);
            int parent2 = pickParent(input_population, fraction);
            double[] child = new double[game.numFeatures()+1];

            //heuristic 1
            if (child_heuristic == 0){
                double rand_val = getRandom(0, 1);
                if (rand_val > 0.5){
                    for(int j = 0; j < game.numFeatures() + 1; j++){
                        child[j] = input_population[parent1][j];
                    }
                }
                else{
                    for(int j = 0; j < game.numFeatures() + 1; j++){
                        child[j] = input_population[parent2][j];
                    }
                }
            }
            //heuristic 2
            else{
                for(int j = 0; j < game.numFeatures() + 1; j++){
                    child[j] = (input_population[parent1][j] + input_population[parent2][j])/2;
                }
            }

            // put child in the new generation
            for(int j = 0; j < game.numFeatures() + 1; j++){
                new_population[i][j] = child[j];
                double mutate_rand_val = getRandom(0, 1);
                if(mutate_rand_val < prop_mutation){
                    new_population[i][j] = new_population[i][j] * getRandom(0, 1.5);
                }
            }

        }

        return new_population;
    }

    //this method fuses two matrices to get the best results!!!
    //num_entries == number entries the final matrix should have!!
    public double[][]fuseMatrix(double[][]matrix1,double[][]matrix2,int num_entries){
        int length1 = Math.min(matrix1.length,num_entries);
        int length2 = Math.min(matrix2.length,num_entries);
        double[][] final_arr = new double[(length1+length2)][game.numFeatures()+1];
        for (int i=0; i<length1;i++){
            for (int j = 0; j<game.numFeatures()+1; j++){
                final_arr [i][j]=matrix1[i][j];
            }
        }
        for (int i=0; i<length2;i++){
            for (int j = 0; j<game.numFeatures()+1; j++){
                final_arr [i+length1][j]=matrix2[i][j];
            }
        }
        sortbyColumn(final_arr,game.numFeatures());
        //shrink to desired size!!
        int des_size = Math.max(length1,length2); //only that there are no access errors!!!
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

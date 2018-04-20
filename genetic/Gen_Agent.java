package genetic;

import game.Game;
import game.Results;

import java.io.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import game.TetrisInterface;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

public class Gen_Agent {

    private Game        game;
    private double[]    weights;
    private boolean     weights_loaded;
    private int[]       perf_scores;

    public Gen_Agent(Game game)
    {
        this.game = game;
        //this.weights = new double[]{-0.51,0.76,-0.3566,-0.18448}; //internet weights
        //this.weights = new double[]{-3.459111196332234, 8.798745927744655, -17.509339945947517, -4.244590461223442}; //best trained so far!
        this.weights = new double[game.numFeatures()]; //only to initialize
        this.weights_loaded = false;
        this.perf_scores = new int[1000]; // globally set everytime a game is played
        // this.games = games;
    }

    public double[] get_weights() {
        return this.weights;
    }

    /*public Gen_Agent(Game game, final String encoder_load_file)
    {
        // Define game.
        this.game = game;
        // Start auto encoder.
        System.out.println("\nGenAgent: Loading auto encoder");
        this.game.encoder.buildNetwork(game.numStates());
        this.game.encoder.load("resources/encoder/"+encoder_load_file);
        // Initialise genetic weights.
        this.weights = new double[game.numFeatures()]; //only to initialize
        this.weights_loaded = false;
    }

    public Gen_Agent(Game game, final String encoder_save_file, final int num_samples)
    {
        // Define game.
        this.game = game;
        // Start auto encoder.
        System.out.println("\nGenAgent: Adapting auto encoder");
        this.game.encoder.buildNetwork(game.numStates());
        this.game.encoder.adapt(game.trainingStates(num_samples));
        this.game.encoder.store("resources/encoder/"+encoder_save_file);
        // Initialise genetic weights.
        this.weights = new double[game.numFeatures()]; //only to initialize
        this.weights_loaded = false;
    }*/

    public class Performer implements Runnable {

        private int retVal; // NOT NEEDED
        private int whichGame;
        private Game game;
        private double[]    weights;
        private boolean     weights_loaded;
        
        public Performer(double[] wts) {
            // Gen_Agent ga = new Gen_Agent(game);
            this.game = new TetrisInterface();
            this.weights = wts;
            this.weights_loaded = true;
    
        }

       
        public void setGame(int gameNum) {
            this.whichGame = gameNum;
        }

        // public int perform() {
        public void run() {
            if(!weights_loaded){throw new java.lang.Error("weights not loaded!");}
            //just the function which really does the performance!
            //have a feature weight vector!
            //init reward!
            double total_reward = 0;
            //STEP1: get all actions
            int all_actions = game.numActions();
            int num_features = game.numFeatures();
            //feature vector: dim1=action idx; dim2= features to this index
            double[] weights_ = weights;
            // Important: have to take measure such that we dont choose illegal move!!

            //Need this?! - doubt it
            Results results  = new Results(0, new int[]{0}, false);

            while (! results.terminated) {
                double[] score = new double[all_actions]; //to eval the moves
                //the higher positive!! the score the better so preinit with -10000 so that no illegal moves taken
                Arrays.fill(score, Double.NEGATIVE_INFINITY);
                //Arrays.fill(score, Double.POSITIVE_INFINITY);
            //STEP3: if valid action - play perform the actions virtually and compute the features
                for (int move=0; move < all_actions; move++){
                    if (game.checkAction(move)){
                        //calculate the features on the initial board configuration:
                        //TODO: here calculate the features on the Ausgangslage!!
                        Results outcome = game.virtual_move(game.state(),move);
                        if (outcome.terminated!=true){ //this means games is not over in this drive!
                            //calculate all features!!!
                            //TODO: replace this function by operator overloading to calculate differential features!!!
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
                double best_score = score[0];
                int best_move = 0;
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
                //TETRIS: NUMBER CLEARED ROWS!
                //CTB: -1* absolute distance between ball and board -> if we search for maximum we find the best!!!
                total_reward = results.reward;
            }
            // System.out.println("You have completed "+total_reward+" rows.");
            // return (int)total_reward;
            this.retVal = (int)total_reward; // NOT NEEDED
            Gen_Agent.this.perf_scores[whichGame] = this.retVal;
            // System.out.println("Setting " + this.retVal + " rows");
            game = game.restart();
            
        }

        // NOT NEEDED
        public int getVal() {
            // System.out.println("Returning " + this.retVal + " rows");
            return this.retVal;
        }
    }

    public double[] do_genetic_learning(int num_generations, int population_size, int child_heuristic, 
        double fraction, double prop_mutation, double fraction_direct_pass){
        System.out.println("Simple agent performance was launched...");
        //STEP1: make a first random population
        //general assumption: feature 0,2,3 must be penalized
        //feature 1 must be pushed -> positive

        int size_init_population = population_size; //was 500
        int num_repetitions = 10;
        double[][]init_population = new double[size_init_population][game.numFeatures()+1]; //1000 init weights,... store weights and score
        double[]weights_lowerbound = new double[game.numFeatures()];
        Arrays.fill(weights_lowerbound, -100000.0);
        double[]weights_upperbound = new double[game.numFeatures()];
        Arrays.fill(weights_upperbound, 100000.0);
        //manual cheating:
        Arrays.fill(weights_upperbound, 0.0);
        weights_lowerbound[3]=0;
        weights_upperbound[3]=100000;

        // generate initial population
        for (int i=0;i<size_init_population;i++){
            for (int j = 0; j<game.numFeatures(); j++){
                //TODO: instead of always starting from a completely random population, add some good individuals from the beginning
                init_population[i][j]= getRandom(weights_lowerbound[j],weights_upperbound[j]);
                this.weights_loaded = true;
            }
        }

        // init_population[0] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581, 86516.3};
        // init_population[1] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,78561.4};
        // init_population[2] = new double[] {-0.03497211045837952,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,76210.8};
        // init_population[3] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,70503.5};
        // init_population[4] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,70491.6};
        // init_population[5] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-1023.6018123274217,69749.8};
        // init_population[6] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,68253.3};
        // init_population[7] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,55558.3};
        // init_population[8] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-26260.87767773257,-848.1350849151581,53088.1};
        // init_population[9] = new double[] {-0.03954396067533136,-38176.5851152073,-149067.0334177295,336.34108438682597,-0.0016361556415439,-24509.304811624967,-23515.588128517633,-1.3861447244286251,-482.81368390013466,-19430.13402577235,-848.1350849151581,51523.7};

        // for (int i=10;i<size_init_population;i++){
        //         //TODO: instead of always starting from a completely random population, add some good individuals from the beginning
        //     for (int j = 0; j<game.numFeatures(); j++) {
        // 		init_population[i][j]= getRandom(weights_lowerbound[j],weights_upperbound[j]);
        // 	}
        //     this.weights_loaded = true;
        // }
        System.out.println("Initial population generated....");
        //play with all the 1000 combinations and store the highest
        try {
        	init_population = evalPopulation(init_population,num_repetitions);
        }
        catch (InterruptedException e)
        {
        	System.out.println("Caught interrupted exception");
        }

        
        System.out.println("Initial population succesfully evaluated");

        //STEP2: choose the best ones -> selection
        // double tokeep = 0.05; //percentage of initial population you want to keep, must be smaller than 1!!!
        // double size_new_pop = 0.3; //size of new population relative to initial population (should be smaller than 0.5)
        //reason: is twice this percentage later cause cross over is mutual -> real percentace = 2*size_new_pop

        int numGenerations = num_generations; //100
        //double fraction = fraction;
        //int child_heuristic = child_heuristic; 
        //double prop_mutation= prop_mutation;
        //double fraction_direct_pass = fraction_direct_pass;


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
            
            int minLenght = Math.min(10, init_population.length);
            sortbyColumn(init_population,game.numFeatures());
            double[][] bestTen = new double[minLenght][game.numFeatures()+1];
            for (int j = 0; j < minLenght; j++) {
                System.arraycopy(init_population[j], 0, bestTen[j], 0, bestTen[j].length);
            }
            String dataString = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
            String fileName = size_init_population + "_" + numGenerations + "_" + child_heuristic + "_iteration_" + (i+1) + "_" + dataString;
            storeMatrix("resources/genetic/iterations/" + fileName, bestTen, numGenerations, size_init_population, child_heuristic);
        }
       


        //Fuse selected_population and init_population to get the really best!!
        //int entries = 10; //store the 10 best overall!!!
        //double [][]final_result = fuseMatrix(init_population,selected_population,entries);
        try {
            init_population = evalPopulation(init_population, num_repetitions);
        }
        catch (InterruptedException e)
        {
        	System.out.println("Caught interrupted exception");
        }
            

        String dataString = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
        String fileName = size_init_population + "_" + numGenerations + "_" + child_heuristic + "_" + dataString;
        storeMatrix("resources/genetic/" + fileName, init_population, numGenerations, size_init_population, child_heuristic);

        // System.out.println("Executor : You have completed "+init_population[0][game.numFeatures()]+" rows.");
        //BEST WEIGHTS:
        System.out.println(Arrays.toString(init_population[0]));

        this.weights_loaded = true;

        return new double[]{1};
    }


    public void adapt(final int iterations) {
        // Initialise qlearning rates as decreasing with time
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
                if (entry1[col] == entry2[col]){
                    return 0;
                }
                else if (entry1[col] > entry2[col]) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        });  // End of function call sort().
    }

    //function which returns a random number inside the interval [lower_bound,upper_bound]
    private double getRandom(double lower_bound,double upper_bound){
        Random r = new Random();
        double random = r.nextDouble();
        return (lower_bound+random*(upper_bound-lower_bound));
    }

	private double getRandomG(double mean,double std_dev){
        Random r = new Random();
        double random = r.nextGaussian();
        return (mean+random*std_dev);
    }    

    public class Evaluator implements Runnable {
    	
    	private double[] population;
    	private int num_repetitions;
    	private double[] weights;

    	public Evaluator(double[] population, int num_repetitions, double[] weights)
    	{
    		this.population = population;
    		this.num_repetitions = num_repetitions;
    		this.weights = weights;
    	}

    	public double[] getPopulation()
    	{
    		return this.population;
    	}

    	public void run() {
    		try {
    			ExecutorService executor = Executors.newFixedThreadPool(num_repetitions);
	            double[] store_score = new double[num_repetitions];
	            Performer[] performers = new Performer[num_repetitions];
	            for (int j = 0; j < num_repetitions; j++) {
	                performers[j] = new Performer(this.weights);
	                performers[j].setGame(j);
	                executor.execute(performers[j]);
	                // store_score[j] = performer.getVal();
	            }

	            executor.shutdown();
	            executor.awaitTermination(1000, TimeUnit.MINUTES);
	            for (int j = 0 ; j < num_repetitions; j++)
	            {
	                store_score[j] = performers[j].getVal();
	                System.out.println("Score : " + store_score[j]);
	                // store_score[j] = Gen_Agent.this.perf_scores[j];
	            }

	            double score_best = store_score[0];

	            
	            for (int j = 1; j < num_repetitions; j++) {
	                score_best = score_best + store_score[j];
	            }
	            score_best = score_best/num_repetitions;
	            
	            this.population[game.numFeatures()] = score_best;
	            // System.out.println("Score : " + score_best);
    		}
    		catch (InterruptedException e)
    		{
    			System.out.println("Interruption in Performer");
    		}

    		
        }

    }
    	

    //given a population, execute the game with it and store the results in the resulting array!
    //return: in descending order sorted array!
    private double[][] evalPopulation(double[][] population, int num_repetitions) throws InterruptedException {
        int size_population = population.length;

        ExecutorService executor1 = Executors.newFixedThreadPool(population.length);
        Evaluator[] evaluators = new Evaluator[population.length];
        
        for (int i=0;i<size_population;i++) {
            //set weights in this iteration
            for (int j = 0; j < game.numFeatures(); j++) {
                this.weights[j] = population[i][j];
            }

            evaluators[i] = new Evaluator(population[i], num_repetitions, this.weights);
            executor1.execute(evaluators[i]);
            
        }
        //Sort descending by the score!
        executor1.shutdown();
        executor1.awaitTermination(1000, TimeUnit.MINUTES);
        for (int i = 0; i < population.length; i++)
        {
        	population[i] = evaluators[i].getPopulation();
        	System.out.println("Returned : " + population[i][game.numFeatures()]);
        }
        
        sortbyColumn(population,game.numFeatures());
        return population;
    }

    private double[] evalChild(double[] child, int num_repetitions) throws InterruptedException {
            //set weights in this iteration
        for (int j = 0; j < game.numFeatures(); j++) {
            this.weights[j] = child[j];
        }
        
        //play num_repetition times
        ExecutorService executor = Executors.newFixedThreadPool(num_repetitions);
        double[] store_score = new double[num_repetitions];
        Performer[] performers = new Performer[num_repetitions];
        for (int j = 0; j < num_repetitions; j++) {
            performers[j] = new Performer(this.weights);
            performers[j].setGame(j);
            executor.execute(performers[j]);
            // store_score[j] = performer.getVal();
            // System.out.println("Store score is : " + store_score[j]);
        }

        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.MINUTES);
        for (int j = 0 ; j < num_repetitions; j++)
        {
            store_score[j] = performers[j].getVal();
            // store_score[j] = this.perf_scores[j];
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

        child[game.numFeatures()] = score_best;
    	return child;
    }


    int pickParent(double[][]input_population, double fraction){
        int max_idx = (int) Math.floor(input_population.length*fraction);
        int max_score_idx = 0;
        for (int i = 0; i < max_idx; i++){
            int r = (int) Math.floor(getRandom(0, input_population.length));
            if (input_population[max_score_idx][game.numFeatures()] < input_population[r][game.numFeatures()]){
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
        int num_features = game.numFeatures();

        try {
        	eval_population = evalPopulation(input_population, num_repetitions); // for the parent population
        }
        catch (InterruptedException e)
        {
        	System.out.println("caught interrupted exception");
        }
        double worst_performer_score = eval_population[eval_population.length-1][game.numFeatures()];

        //take the best 30% of the old generation (init generation)
        for (int i = 0 ; i < bestOfOld ; i++) {
            for (int j = 0; j < num_features; j++) {
            	
            	new_population[i][j] = eval_population[i][j];	
            	
            }
        }

        for (int i = bestOfOld; i < input_population.length ; i++) {
            double[] child = new double[game.numFeatures()+1];
            
            int iter = 0;
            do{
            	int parent1, parent2;
            	int iter1 = 0;
            	do {

            		parent1 = pickParent(input_population, fraction);
	            	parent2 = pickParent(input_population, fraction);
	            	iter1++;
	            
            	} while (parent1 == parent2 && iter1 < 5);
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
	                try {
	                	child = evalChild(child, num_repetitions);	
	                }
	                catch (InterruptedException e)
	                {
	                	System.out.println("caught interrupted exception");
	                }
	                
	            }
	            //heuristic 2
	            else if (child_heuristic == 1) {
	                for(int j = 0; j < game.numFeatures() + 1; j++){
	                    child[j] = (input_population[parent1][j] + input_population[parent2][j])/2;
	                }
	                try {
	                	child = evalChild(child, num_repetitions);	
	                }
	                catch (InterruptedException e)
	                {
	                	System.out.println("caught interrupted exception");
	                }
	                
	            }
                //heuristic 3 - mix n match weights from both parent 1 and parent 2
                else if(child_heuristic == 2) {
                    for (int j = 0; j < num_features; j++) {
                        double rand_val = getRandom(0, 1);
                        if (rand_val > 0.5){
                            child[j] = input_population[parent1][j];
                        }
                        else{
                            child[j] = input_population[parent2][j];
                        }
                    }
                    child[num_features] = (input_population[parent1][num_features] + input_population[parent2][num_features])/2;
                    try {
                    	child = evalChild(child, num_repetitions);	
                    }
                    catch (InterruptedException e)
	                {
	                	System.out.println("caught interrupted exception");
	                }
                    
                }
                //heuristic 4 - set a line
	            else {
                    int line = (int) Math.round(getRandom(0, num_features));
                    for (int j = 0; j < line; j++) {
                            child[j] = input_population[parent1][j];
                    }
                    for (int j = line; j < num_features; j++) {
                            child[j] = input_population[parent2][j];
                    }
                    child[num_features] = (input_population[parent1][num_features] + input_population[parent2][num_features])/2;
                    try {
                    	child = evalChild(child, num_repetitions);	
                    }
                    catch (InterruptedException e)
                    {
	                	System.out.println("caught interrupted exception");
	                }
                    
	            }
	            iter++;
            } while (child[game.numFeatures()] < worst_performer_score && iter < 5);
            

            // put child in the new generation
            for(int j = 0; j < game.numFeatures() + 1; j++){
            	new_population[i][j] = child[j];
                double mutate_rand_val = getRandom(0, 1);
                if(mutate_rand_val < prop_mutation){
                    new_population[i][j] = new_population[i][j] * getRandomG(1, 0.1);
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


    public void storeMatrix(final String filename, double[][]matrix, int generations, int size_init_population, int heuristic) {
        try {
            final FileWriter fw = new FileWriter(filename);
            fw.write(getCurrentTimeStamp());
            fw.write("\n");
            fw.write("Number of generations: " + generations);
            fw.write("\n");
            fw.write("Size of population: " + size_init_population);
            fw.write("\n");
            fw.write("Heuristic " + heuristic);
            fw.write("\n");
            fw.write(Integer.toString(game.numFeatures()));
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

    public void loadMatrix(final String filename)
    {
        int num_features_txt = 0;
        //System.out.println("resources/genetic/"+filename);
        try
        {
            final BufferedReader in = new BufferedReader(new FileReader("resources/genetic/"+filename));
            String line;
            int desired_weights = 2;
            int offset = 1; //there stand the number of weights!!!
            int i = 0;
            int a = 0;
            //System.out.println(game.numFeatures());
            while ((line = in.readLine()) != null)
            {
                //System.out.print(line);
                //System.out.print("\n");
                //System.out.print(i);
                if (i==offset){
                    final String[] values = line.split(",");
                    for (String str : values)
                    {
                        num_features_txt = Integer.parseInt(str);
                        //Stop if number of features do not coincide
                        if(num_features_txt!=game.numFeatures()){throw new java.lang.Error("number of features are different!");}
                    }
                }


                if (i==desired_weights){
                    final String[] values = line.split(",");
                    for (String str : values)
                    {
                        if (a<game.numFeatures()){
                            this.weights[a] = Double.parseDouble(str);
                            a++;
                        }
                        if (a>=(game.numFeatures()-1)){this.weights_loaded=true;}

                    }
                }
                i++;
            }

        } catch (IOException e) { e.printStackTrace(); }
        System.out.println("Loaded Desired weights in " + filename);
        System.out.println(Arrays.toString(this.weights));
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    public Game getGame() {return game; }

}

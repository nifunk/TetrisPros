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
import java.util.Random;

public class Gen_Agent {

    private Game        game;
    private double[]    weights;
    private boolean     weights_loaded;
    private int[]       perf_scores;
    Random random = new Random();
    private final double pso_omega = 0.2;
	private final double pso_local_coeff = 0.13;
	private final double pso_global_coeff = 0.2;
	
	private double[][] 	vel;
    private double[][]  personalBestWeights;
    private double[]    globalBest;
    
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

    public void initVelocity(int popIndex) {
    	for (int i = 0; i < game.numFeatures(); i++)
    	{
    		this.vel[popIndex][i] = getRandom(0,1) * (-2) * 2 - (-2);
    	}
    }

    public void updateVelocity(int popIndex, double[] population) {
		double rl, rg;

		for (int i = 0; i < game.numFeatures(); i++) {
			rl = getRandom(0, 1);
			rg = getRandom(0, 1);

			this.vel[popIndex][i] = pso_omega * vel[popIndex][i] + pso_local_coeff * rl * (personalBestWeights[popIndex][i] - population[i])
					+ pso_global_coeff * rg * (globalBest[i] - population[i]);
		}
	}

	public double[] updatePosition(int popIndex, double[] population) {
		for (int i = 0; i < game.numFeatures(); i++) {
			population[i] = population[i] + vel[popIndex][i];
		}
		return population;
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

        int size_init_population = population_size;
        int num_repetitions = 5;
        double[][]init_population = new double[size_init_population][game.numFeatures()+1]; //1000 init weights,... store weights and score
        double[]weights_lowerbound = new double[game.numFeatures()];
        Arrays.fill(weights_lowerbound, 0.0);
        double[]weights_upperbound = new double[game.numFeatures()];
        Arrays.fill(weights_upperbound, 1.0);
        this.vel = new double[population_size][game.numFeatures()];
        this.personalBestWeights = new double[population_size][game.numFeatures()+1];
        this.globalBest = new double[game.numFeatures()+1];


        // generate initial population
        for (int i=0;i<size_init_population;i++){
            initVelocity(i);
            
            for (int j = 0; j<game.numFeatures(); j++){
                init_population[i][j]= getRandom(weights_lowerbound[j],weights_upperbound[j]);
                this.personalBestWeights[i][j] = init_population[i][j];
                this.globalBest[j] = init_population[i][j];
                this.weights_loaded = true;
            }
        }

        System.out.println("Initial population generated....");
        try {
        	init_population = evalPopulation(init_population,num_repetitions);
        }
        catch (InterruptedException e)
        {
        	System.out.println("Caught interrupted exception");
        }

        
        System.out.println("Initial population succesfully evaluated");

        int numGenerations = num_generations; //100        

        for (int i = 0; i < numGenerations ; i++) {
            init_population = doCrossingandMutation_new(init_population, num_repetitions, fraction, child_heuristic, prop_mutation, fraction_direct_pass);
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

    public class Evaluator implements Runnable {
    	
    	private double[] population;
    	private int num_repetitions;
    	private double[] weights;

    	public Evaluator(double[] population, int num_repetitions, double[] weights)
    	{
    		this.weights = new double[weights.length];
            this.population = population;
    		this.num_repetitions = num_repetitions;
    		for(int j = 0 ; j < game.numFeatures(); j++)
            {
                this.weights[j] = population[j];
            }
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
	                // store_score[j] = Gen_Agent.this.perf_scores[j];
	            }

	            double score_best = store_score[0];

	            
	            for (int j = 1; j < num_repetitions; j++) {
	                score_best = score_best + store_score[j];
	            }
	            score_best = score_best/num_repetitions;
	            
	            this.population[game.numFeatures()] = score_best;
	            System.out.println("Score : " + score_best);
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
            updateVelocity(i, population[i]);
            population[i] = updatePosition(i, population[i]);
            
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
        	if (population[i][game.numFeatures()] > this.personalBestWeights[i][game.numFeatures()])
        	{
        		for (int k = 0 ;k <= game.numFeatures(); k++)
        		{
        			this.personalBestWeights[i][k] = population[i][k];
        		}
        	System.out.println("Updating personal best : " + this.personalBestWeights[i][game.numFeatures()]);
        	}
        	
        }
        
        sortbyColumn(population,game.numFeatures());
        if (population[0][game.numFeatures()] > this.globalBest[game.numFeatures()])
        {
        	for (int k = 0 ;k <= game.numFeatures(); k++)
    		{
    			this.globalBest[k] = population[0][k];
    		}
    		System.out.println("Updating global best : " + this.globalBest[game.numFeatures()]);
        }
        return population;
    }

    private double[] evalChild(double[] child, int num_repetitions) throws InterruptedException {
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
        }

        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.MINUTES);
        for (int j = 0 ; j < num_repetitions; j++)
        {
            store_score[j] = performers[j].getVal();
            // store_score[j] = this.perf_scores[j];
        }

        double score_best = store_score[0];

        for (int j = 1; j < num_repetitions; j++) {
            score_best = score_best + store_score[j];
        }
        score_best = score_best/num_repetitions;

        child[game.numFeatures()] = score_best;
    	return child;
    }

    int pickParentnew(double[][]input_population, double[][]eval_population, float fit_total, int num_features){
        int j;
        double temp_check;
        temp_check = random.nextFloat() * fit_total;

        for(j = 0; j < input_population.length; j++) {
            double curr_fit = eval_population[j][num_features];
            if(temp_check < curr_fit) {
                return j;
            }
            else {
                temp_check = temp_check - curr_fit;
            }
        }

        return 0;
    }

    private double[][] doCrossingandMutation_new(double[][] input_population, int num_repetitions, double fraction, int child_heuristic, double prop_mutation, double fraction_direct_pass){
        int size_input = input_population.length;
        int num_features = game.numFeatures();
        float crossoverRate = 0.6f;

        double[][]new_population = new double[input_population.length][game.numFeatures()+1];
        double[][]eval_population = new double[input_population.length][game.numFeatures()+1];

        try {
            eval_population = evalPopulation(input_population, num_repetitions); // for the parent population
        }
        catch (InterruptedException e)
        {
            System.out.println("caught interrupted exception");
        }

        float fit_total = 0.0f;
        for(int j = 0; j < size_input; j++) {
            fit_total += eval_population[j][num_features];
        }

        int pop_size_thus_far = 0;

        while(pop_size_thus_far < size_input) {
            int parent1 = pickParentnew(input_population, eval_population, fit_total, num_features);
            int parent2 = pickParentnew(input_population, eval_population, fit_total, num_features);

            if(random.nextFloat() < crossoverRate) {//cross over happens
                int pos_of_cross = random.nextInt(num_features + 1);
                System.arraycopy(input_population[parent1], 0, new_population[pop_size_thus_far], 0, pos_of_cross);
                System.arraycopy(input_population[parent2], 0, new_population[pop_size_thus_far + 1], 0, pos_of_cross);
                System.arraycopy(input_population[parent1], pos_of_cross, new_population[pop_size_thus_far + 1], pos_of_cross, num_features - pos_of_cross);
                System.arraycopy(input_population[parent2], pos_of_cross, new_population[pop_size_thus_far], pos_of_cross, num_features - pos_of_cross);
                pop_size_thus_far = pop_size_thus_far + 2;
            }
            else {
                for(int j = 0; j < game.numFeatures() + 1; j++){
                    new_population[pop_size_thus_far][j] = input_population[parent1][j];
                }
                pop_size_thus_far++;

                for(int j = 0; j < game.numFeatures() + 1; j++){
                    new_population[pop_size_thus_far][j] = input_population[parent2][j];
                }
                pop_size_thus_far++;
            }

            for(int j = 0; j < game.numFeatures() + 1; j++){
                double mutate_rand_val = getRandom(0, 1);
                if(mutate_rand_val < prop_mutation){
                    new_population[pop_size_thus_far-2][j] = new_population[pop_size_thus_far-2][j] * getRandom(0.8, 1.2);
                }
            }

            for(int j = 0; j < game.numFeatures() + 1; j++){
                double mutate_rand_val = getRandom(0, 1);
                if(mutate_rand_val < prop_mutation){
                    new_population[pop_size_thus_far-1][j] = new_population[pop_size_thus_far-1][j] * getRandom(0, 1.5);
                }
            } 
        }

        return new_population;
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
            	int parent1 = pickParent(input_population, fraction);
	            int parent2 = pickParent(input_population, fraction);
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
        try
        {
            final BufferedReader in = new BufferedReader(new FileReader("resources/genetic/"+filename));
            String line;
            int desired_weights = 2;
            int offset = 1; //there stand the number of weights!!!
            int i = 0;
            int a = 0;
            while ((line = in.readLine()) != null)
            {
                if (i==offset){
                    final String[] values = line.split(",");
                    for (String str : values)
                    {
                        num_features_txt = Integer.parseInt(str);
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

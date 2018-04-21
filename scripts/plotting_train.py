'''
Plotting results of training genetic algorithm.
Input: path to directory
'''
import matplotlib.pyplot as plt
import numpy as np
import os 
import pandas as pd
import seaborn as sns
sns.set_style("dark")
import sys

# Read input argument - Path to directory. 
if len(sys.argv) < 2:
	raise InvalidArgumentError("Please input path !")

# Create data frame. 
df = pd.DataFrame()

# Read out files. 
for subdir, dirs, files in os.walk(sys.argv[1]):
	for file in files:
		sample = {}
		path   = subdir + os.sep + file
		# Mereley take txt files in directory. 
		if not path.endswith(".txt"):
			continue
		# Read basic information from file name. 
		sample['InitPopulation'] = int(file.split("_")[0])
		sample['Generation']     = int(file.split("_")[4]) 
		sample['Heuristic']      = int(file.split("_")[2])
		# Read number of cleared rows out of file. 
		txt_in_file = open(path)
		num_line    = 0
		sample['NumCleared'] = []
		for line in txt_in_file:
			num_line += 1
			if num_line == 5:
				sample['NumFeatures'] = int(line)
			if num_line < 6:
				continue 
			sample['NumCleared'].append(float(line.split(",")[-2]))
		sample['BestResult'] = sample['NumCleared'][0]
		# Add to pandas dataframe. 
		df = df.append(sample, ignore_index=True)

# Feature engineering. 
df['MeanResult']   = df['NumCleared'].apply(np.mean)
df['MedianResult'] = df['NumCleared'].apply(np.median)
df = df.drop(df[df['BestResult'] < 10000].index)
df = df.drop(df[df['InitPopulation'] == 20.0].index)
df = df.drop(df[df['InitPopulation'] == 50.0].index)
df = df.replace([np.inf, -np.inf], np.nan).dropna()

# Generations plot. 
sns.violinplot(x='InitPopulation', y='BestResult', data=df, color=".3")
sns.swarmplot(x='InitPopulation', y='BestResult', hue='Heuristic', data=df,
              size=2, linewidth=0)
plt.show()
# Heuristics plot. 
sns.violinplot(x='Heuristic', y='BestResult', data=df, color=".3")
sns.swarmplot(x='Heuristic', y='BestResult', hue='InitPopulation', data=df,
              size=2, linewidth=0)
plt.show()
# Number of features plot. 
g = sns.boxplot(x='NumFeatures', y='MeanResult', data=df)
g.set(xticklabels=['Madhav', 'Niklas'])
plt.show()
# Outlier plot (mean over best).
sns.lmplot(x='MedianResult', y='MeanResult', data=df,
           fit_reg=False, 
           hue='InitPopulation')
plt.show()
sns.lmplot(x='BestResult', y='MeanResult', data=df,
           fit_reg=False, 
           hue='InitPopulation')
plt.show()
# Training improves over generation. 
df_40 = df.drop(df[df['InitPopulation'] != 40.0].index)
sns.lmplot(x='Generation', y='BestResult', data=df_40)
plt.show()


		
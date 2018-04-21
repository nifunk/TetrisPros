'''
Plotting results of running genetic algorithm.
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
		path   = subdir + os.sep + file
		# Mereley take txt files in directory. 
		if not path.endswith(".txt"):
			continue
		# Read number of cleared rows out of file. 
		txt_in_file = open(path)
		for line in txt_in_file:
			for num_string in line.split(","):
				if num_string == '': 
					continue
				sample = {'Name' : file.replace(".txt", ""), 
						  'Results' : int(num_string)}
				# Add to pandas dataframe. 
				df = df.append(sample, ignore_index=True)

# Feature engineering. 
df = df.replace([np.inf, -np.inf], np.nan).dropna()

# Comparision boxplot.  
sns.boxplot(y='Results', x='Name', data=df)
plt.show()

# Histogram. 
sns.set(); 
sns.distplot(df['Results'], bins=10, kde=False, hist=True)
plt.ylabel("Frequency")
plt.show()


		
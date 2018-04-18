'''
Plot trainig procedure plots such as error over epochs.
Training file (errors) as input argument.
'''

import matplotlib.pyplot as plt
import sys

# Check input arguments. 
if(len(sys.argv) != 2):
	raise InvalidArgumentError("Please input file as argument !")
file_name = sys.argv[1]

# Open and read file. 
file = open(file_name, 'r')
errors = []
for line in file.readlines(): 
	words = line.split(", ")
	for x in words: 
		try:
			errors.append(float(x))
		except Exception as e:
			pass
print(errors)

# Plot errors. 
plt.plot(errors)
plt.xlabel("Training epoch")
plt.ylabel("MSE (Output vs Input)")
plt.show()

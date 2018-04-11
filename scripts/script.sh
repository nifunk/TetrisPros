#!/bin/bash
#numruns=$1
gen=(5 10 20 50 100 150 200 250 300 400 500 750 1000)
psize=50
heur=0
frac=0.25
probmut=0.05
fracpas=0.3
cd /home/users/nus/e0268461/AI/TetrisPros
module load java
javac Player.java
for i in {0..5}
do
	echo "Running $(i+1)"
        java Player ${gen[$i]} $psize $heur $frac $probmut $fracpas &
done


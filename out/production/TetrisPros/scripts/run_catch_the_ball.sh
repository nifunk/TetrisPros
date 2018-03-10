#!/usr/bin/env bash
cd ..
rm -r build
mkdir build
javac CTBPlayer.java -d build/
cd build
java CTBPlayer
cd ../scripts
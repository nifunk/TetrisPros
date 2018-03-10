#!/usr/bin/env bash
cd ..
rm -r build
mkdir build
javac Player.java -d build/
cd build
java Player
cd ../scripts
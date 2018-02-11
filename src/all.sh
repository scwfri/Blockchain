#! /bin/bash

xterm -hold -e java Blockchain 0 &
sleep 2 &
xterm -hold -e java Blockchain 1 &
sleep 2 &
xterm -hold -e java Blockchain 2

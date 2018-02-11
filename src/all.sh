#! /bin/bash

java Blockchain 0 &
sleep 2 &
java Blockchain 1 &
sleep 2 &
java Blockchain 2

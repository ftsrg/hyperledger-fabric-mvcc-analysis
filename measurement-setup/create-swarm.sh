#!/bin/bash

echo "####### INITIALIZE SWARM"
npx cluster-admin swarm init

echo "####### SLEEP 20s"
sleep 20s

echo "####### JOIN SWARM"
npx cluster-admin swarm join --all $1 # optional --no-parallel flag
#!/bin/bash

echo "####### LEAVE SWARM"
npx cluster-admin swarm leave --all $1 # optional --no-parallel flag
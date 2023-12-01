#!/bin/bash

./destroy-swarm.sh $1

echo "####### REBOOT"
npx cluster-admin general exec --all --command 'sudo reboot'

SLEEP_TIME=120

echo "####### SLEEP ${SLEEP_TIME}s"
sleep ${SLEEP_TIME}s

./create-swarm.sh $1
#!/bin/bash

STACK=test

echo "####### CLEANUP - REMOVE STACK"
npx cluster-admin swarm rm-stack --stack ${STACK}

echo "####### CLEANUP - CLEAN CHAINCODES REMNANTS"
npx cluster-admin docker clean-cc -a

echo "####### CLEANUP - REMOVE STACK"
npx cluster-admin swarm rm-stack --stack ${STACK}

echo "####### CLEANUP - PRUNE VOLUMES"
npx cluster-admin docker prune-volumes -a

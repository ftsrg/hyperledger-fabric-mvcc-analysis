#!/bin/bash

##Script to set up the enviroment for running tests
NPM_PATH=`which npm`
if [ ! -f "$NPM_PATH" ]; then
  echo "npm is not installed"
  echo "npm version 6.14.4 and node version 10.19.0"
  echo "are known to be compatible with the project"
else
    ROOT=`pwd`
    DIR=./node_modules
    if [ ! -d "$DIR" ]; then
        # Install modules if they are missing
        echo "Installing node_modules in `pwd`..."
        npm install
        cd "$ROOT"
    fi
    DIR=./benchmarks/swarm/tpcc/configs/fabric/cryptogen-to-wallet/node_modules
    if [ ! -d "$DIR" ]; then
        # Install modules if they are missing
        cd `dirname "$DIR"`
        echo "Installing node_modules in `pwd`..."
        npm install
        cd "$ROOT"
    fi
    echo "Dependecies installed"
    echo "Generating crypto artifacts"
    cd ./benchmarks/swarm/tpcc/configs/fabric/
    chmod +x generate.sh
    ./generate.sh
    rm -rf ./cryptogen-to-wallet/node_modules
    cd "$ROOT"
    echo "Setup finished"
fi
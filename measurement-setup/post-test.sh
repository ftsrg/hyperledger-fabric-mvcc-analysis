#!/bin/bash

if (($# < 1))
then
	echo "Specify the name of the test"
	exit -1
fi

echo "Dump Elasticsearch data"
npx cluster-admin elastic dump --index bench --field blockinfo --path ./dumps/blockinfo.txt
npx cluster-admin elastic dump --index bench --field txinfo --path ./dumps/txinfo.txt
npx cluster-admin elastic dump --index bench --field shorttxinfo --path ./dumps/shorttxinfo.txt

echo "####### Retrieve system logs"
npx cluster-admin general exec -a --command "sudo chmod -R 777 ~/tpcc/systemlogs"
npx cluster-admin data collect -a --source "~/tpcc/systemlogs" --target ./dumps

echo "Collecting explorer data..."
#./explorerdump.py -h vm.niif.cloud.bme.hu -p 11408 -d fabricexplorer -u hppoc -pw password -t ./$1/explorerdump
./collect-db.sh

echo "Preparing for artifact processing..."
mkdir $1
mkdir $1/fabricconf
mkdir $1/explorerdump
cp -r ./dumps/* ./$1/
rm ./$1/processed_systemlogs/*

echo "Generating plots from system resource logs..."
./sys-mon-plot.sh $1
rm ./$1/systemlogs/*

echo "Collecting test configurations for archiving..."
cp -r ./benchmarks/swarm/tpcc/configs/fabric/peers $1/fabricconf
cp -r ./benchmarks/swarm/tpcc/configs/fabric/configtx.yaml $1/fabricconf

echo "Transforming json logs to csv..."
python ./json-to-csv.py ./$1 blockinfo && python ./json-to-csv.py ./$1 txinfo && python ./json-to-csv.py ./$1 shorttxinfo
rm ./$1/*.txt

echo "Archiving test artifacts..."
tar -czvf $1.tar.gz ./$1

echo "Cleaning up.."
rm -rf ./$1
rm -rf ./dumps/systemlogs/*

echo "Finished"

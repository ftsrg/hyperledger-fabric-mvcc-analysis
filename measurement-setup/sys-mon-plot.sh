#!/bin/bash


genName() {
    FILENAME=$(basename $1)
    FILENAME=$(echo $FILENAME | cut -d'.' -f1)
    FILENAME="${FILENAME}.html"
    echo $FILENAME
}

if [ -d ./dumps/systemlogs ]
then
    mkdir ./dumps/processed_systemlogs
    for log in ./dumps/systemlogs/*.json; do
	    cmonitor_chart --input=$log --output=./$1/processed_systemlogs/$(genName $log)
    done;
fi

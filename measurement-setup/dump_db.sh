#!/bin/bash
# dumps data from postgre to the path specified
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
else
    CONTAINER_NAME=$(docker ps --filter "name=explorerdb" --format "{{.Names}}")
    docker exec -t $CONTAINER_NAME pg_dump fabricexplorer -c -U hppoc | gzip > $1
fi
#!/bin/bash
# npx cluster-admin general exec --nodes manager --command "docker exec -it `docker ps | grep "explorerdb" | awk -F ' ' '{ print $12 }'` pg_dump fabricexplorer -c -U postgres | gzip > dump_explorerdb.sql.gz"
# npx cluster-admin general exec --nodes manager --command "docker exec -it `docker ps | grep "explorerdb" | awk -F ' ' '{ print $12 }'` pg_dump fabricexplorer -c -U postgres | gzip > ~/dumps/dump_explorerdb.sql.gz"
echo "TRANSFERRING SCRIPTS" 
echo "transfer script"
npx cluster-admin data distribute --nodes manager --source "./scripts" --target "~/tpcc"
echo "make executable"
npx cluster-admin general exec --nodes manager --command "sudo chmod +x ~/tpcc/dump_db.sh"
echo "COLLECTING DATA"
npx cluster-admin general exec --nodes manager --command "mkdir ~/tpcc/dumps"
echo "dumping data from explorer"
npx cluster-admin general exec --nodes manager --command "sudo ./tpcc/dump_db.sh ~/tpcc/dumps/explorerdb_dump.sql.gz"
echo "collecting dumped data"
npx cluster-admin data collect --nodes manager --source "~/tpcc/dumps" --target ./dumps
# pg_dump fabricexplorer -c -U postgres | gzip > dump_explorerdb.sql.gz

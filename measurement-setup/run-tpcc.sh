#!/bin/bash

STACK=test
BENCH=tpcc

# $1: seconds
function sleep_sec() {
    echo "####### SLEEPING ${1}s"
    sleep "${1}s"
}

# $1: service dir
function deploy() {
    echo "####### DEPLOYING: ${1}"
    npx cluster-admin swarm deploy --services "~/${BENCH}/services/${1}" --stack ${STACK} --auth
}

# $1: service name
function wait_for() {
    echo "####### WAITING FOR: ${1}"
    sleep 5s
    npx cluster-admin swarm wait --service "${STACK}_${1}"
}

rm -rf ./logs/*

./clean-services.sh

echo "####### CLEANUP - DELETE ARTIFACTS"
npx cluster-admin data delete -a --target "~/${BENCH}"

FABRIC_DIR="./benchmarks/swarm/${BENCH}/configs/fabric"
if [[ ! -d "${FABRIC_DIR}/crypto-config" ]]; then
  echo "####### GENERATE FABRIC ARTIFACTS"
  $( cd ${FABRIC_DIR} && ./generate.sh )
fi

echo "####### DISTRIBUTE ARTIFACTS"
npx cluster-admin data distribute -a --source "./benchmarks/swarm/${BENCH}" --target '~/'
npx cluster-admin data distribute --nodes manager --source ./scripts --target '~/tpcc'

deploy "management"
deploy "logging"
sleep_sec "10"

deploy "fabric-orderers"
sleep_sec "5"
deploy "fabric-peers"
sleep_sec "5"

deploy "fabric-cli"
wait_for "fabric-cli"
wait_for "fabric-cli"
wait_for "fabric-cli"

echo "####### Deploying explorer"
echo "        STEP #1 deploy postgresql db"
deploy "explorerdb"
sleep_sec "30"

echo "        STEP #2 deploy explorer"
deploy "explorer"

echo "####### Deploying system-monitoring"
npx cluster-admin general exec -a --cwd "~/tpcc/dumps" --command "docker run -d \
   --rm \
   --name=cmonitor-baremetal-collector \
   --network=host \
   --pid=host \
   --volume=/sys:/sys:ro \
   --volume=/etc/os-release:/etc/os-release:ro \
   --volume=/home/cloud/tpcc/systemlogs:/perf:rw \
   f18m/cmonitor:v2.2-0 \
   --sampling-interval=5"
sleep_sec "5"

deploy "mosquitto"
deploy "caliper"
wait_for "caliper-master"

echo "####### Stop system monitoring"
npx cluster-admin general exec -a --command "sudo docker container stop cmonitor-baremetal-collector"


./post-test.sh $1

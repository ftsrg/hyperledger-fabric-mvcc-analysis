#!/bin/bash

export NUM_OF_ORGS=2
export NUM_OF_PEERS_MINUS_ONE=0
export CHANNEL_NAME=epengo-channel
export CONFIG_ROOT=/etc/fabric-config
export CRYPTO=${CONFIG_ROOT}/crypto-config
export GODEBUG=netdns=go
export SLEEP_TIME=5

export FABRIC_CFG_PATH=/etc/hyperledger/fabric
export ORDERER_URL=orderer0:7050
export ORDERER_CAFILE=${CRYPTO}/ordererOrganizations/example.com/orderers/orderer0.example.com/tls/ca.crt
export CORE_PEER_TLS_ENABLED=true
function log() {
    echo "#### ${1}"
}

# $1: org MSP ID
# $2: org name
function setup_org_peer_context() {
    export CORE_PEER_LOCALMSPID=${1}
    export CORE_PEER_MSPCONFIGPATH=${CRYPTO}/peerOrganizations/${2}/users/Admin@${2}/msp
    export CORE_PEER_ADDRESS=peer${3}${2}:7051
    export CORE_PEER_TLS_ROOTCERT_FILE=${CRYPTO}/peerOrganizations/${2}/users/Admin@${2}/tls/ca.crt
    export CORE_PEER_TLS_CERT_FILE=${CRYPTO}/peerOrganizations/${2}/users/Admin@${2}/tls/client.crt
    export CORE_PEER_TLS_KEY_FILE=${CRYPTO}/peerOrganizations/${2}/users/Admin@${2}/tls/client.key    
}

function use_central_bank() {
    log "Switching to central bank identity"
    setup_org_peer_context "CentralBankOrgMSP" "cb" ${1}
}

function use_fi() {
    log "Switching to FI identity"
    setup_org_peer_context "FIOrgMSP" "fi" ${1}
}

function safety_sleep() {
    log "Sleeping ${SLEEP_TIME}s"
    sleep ${SLEEP_TIME}s
}

function join_channel() {
    # The "peer channel create" command will create a local file "<channel_name>.block"
    log "Joining channel"
    if peer channel join --blockpath ${CHANNEL_NAME}.block --tls --cafile ${ORDERER_CAFILE}
    then
        log "Joined channel"
    else
        log "Failed to join channel: ${CHANNEL_NAME}"
        exit 1
    fi
}

# $1: "cb" or "fi"
function update_anchor() {
    log "Updating anchor peer"
    FILE=${CONFIG_ROOT}/$1-update.tx
    if peer channel update --orderer ${ORDERER_URL} --channelID ${CHANNEL_NAME} --file ${FILE} --tls --cafile ${ORDERER_CAFILE}
    then
        log "Anchor peer updated"
    else
        log "Anchor peer update failed: $?"
        exit 1
    fi
}

# $1: name
# $2: version
# $3: path
# $4: language
function install_chaincode() {
    log "Installing chaincode: $1"
    if peer chaincode install --name "$1" --version "$2" --path "$3" --lang "$4" --tls --cafile ${ORDERER_CAFILE}
    then
        log "Chaincode installed"
    else
        log "Chaincode install failed: $?"
        exit 1
    fi
}

# $1: name
# $2: version
function instantiate_chaincode() {
    log "Instantiating chaincode: $1"
    POLICY="OR('CentralBankOrgMSP.member','FIOrgMSP.member')"
    if peer chaincode instantiate --orderer ${ORDERER_URL} --channelID ${CHANNEL_NAME} --name $1 --version $2 --policy ${POLICY} --ctor '{"Args":[]}' --tls --cafile ${ORDERER_CAFILE}
    then
        log "Chaincode instantiated"
    else
        log "Chaincode instantiation failed: $?"
        exit 1
    fi
}

# $1: chaincode name
# $2: peer name
function ping_chaincode() {
    log "Pinging chaincode $1 on $3"
    if peer chaincode query --channelID ${CHANNEL_NAME} --name $1 --ctor '{"Args":["ping"]}' --peerAddresses "${3}:7051" --tls --tlsRootCertFiles "${CRYPTO}/peerOrganizations/$2/peers/peer0.$2/msp/tlscacerts/tlsca.$2-cert.pem"
    then
        log "Chaincode pinged"
    else
        log "Chaincode ping failed: $?"
        exit 1
    fi
}



function install_cbdc_chaincode() {
    install_chaincode "cbdc" "1.0.0" "/opt/contracts/cbdc" "node"
}

function install_micro_chaincode() {
    install_chaincode "micro" "1.0.0" "/opt/contracts/micro" "java"
}

function install_micro_aff_chaincode() {
    install_chaincode "micro-aff" "1.0.0" "/opt/contracts/micro-aff" "java"
}

function install_micro_naive_chaincode() {
    install_chaincode "micro-naive" "1.0.0" "/opt/contracts/micro-naive" "java"
}

function install_fabcar_chaincode() {
    install_chaincode "fabcar" "1.0.0" "/opt/contracts/fabcar" "node"
}

use_central_bank "0"

log "Creating channel"
if peer channel create --orderer ${ORDERER_URL} --channelID ${CHANNEL_NAME} --file ${CONFIG_ROOT}/${CHANNEL_NAME}.tx --tls --cafile ${ORDERER_CAFILE}
then
    log "Channel created"
else
    log "Channel creation failed: $?"
    exit 1
fi

safety_sleep

use_central_bank "0"
join_channel

use_fi "0"
join_channel

#use_central_bank "1"
#join_channel
#
#use_fi "1"
#join_channel
#
#use_central_bank "2"
#join_channel
#
#use_fi "2"
#join_channel
#
#use_central_bank "3"
#join_channel
#
#use_fi "3"
#join_channel

# safety_sleep

use_central_bank "0"
update_anchor "cb"

use_fi "0"
update_anchor "fi"

# safety_sleep

use_central_bank "0"

install_micro_chaincode
install_micro_naive_chaincode
install_micro_aff_chaincode

use_fi "0"

install_micro_chaincode
install_micro_aff_chaincode
install_micro_naive_chaincode

#use_central_bank "1"
#install_cbdc_chaincode
#
#use_fi "1"
#install_cbdc_chaincode
#
#use_central_bank "2"
#install_cbdc_chaincode
#
#use_fi "2"
#install_cbdc_chaincode
#
#use_central_bank "3"
#install_cbdc_chaincode
#
#use_fi "3"
#install_cbdc_chaincode

use_central_bank "0"
instantiate_chaincode "micro" "1.0.0"
instantiate_chaincode "micro-aff" "1.0.0"
instantiate_chaincode "micro-naive" "1.0.0"

safety_sleep

#TODO ping method for chaincode micro
#ping_chaincode "cbdc" "cb" "peer0cb"
#ping_chaincode "cbdc" "fi" "peer0fi"
#ping_chaincode "cbdc" "cb" "peer1cb"
#ping_chaincode "cbdc" "fi" "peer1fi"
#ping_chaincode "cbdc" "cb" "peer2cb"
#ping_chaincode "cbdc" "fi" "peer2fi"
#ping_chaincode "cbdc" "cb" "peer3cb"
#ping_chaincode "cbdc" "fi" "peer3fi"

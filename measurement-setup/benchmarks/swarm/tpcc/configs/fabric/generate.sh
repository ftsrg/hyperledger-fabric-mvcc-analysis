#!/usr/bin/env bash

# if the binaries are not available, download them
if [[ ! -d "bin" ]]; then
  curl -sSL http://bit.ly/2ysbOFE | bash -s -- -ds
fi

rm -rf ./crypto-config/
rm -f ./genesis.block
rm -f ./*.tx
rm -rf ./wallet/

./bin/cryptogen generate --config=./crypto-config.yaml

# Rename the key files we use to be key.pem instead of a uuid
for KEY in $(find crypto-config -type f -name "*_sk"); do
    KEY_DIR=$(dirname ${KEY})
    mv ${KEY} ${KEY_DIR}/key.pem
done

export NUM_OF_ORGS=2
FABRIC_CFG_PATH=$PWD ./bin/configtxgen -profile OrdererGenesis -outputBlock genesis.block -channelID syschannel
FABRIC_CFG_PATH=$PWD ./bin/configtxgen -profile ChannelConfig -outputCreateChannelTx epengo-channel.tx -channelID epengo-channel

FABRIC_CFG_PATH=$PWD ./bin/configtxgen -profile ChannelConfig -outputAnchorPeersUpdate cb-update.tx -channelID epengo-channel -asOrg CentralBankOrg 
FABRIC_CFG_PATH=$PWD ./bin/configtxgen -profile ChannelConfig -outputAnchorPeersUpdate fi-update.tx -channelID epengo-channel -asOrg FIOrg
cd cryptogen-to-wallet
npm install
node convert.js ../crypto-config fi FIOrgMSP ../wallet
node convert.js ../crypto-config cb CentralBankOrgMSP ../wallet
rm -rf node_modules/
cd ..
rm -rf bin/
rm -rf config/
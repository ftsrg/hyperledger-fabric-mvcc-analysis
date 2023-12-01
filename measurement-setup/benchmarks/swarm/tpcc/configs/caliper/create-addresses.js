'use strict';

const { CaliperUtils } = require('@hyperledger/caliper-core');
const {createAccountManager, getAccountManager} = require('./account-manager');
const txLogger = CaliperUtils.getLogger('txinfo');

let blockchain;
let context;
let workerId;
let workerTotal;
let txIndex;

let ccId;
let ccVersion;
let timeoutSeconds;
let accountManager;

module.exports.info  = 'Caliper workload module for creating CBDC accounts';

module.exports.prepareWorkerParameters = async (workerParameters) => {
    return workerParameters;
};

module.exports.init = async (bc, contx, args, workerParameters, workerIndex, totalWorkers) => {
    blockchain = bc;
    context = contx;
    workerId = workerIndex;
    workerTotal = totalWorkers;
    txIndex = 0;
    
    ccId = args.chaincodeId;
    ccVersion = args.chaincodeVersion;
    timeoutSeconds = args.timeoutSeconds;

    createAccountManager();
    accountManager = getAccountManager();
};

module.exports.run = async () => {
    const currentIndex = txIndex;
    txIndex += 1;

    const invokeArgs = {
        chaincodeFunction: 'createAddress',
        chaincodeArguments: accountManager.createAddressAndGetArgs(),
        invokerIdentity: 'User1@fi'
    };
    let results = await blockchain.invokeSmartContract(context, ccId, ccVersion, invokeArgs, timeoutSeconds);

    for (let result of results) {
        let txinfo = {
            tx_id: result.GetID(),
            time_create: result.GetTimeCreate(),
            time_end: result.GetTimeFinal(),
            status: result.GetStatus(),
            function: 'create-address',
            worker_index: workerId,
            tx_index: currentIndex
        };

        let custom = result.GetCustomData();
        for (let entry of custom.entries()) {
            if (entry[0].includes('endorsement_result')) {
                continue;
            }

            txinfo[entry[0]] = entry[1].toString();
        }

        txLogger.info(JSON.stringify(txinfo));
    }

    return results;
};

module.exports.end = async () => {};
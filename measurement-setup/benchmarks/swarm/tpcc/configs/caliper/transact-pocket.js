'use strict';

const { CaliperUtils } = require('@hyperledger/caliper-core');
const txLogger = CaliperUtils.getLogger('txinfo');
const logger = CaliperUtils.getLogger('transfer-units');

let blockchain;
let context;
let workerId;
let workerTotal;
let txIndex;

let ccId;
let ccVersion;
let timeoutSeconds;
let numAccounts;
let roundId;

module.exports.info  = 'Caliper workload module for transacting with pockets in microBM';

module.exports.prepareWorkerParameters = async (workerParameters) => {
    return workerParameters;
};

module.exports.init = async (bc, contx, args, workerParameters, workerIndex, totalWorkers) => {
    blockchain = bc;
    context = contx;
    workerId = workerIndex;
    workerTotal = totalWorkers;
    txIndex = 0;
    numAccounts = args.numAccounts;
    roundId = args.round;
    
    ccId = args.chaincodeId;
    ccVersion = args.chaincodeVersion;
    timeoutSeconds = args.timeoutSeconds;
};

function getRandomIndex(numAccounts) {
    // --rand--> [0,1) --*--> [0, length) --floor-- > [0, length-1]
    return Math.floor(Math.random() * numAccounts);
}

function getPocketNumber(txIndex) {
    return (txIndex % 3)+1;
}

function getPocketTxtype(txIndex) {
    let res = "transactWithPocket";
//    if(txIndex%2===0){
//        res = "transactWithPocket23";
//    }
    return (res+getPocketNumber(txIndex)).toString();
}
function getPocketTxArgs(txIndex) {
    let res = [(getRandomIndex(numAccounts)).toString(), '0.0'];
    if(txIndex%2===0){
        res = [(getRandomIndex(numAccounts)).toString(), '0.0', '0.0'];
    }
    return res;
}

module.exports.run = async () => {
    const currentIndex = txIndex;
    txIndex += 1;

    const invokeArgs = {
        chaincodeFunction: getPocketTxtype(txIndex).toString(),
        chaincodeArguments: getPocketTxArgs(txIndex),
        invokerIdentity: 'User1@fi'
    };
    let results = await blockchain.invokeSmartContract(context, ccId, ccVersion, invokeArgs, timeoutSeconds);
    
    let result = results[0];
    const transferSuccess = result.IsCommitted();

    for (let result of results) {
        let txinfo = {
            tx_id: result.GetID(),
            time_create: result.GetTimeCreate(),
            time_end: result.GetTimeFinal(),
            status: result.GetStatus(),
            function: roundId,
            invoked_func: getPocketTxtype(txIndex).toString(),
            worker_index: workerId,
	        cc: ccId,
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

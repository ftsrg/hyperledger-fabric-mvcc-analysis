'use strict';

const { CaliperUtils } = require('@hyperledger/caliper-core');
const txLogger = CaliperUtils.getLogger('txinfo');

let blockchain;
let context;
let workerId;
let workerTotal;
let txIndex;

let allowanceAmountStr;
let msp;
let invoker;
let ccId;
let ccVersion;
let timeoutSeconds;

module.exports.info  = 'Caliper workload module for setting CBDC allowance of FIs';

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

    msp = args.msp;
    allowanceAmountStr = args.allowance.toString();
    invoker = args.invoker;
};

async function setAllowance(txIndex) {
    const invokeArgs = {
        chaincodeFunction: 'setMintingAllowance',
        chaincodeArguments: [msp, allowanceAmountStr],
        invokerIdentity: invoker
    };
    let results = await blockchain.invokeSmartContract(context, ccId, ccVersion, invokeArgs, timeoutSeconds);
    for (let result of results) {
        let txinfo = {
            tx_id: result.GetID(),
            time_create: result.GetTimeCreate(),
            time_end: result.GetTimeFinal(),
            status: result.GetStatus(),
            function: 'set-allowance',
            worker_index: workerId,
            tx_index: txIndex
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
}

async function ping(txIndex) {
    const queryArgs = {
        chaincodeFunction: 'ping',
        chaincodeArguments: []
    };
    let results = await blockchain.querySmartContract(context, ccId, ccVersion, queryArgs, timeoutSeconds);
    for (let result of results) {
        let txinfo = {
            tx_id: result.GetID(),
            time_create: result.GetTimeCreate(),
            time_end: result.GetTimeFinal(),
            status: result.GetStatus(),
            function: 'ping',
            worker_index: workerId,
            tx_index: txIndex
        };

        txLogger.info(JSON.stringify(txinfo));
    }

    return results;
}

module.exports.run = async () => {
    const currentIndex = txIndex;
    txIndex += 1;

    if (currentIndex === 0) {
        return await setAllowance(currentIndex);
    } else {
        return await ping(currentIndex);
    }
};

module.exports.end = async () => {};
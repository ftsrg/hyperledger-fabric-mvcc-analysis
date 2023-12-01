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


module.exports.run = async () => {
    const currentIndex = txIndex;
    txIndex += 1;

    const invokeArgs = {
        chaincodeFunction: 'demoAssetExists',
        chaincodeArguments: ['0'],
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
            worker_index: workerId,
	        cc:ccId,
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

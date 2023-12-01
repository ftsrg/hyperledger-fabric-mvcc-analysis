'use strict';

const { CaliperUtils } = require('@hyperledger/caliper-core');
const {getAccountManager} = require('./account-manager');
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
let accountManager;
let transferAmountStr;

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

    transferAmountStr = args.transfer.toString();

    accountManager = getAccountManager();
};

async function getNonce(address) {
    const invokeArgs = {
        chaincodeFunction: 'getNonce',
        chaincodeArguments: [address],
        invokerIdentity: 'User1@fi'
    };
    let results = await blockchain.querySmartContract(context, ccId, ccVersion, invokeArgs, timeoutSeconds);
    const res = results[0].GetResult();
    logger.info(`getNonce query result: ${res} | ${res.toString()} | ${res.toString('utf8')}`);
    return res.toString('utf8');
}

module.exports.run = async () => {
    const currentIndex = txIndex;
    txIndex += 1;

    const invokeArgs = {
        chaincodeFunction: 'transfer',
        chaincodeArguments: accountManager.getTransferArgs(transferAmountStr),
        invokerIdentity: 'User1@fi',
        countAsLoad: false
    };
    let results = await blockchain.invokeSmartContract(context, ccId, ccVersion, invokeArgs, timeoutSeconds);
    
    let result = results[0];
    const transferSuccess = result.IsCommitted();
    // if (!transferSuccess) {
    //     const address = invokeArgs.chaincodeArguments[0];
    //     const nonceStr = await getNonce(address);
    //     accountManager.updateNonce(address, nonceStr);
    // }

    accountManager.transferFinished(invokeArgs.chaincodeArguments[0], transferSuccess);

    for (let result of results) {
        let txinfo = {
            tx_id: result.GetID(),
            time_create: result.GetTimeCreate(),
            time_end: result.GetTimeFinal(),
            status: result.GetStatus(),
            function: 'transfer-units',
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
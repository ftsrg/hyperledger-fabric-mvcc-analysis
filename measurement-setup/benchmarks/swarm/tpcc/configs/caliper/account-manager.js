'use strict';

const Wallet = require('ethereumjs-wallet');
const Account = require('./account');

class AccountManager {
    constructor() {
        this.accounts = [];
        this.accountsByAddress = {};
        this.inflightTransfersFrom = {};
    }

    createAddressAndGetArgs() {
        const wallet = Wallet.default.generate();

        const sPrivKey = wallet.getPrivateKeyString();
        const sPubKey = wallet.getPublicKeyString();
        const sAddr = wallet.getAddressString();

        const account = new Account(sPubKey, sPrivKey, sAddr, 0);
        this.accounts.push(account);
        this.accountsByAddress[account.getAddress()] = account;

        const address = account.getAddress();
        const initialNonceStr = account.getNextNonce().toString();
        const signature = account.signMessage([address, initialNonceStr]);

        account.increaseNonce();

        return [address, initialNonceStr, signature.v, signature.r, signature.s];
    }

    getMintUnitsArgs(txIndex, amountStr) {
        const accountIndex = txIndex >= this.accounts.length ? 0 : txIndex;
        const account = this.accounts[accountIndex];

        const address = account.getAddress();
        const nextNonceStr = account.getNextNonce().toString();
        const signature = account.signMessage([address, amountStr, nextNonceStr]);

        account.increaseNonce();

        return [address, amountStr, nextNonceStr, signature.v, signature.r, signature.s];
    }

    _getRandomIndex() {
        // --rand--> [0,1) --*--> [0, length) --floor-- > [0, length-1]
        return Math.floor(Math.random() * this.accounts.length);
    }

    getIdleAccount() {
        let tries = 1;
        while (true) {
            const account = this.accounts[this._getRandomIndex()];
            const address = account.getAddress();
            if (this.inflightTransfersFrom[address]) {
                if (tries >=5) {
                    this.accounts[this._getRandomIndex()];
                }

                tries +=1;
                continue;
            }

            return account;
        }
    }

    getTransferArgs(amountStr) {
        const fromAccount = this.getIdleAccount();
        this.inflightTransfersFrom[fromAccount.address] = true;
        const toAccount = this.getIdleAccount();

        const fromAddress = fromAccount.getAddress();
        const toAddress = toAccount.getAddress();
        const nextNonceStr = fromAccount.getNextNonce().toString();
        const signature = fromAccount.signMessage([fromAddress, toAddress, amountStr, nextNonceStr]);

        return [fromAddress, toAddress, amountStr, nextNonceStr, signature.v, signature.r, signature.s];
    }

    transferFinished(fromAddress, success) {
        this.inflightTransfersFrom[fromAddress] = false;
        
        if (success) {
            this.accountsByAddress[fromAddress].increaseNonce();
        }
    }

    updateNonce(address, currentNonceStr) {
        const account = this.accountsByAddress[address];
        const nextNonce = Number.parseInt(currentNonceStr) + 1;
        account.setNextNonce(nextNonce);
    }
}

let accountManager;

function createAccountManager() {
    accountManager = new AccountManager();
}

function getAccountManager() {
    return accountManager;
}

module.exports.createAccountManager = createAccountManager;
module.exports.getAccountManager = getAccountManager;
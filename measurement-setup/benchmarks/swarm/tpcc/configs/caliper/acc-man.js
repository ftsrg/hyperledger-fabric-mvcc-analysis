'use strict';

class AccountManager {
    constructor() {
        this.accounts = [];
    }

    _getRandomInt(min, max) {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min) + min); // The maximum is exclusive and the minimum is inclusive
    }

    getAccountCreateArgs(){
        const uuid = this._getRandomInt(0,99999);
	this.accounts.push(uuid);
	return [uuid, 100000,100000,100000];
    }

    getPocketTransactArgs(){
        const uuid = this.accounts[this._getRandomIndex()];
	const amount = this._getRandomInt(-10,+10);
	return {uuid, amount};
    }

    _getRandomIndex() {
        // --rand--> [0,1) --*--> [0, length) --floor-- > [0, length-1]
        return Math.floor(Math.random() * this.accounts.length);
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

'use strict';

const ethUtil = require('ethereumjs-util');

class Account {
    constructor(publicKey, privateKey, address, initialNonce) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.address = address;
        this.nextNonce = initialNonce;

        if (privateKey.startsWith('0x')) {
            privateKey = privateKey.substring(2);
        }

        this.keyBuffer = Buffer.from(privateKey, 'hex');
    }

    signMessage(messageParts) {
        const message = Buffer.from(messageParts.join(' '));
        const messageHash = ethUtil.hashPersonalMessage(message);
        const signature = ethUtil.ecsign(messageHash, this.keyBuffer);

        return {
            v: signature.v.toString(),
            r: signature.r.toString('base64'),
            s: signature.s.toString('base64')
        };
    }

    getAddress() {
        return this.address;
    }

    getNextNonce() {
        return this.nextNonce;
    }

    increaseNonce() {
        this.nextNonce += 1;
    }

    setNextNonce(nextNonce) {
        this.nextNonce = nextNonce;
    }
}



module.exports = Account;
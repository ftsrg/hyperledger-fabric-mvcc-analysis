package org.example.exceptions;

import org.hyperledger.fabric.shim.ChaincodeException;

public class NonExistentAssetException extends ChaincodeException {
    public NonExistentAssetException(String uuid) {
        super(String.format("Asset with uuid %s doesn't exist", uuid));
    }
}
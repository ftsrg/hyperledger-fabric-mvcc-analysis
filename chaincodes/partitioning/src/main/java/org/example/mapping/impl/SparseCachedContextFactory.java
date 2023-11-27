package org.example.mapping.impl;

import org.hyperledger.fabric.shim.ChaincodeStub;

public final class SparseCachedContextFactory {
    private static SparseCachedContextFactory cf;

    public static synchronized SparseCachedContextFactory getInstance() {
        if (cf == null) {
            cf = new SparseCachedContextFactory();
        }
        return cf;
    }

    public SparseCachedContext createContext(final ChaincodeStub stub) {
        return new SparseCachedContext(stub);
    }

}

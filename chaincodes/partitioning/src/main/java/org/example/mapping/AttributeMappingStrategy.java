package org.example.mapping;

import java.util.Map;

import org.example.asset.AssetBase;
import org.example.exceptions.JsonProcessingFailureException;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public interface AttributeMappingStrategy {

    public Map<String, String> mapAttributesToJsonObjects(Class<?> clazz, AssetBase object)
            throws JsonProcessingFailureException;

    public CompositeKey mapAttributeToCompositeKey(Class<?> clazz, AssetBase asset,
            String attributeName)
            throws NoSuchFieldException, SecurityException;

}

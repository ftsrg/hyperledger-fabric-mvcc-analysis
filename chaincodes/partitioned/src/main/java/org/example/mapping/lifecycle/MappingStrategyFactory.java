package org.example.mapping.lifecycle;

import java.util.logging.Logger;

import org.example.mapping.AttributeMappingStrategy;
import org.example.mapping.impl.SparseAttributeMappingStrategy;
import org.example.utils.LoggerFactory;
import org.hyperledger.fabric.contract.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingStrategyFactory {

    private static MappingStrategyFactory mp;
    private static final Logger LOGGER = LoggerFactory.getLogger(MappingStrategyFactory.class.getName());

    private MappingStrategyFactory() {
    }

    public static synchronized MappingStrategyFactory getInstance() {
        if (mp == null) {
            mp = new MappingStrategyFactory();
        }
        return mp;
    }

    public AttributeMappingStrategy createMappingStrategy(Context ctx, ObjectMapper mapper) {
        LOGGER.finest("create mappingstrategy called");
        return new SparseAttributeMappingStrategy(mapper,ctx);
    }

}

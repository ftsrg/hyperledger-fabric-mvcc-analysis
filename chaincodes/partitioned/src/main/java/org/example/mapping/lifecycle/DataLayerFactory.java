package org.example.mapping.lifecycle;

import java.util.logging.Logger;

import org.example.mapping.AttributeMappingStrategy;
import org.example.mapping.DataLayer;
import org.example.mapping.impl.SparseDataLayer;
import org.example.utils.LoggerFactory;
import org.hyperledger.fabric.contract.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DataLayerFactory {

    private static DataLayerFactory dlf;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataLayerFactory.class.getName());

    private DataLayerFactory() {
    }

    public static synchronized DataLayerFactory getInstance() {
        if (dlf == null) {
            dlf = new DataLayerFactory();
        }
        return dlf;
    }

    public DataLayer createDataLayer(AttributeMappingStrategy mappingStrategy, ObjectMapper mapper, Context context) {
        LOGGER.finest("get current ObjectMapper called");
        return new SparseDataLayer(mappingStrategy ,mapper, context);
    }
    
}

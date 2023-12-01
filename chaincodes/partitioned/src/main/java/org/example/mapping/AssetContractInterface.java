package org.example.mapping;

import java.util.logging.Logger;

import org.example.mapping.impl.SparseCachedContextFactory;
import org.example.exceptions.ExceptionLogger;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.mapping.lifecycle.DataLayerFactory;
import org.example.mapping.lifecycle.MappingStrategyFactory;
import org.example.mapping.lifecycle.ObjectMapperFactory;
import org.example.utils.LoggerFactory;
import org.example.utils.TraceLogger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import com.fasterxml.jackson.databind.ObjectMapper;

// extend this interface to inject a a subclass of Context that implements
// an AssetCache
public interface AssetContractInterface extends ContractInterface {

    public static final Logger LOGGER = LoggerFactory.getLogger(AssetContractInterface.class.getName());
    

    @Override
    default void beforeTransaction(Context ctx) {
        LOGGER.info("beforeTransaction called");
        if (ctx instanceof AssetCache) {
            LOGGER.info("found instance of AssetCache");
            AssetCache cache = (AssetCache) ctx;
            ObjectMapper om = ObjectMapperFactory.getInstance().createObjectMapper();
            AttributeMappingStrategy ams = MappingStrategyFactory.getInstance().createMappingStrategy(ctx,om);
            DataLayer dl = DataLayerFactory.getInstance().createDataLayer(ams, om, ctx);
            TraceLogger tlog = new TraceLogger(ctx.getStub().getTxId(),"txinfo");
            cache.init(ams,dl,om,tlog);
        }
        ContractInterface.super.beforeTransaction(ctx);
        LOGGER.info("beforeTransaction exited");
    }

    @Override
    default public void afterTransaction(Context ctx, Object result) {
        LOGGER.info("afterTransaction called");
        if (!(ctx instanceof AssetCache)) {
            LOGGER.warning("No instance of AssetCache could be found");
            ContractInterface.super.afterTransaction(ctx, result);
            return;
        }
        AssetCache cache = (AssetCache) ctx;
        try {
            cache.persistCacheOnChain();
        } catch (JsonProcessingFailureException e) {
            ExceptionLogger.log(LOGGER, e);
            throw new ChaincodeException(e);
        }
        cache.invalidateCache();
        cache.getLogger().finalizeTrace();
        ContractInterface.super.afterTransaction(ctx, result);
        LOGGER.info("afterTransaction exited");
    }

    @Override
    default Context createContext(ChaincodeStub stub) {
        return SparseCachedContextFactory.getInstance().createContext(stub);
    }

}

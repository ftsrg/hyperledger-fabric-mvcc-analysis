package org.example.demo;

import java.util.HashMap;
import java.util.logging.Logger;

import org.example.asset.AssetBase;
import org.example.asset.annotation.AssetType;
import org.example.asset.annotation.Attribute;
import org.example.exceptions.ExceptionLogger;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.mapping.impl.SparseCachedContext;
import org.example.utils.LoggerFactory;
import org.hyperledger.fabric.shim.ChaincodeException;

@AssetType(type = "DemoAsset")
public class DemoGeneratedAsset extends AssetBase {

    @Attribute
    public Double pocket1;

    @Attribute
    public HashMap<String, Double> pocket23;

    protected static Logger LOGGER = LoggerFactory.getLogger(DemoGeneratedAsset.class.getName());

    DemoGeneratedAsset() {
        super();
    }

    DemoGeneratedAsset(SparseCachedContext ctx, String uuid) {
        super(ctx, uuid);
    }

    DemoGeneratedAsset(AssetBase nestHost) {
        super(nestHost);
    }

    public Double getPocket1Attribute() {
        try {
            Double res = dataLayer.getAttribute(DemoAsset.class, this, "pocket1", () -> pocket1, null);
            LOGGER.finest("got pocket1 attrib successfully");
            return res;
        } catch (NoSuchFieldException | SecurityException | JsonProcessingFailureException e) {
            ExceptionLogger.log(LOGGER, e);
            throw new ChaincodeException(e);
        }
    }

    public HashMap<String,Double> getPocket23Attribute() {
        try {
            return dataLayer.getAttribute(DemoAsset.class, this, "pocket23", () -> pocket23, null);
        } catch (NoSuchFieldException | SecurityException | JsonProcessingFailureException e) {
            ExceptionLogger.log(LOGGER, e);
            throw new ChaincodeException(e);
        }
    }

    public void setPocket1Attribute(Double amount) {
        dataLayer.setAttribute(DemoAsset.class, this, amount, "pocket1", (val) -> this.pocket1 = val);
    }

    public void setPocket23Attribute(Double amount2,Double amount3) {
        HashMap<String, Double> newMap = new HashMap<>();
        newMap.put("pocket2", amount2);
        newMap.put("pocket3", amount3);
        dataLayer.setAttribute(DemoAsset.class, this, newMap , "pocket23",
                (val) -> this.pocket23 = val);
    }
}

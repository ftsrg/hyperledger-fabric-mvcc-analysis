/*
* SPDX-License-Identifier: Apache-2.0
*/
package org.example.demo;

import java.util.logging.Logger;

import org.example.exceptions.ExceptionLogger;
import org.example.mapping.AssetContractInterface;
import org.example.mapping.lifecycle.ObjectMapperFactory;
import org.example.utils.LoggerFactory;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;

@Contract(name = "DemoContract", info = @Info(title = "AssetBase contract", description = "My Smart Contract", version = "0.0.1", license = @License(name = "Apache-2.0", url = ""), contact = @Contact(email = "data-access-demo-hlf@example.com", name = "data-access-demo-hlf", url = "http://data-access-demo-hlf.me")))
@Default
public class DemoContract implements AssetContractInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoContract.class.getName());
    private static final DemoAssetRepository repo = DemoAssetRepository.getInstance();

    public DemoContract() {
    }

    @Transaction()
    public boolean demoAssetExists(Context ctx, String uuid) {
        LOGGER.info("demoAssetExists called");
        return repo.demoAssetExists(ctx, uuid);
    }

    @Transaction()
    public void createDemoAsset(Context ctx, String uuid, Double pocket1, Double pocket2, Double pocket3) {
        repo.createDemoAsset(ctx, uuid, pocket1, pocket2, pocket3);
    }

    @Transaction()
    public void transactWithPocket1(Context ctx, String uuid, Double amount) {
        DemoAsset asset = repo.readDemoAsset(ctx, uuid);
        if (asset.getPocket1Attribute() + amount < 0) {
            throw new ChaincodeException("Not enough funds in pocket1 to finish transaction");
        }
        asset.setPocket1Attribute(asset.getPocket1Attribute() + amount);
    }

    @Transaction()
    public void transactWithPocket23(Context ctx, String uuid, Double amount2, Double amount3) {
        DemoAsset asset = repo.readDemoAsset(ctx, uuid);
        if (asset.getPocket23Attribute().get("pocket3") + amount3 < 0 && asset.getPocket23Attribute().get("pocket2") + amount2 < 0) {
            throw new ChaincodeException("Not enough funds in pocket3 or pocket2 to finish transaction");
        }
        asset.setPocket23Attribute(asset.getPocket23Attribute().get("pocket2") + amount2,asset.getPocket23Attribute().get("pocket3") + amount3);
    }

    @Transaction()
    public String readDemoAsset(Context ctx, String uuid) {
        DemoAsset res = repo.readDemoAsset(ctx, uuid);
        res.getPocket1Attribute();
        res.getPocket23Attribute().get("pocket3");

        LOGGER.info(String.format("readDemoAsset returning with read asset %s", res.getUuid()));
        String resStr = null;
        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(new DemoAssetSerializer(DemoAsset.class));
            ObjectMapper mapper = ObjectMapperFactory.getInstance().createObjectMapper();
            mapper.registerModule(module);
            resStr = mapper.writeValueAsString(res);
        } catch (JsonProcessingException e) {
            LOGGER.severe(String.format("Could not serialilze asset with uuid %s",
                    res.getUuid()));
            ExceptionLogger.log(LOGGER, e);
        }
        return resStr;
    }

    @Transaction()
    public void deleteDemoAsset(Context ctx, String uuid) {
        repo.deleteDemoAsset(ctx, uuid);
    }

}

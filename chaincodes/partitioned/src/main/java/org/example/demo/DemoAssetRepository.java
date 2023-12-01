/*
* SPDX-License-Identifier: Apache-2.0
*/
package org.example.demo;

import java.util.logging.Logger;

import org.example.exceptions.ExceptionLogger;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.mapping.impl.SparseCachedContext;
import org.example.utils.LoggerFactory;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;

public class DemoAssetRepository{
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAssetRepository.class.getName());
    private static DemoAssetRepository repository = null;

    private DemoAssetRepository() {
    }
    public static DemoAssetRepository getInstance(){
        if(DemoAssetRepository.repository == null){
            DemoAssetRepository.repository = new DemoAssetRepository();
        }
        return DemoAssetRepository.repository;
    }

    public boolean demoAssetExists(Context ctx, String uuid) {
        LOGGER.finest(String.format("demoAssetExists called for asset %s", uuid));
        SparseCachedContext cctx = (SparseCachedContext) ctx;
        return cctx.getDataLayer().assetExists(DemoAsset.class, uuid);
    }

    public DemoAsset createDemoAsset(Context ctx, String uuid, Double pocket1, Double pocket2, Double pocket3) {
        LOGGER.finest(String.format("createDemoAsset called for asset %s and value %s", uuid, pocket1));
        SparseCachedContext cctx = (SparseCachedContext) ctx;
        boolean exists = demoAssetExists(ctx, uuid);
        if (exists) {
            throw new ChaincodeException("The asset " + uuid + " already exists");
        }

        DemoAsset asset = cctx.getDataLayer().createAsset(DemoAsset.class, uuid, DemoAsset::fromAssetBase);
        asset.setPocket1Attribute(pocket1);
        asset.setPocket23Attribute(pocket2,pocket3);
        // LOGGER.finest("pocket1 on asset is: " + asset.pocket1);
        // LOGGER.finest("pocket2 on asset is: " + asset.pocket2);
        // LOGGER.finest("pocket3 on asset is: " + asset.pocket3);
        return asset;
    }

    public DemoAsset readDemoAsset(Context ctx, String uuid) {
        LOGGER.finest(String.format("readDemoAsset called for asset %s", uuid));
        SparseCachedContext cctx = (SparseCachedContext) ctx;
        boolean exists = demoAssetExists(ctx, uuid);
        if (!exists) {
            throw new ChaincodeException("The asset " + uuid + " does not exist");
        }
        DemoAsset res = cctx.getDataLayer().readAsset(DemoAsset.class, uuid, DemoAsset::fromAssetBase, null);
        return res;
    }

    public void updateDemoAsset(Context ctx, String uuid, Double pocket1, Double pocket2, Double pocket3) {
        LOGGER.finest(String.format("updateDemoAsset called for asset %s and newValue %s", uuid, pocket1));
        boolean exists = demoAssetExists(ctx, uuid);
        if (!exists) {
            throw new ChaincodeException("The asset " + uuid + " does not exist");
        }
        //TODO add abstraction layer
        DemoAsset asset = new DemoAsset((SparseCachedContext)ctx, uuid);
        if (pocket1 != null) {
            asset.setPocket1Attribute(pocket1);
            LOGGER.finest(String.format("updated asset %s with pocket1 value %s", uuid, asset.pocket1));
        }
        if (pocket2 != null) {
            asset.setPocket23Attribute(pocket2, asset.getPocket23Attribute().get("pocket3"));
            LOGGER.finest(String.format("updated asset %s with pocket2 value %s", uuid, asset.pocket23.get("pocket2")));
        }
        if (pocket3 != null) {
            asset.setPocket23Attribute(asset.getPocket23Attribute().get("pocket2"), pocket3);
            LOGGER.finest(String.format("updated asset %s with pocket3 value %s", uuid, asset.pocket23.get("pocket2")));
        }
    }

    public void deleteDemoAsset(Context ctx, String uuid) {
        LOGGER.finest(String.format("deleteDemoAsset called for asset %s", uuid));
        SparseCachedContext cctx = (SparseCachedContext) ctx;
        boolean exists = demoAssetExists(ctx, uuid);
        if (!exists) {
            throw new ChaincodeException("The asset " + uuid + " does not exist");
        }
        try {
            cctx.getDataLayer().deleteAsset(DemoAsset.class, uuid);
        } catch (JsonProcessingFailureException e) {
            LOGGER.severe("Failed to process json");
            ExceptionLogger.log(LOGGER, e);
            throw new ChaincodeException(e);
        }
        LOGGER.finest(String.format("deleted asset %s", uuid));
    }

}

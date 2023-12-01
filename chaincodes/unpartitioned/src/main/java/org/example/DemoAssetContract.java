/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "DemoAssetContract", info = @Info(title = "DemoAsset contract", description = "My Smart Contract", version = "0.0.1", license = @License(name = "Apache-2.0", url = ""), contact = @Contact(email = "naive-contract@example.com", name = "naive-contract", url = "http://naive-contract.me")))
@Default
public class DemoAssetContract implements ContractInterface {
    public DemoAssetContract() {

    }

    @Transaction()
    public boolean demoAssetExists(Context ctx, String demoAssetId) {
        byte[] buffer = ctx.getStub().getState(demoAssetId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void createDemoAsset(Context ctx, String uuid, Double pocket1, Double pocket2, Double pocket3) {
        boolean exists = demoAssetExists(ctx, uuid);
        if (exists) {
            throw new RuntimeException("The asset " + uuid + " already exists");
        }
        DemoAsset asset = new DemoAsset();
        asset.setUuid(uuid);
        asset.setPocket1(pocket1);
        asset.setPocket2(pocket2);
        asset.setPocket3(pocket3);
        ctx.getStub().putState(uuid, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public DemoAsset readDemoAsset(Context ctx, String demoAssetId) {
        boolean exists = demoAssetExists(ctx, demoAssetId);
        if (!exists) {
            throw new RuntimeException("The asset " + demoAssetId + " does not exist");
        }

        DemoAsset newAsset = DemoAsset.fromJSONString(new String(ctx.getStub().getState(demoAssetId), UTF_8));
        return newAsset;
    }

    @Transaction()
    public void transactWithPocket1(Context ctx, String uuid, Double pocket1) {
        DemoAsset asset = readDemoAsset(ctx, uuid);
        if ((asset.getPocket1() + pocket1) >= 0) {
            updateDemoAsset(ctx, uuid, asset.getPocket1() + pocket1, null, null);
        }
    }

    @Transaction()
    public void transactWithPocket2(Context ctx, String uuid, Double pocket2) {
        DemoAsset asset = readDemoAsset(ctx, uuid);
        if ((asset.getPocket2() + pocket2) >= 0) {
            updateDemoAsset(ctx, uuid, null, asset.getPocket2() + pocket2, null);
        }
    }

    @Transaction()
    public void transactWithPocket3(Context ctx, String uuid, Double pocket3) {
        DemoAsset asset = readDemoAsset(ctx, uuid);
        if ((asset.getPocket3() + pocket3) >= 0) {
            updateDemoAsset(ctx, uuid, null, null, asset.getPocket3() + pocket3);
        }
    }

    @Transaction()
    public void transactWithPocket23(Context ctx, String uuid, Double pocket2, Double pocket3) {
        DemoAsset asset = readDemoAsset(ctx, uuid);
        if ((asset.getPocket3() + pocket3) >= 0 && (asset.getPocket2() + pocket2) >= 0) {
            updateDemoAsset(ctx, uuid, null, asset.getPocket2() + pocket2, asset.getPocket3() + pocket3);
        }
    }

    @Transaction()
    public void updateDemoAsset(Context ctx, String uuid, Double pocket1, Double pocket2, Double pocket3) {
        boolean exists = demoAssetExists(ctx, uuid);
        if (!exists) {
            throw new RuntimeException("The asset " + uuid + " does not exist");
        }
        DemoAsset asset = readDemoAsset(ctx, uuid);
        if (pocket1 != null) {
            asset.setPocket1(pocket1);
        }
        if (pocket2 != null) {
            asset.setPocket2(pocket2);
        }
        if (pocket3 != null) {
            asset.setPocket3(pocket3);
        }

        ctx.getStub().putState(uuid, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public void deleteDemoAsset(Context ctx, String demoAssetId) {
        boolean exists = demoAssetExists(ctx, demoAssetId);
        if (!exists) {
            throw new RuntimeException("The asset " + demoAssetId + " does not exist");
        }
        ctx.getStub().delState(demoAssetId);
    }

}

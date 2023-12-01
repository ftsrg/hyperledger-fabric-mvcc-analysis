/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;

@DataType()
public class DemoAsset {

    private final static Genson genson = new Genson();

    @Property()
    private String uuid;

    @Property()
    private Double pocket1;

    @Property()
    private Double pocket2;

    @Property()
    private Double pocket3;
    
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getPocket1() {
        return pocket1;
    }

    public void setPocket1(Double pocket1) {
        this.pocket1 = pocket1;
    }

    public Double getPocket2() {
        return pocket2;
    }

    public void setPocket2(Double pocket2) {
        this.pocket2 = pocket2;
    }

    public Double getPocket3() {
        return pocket3;
    }

    public void setPocket3(Double pocket3) {
        this.pocket3 = pocket3;
    }

    public DemoAsset(){
    }

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public static DemoAsset fromJSONString(String json) {
        DemoAsset asset = genson.deserialize(json, DemoAsset.class);
        return asset;
    }
}

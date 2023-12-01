package org.example.mapping;

import java.util.Arrays;

import org.example.asset.AssetBase;
import org.example.utils.ArrayUtils;

public class CacheObject {

    public AssetBase asset;
    public Class<?> assetType;
    public boolean created;
    public String[] attribsChanged;

    public CacheObject(AssetBase asset, Class<?> assetType, String[] attribs, boolean created) {
        this.asset = asset;
        this.assetType = assetType;
        this.attribsChanged = attribs;
        this.created = created;
    }

    public void update(AssetBase asset, String[] attribs) {
        String[] difference = Arrays.stream(attribs).filter(att -> {
            return !Arrays.stream(attribsChanged).anyMatch(oldAtts -> oldAtts.equals(att));
        }).toArray(String[]::new);
        this.attribsChanged = ArrayUtils.concatArrays(attribsChanged, difference);
        this.asset = asset;
    }
}

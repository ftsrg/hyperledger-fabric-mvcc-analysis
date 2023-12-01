package org.example.asset;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.example.asset.annotation.AssetType;
import org.example.mapping.DataLayer;
import org.example.mapping.impl.SparseCachedContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetBase {

    @JsonIgnore
    private AssetBase nestHost;

    @JsonIgnore
    protected DataLayer dataLayer;

    @JsonIgnore
    private SparseCachedContext context;

    private String uuid;

    public AssetBase() {
    }

    public AssetBase(SparseCachedContext ctx, String uuid) {
        this.context = ctx;
        this.dataLayer = ctx.getDataLayer();
        this.uuid = uuid;
    }

    public AssetBase(AssetBase nestHost) {
        this.nestHost = nestHost;
        String rootUuid = nestHost.findRootAsset().getUuid();
        this.uuid = nestHost.getAssetType().orElseThrow(()->new NoSuchElementException()).type() + rootUuid;
        this.context = nestHost.context;
        this.dataLayer = nestHost.dataLayer;
    }

    public SparseCachedContext getContext() {
        return context;
    }

    public String getUuid() {
        return this.uuid;
    }

    @JsonIgnore
    public Optional<AssetBase> getNestHost() {
        return Optional.ofNullable(this.nestHost);
    }

    public AssetBase findRootAsset() {
        if (this.nestHost == null) {
            return this;
        }

        AssetBase res = nestHost;
        AssetBase assetIt = nestHost;
        while (assetIt != null) {
            res = assetIt;
            assetIt = assetIt.getNestHost().orElse(null);
        }
        if (res == null)
            throw new RuntimeException("Rootasseterr");
        return res;
    }

    @JsonIgnore
    public Optional<AssetType> getAssetType() {
        return Optional.ofNullable(this.getClass().getAnnotation(AssetType.class));
    }

}

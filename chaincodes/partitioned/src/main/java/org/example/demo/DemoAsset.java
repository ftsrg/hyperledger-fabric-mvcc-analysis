package org.example.demo;

import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.example.asset.AssetBase;
import org.example.asset.annotation.AssetType;
import org.example.mapping.impl.SparseCachedContext;
import org.example.utils.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@AssetType(type = "DemoAsset")
@JsonSerialize(using = DemoAssetSerializer.class)
public class DemoAsset extends DemoGeneratedAsset {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAsset.class.getName());

    public DemoAsset(AssetBase nestHost) {
        super(nestHost);
    }

    public DemoAsset(SparseCachedContext ctx, String uuid) {
        super(ctx, uuid);
    }

    public static DemoAsset fromAssetBase(AssetBase base) {
        LOGGER.finest(String.format("Transforming AssetBase asset %s to DemoAsset", base.getUuid()));
        if (base.getNestHost().isPresent()) {
            LOGGER.finest(String.format("Base asset %s IS nested", base.getUuid()));
            return new DemoAsset(base.getNestHost().orElseThrow(() -> new NoSuchElementException()));
        }
        LOGGER.finest(String.format("Base asset %s IS NOT nested", base.getUuid()));
        return new DemoAsset(base.getContext(),base.getUuid());
    }
}

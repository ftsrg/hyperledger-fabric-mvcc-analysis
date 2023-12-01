package org.example.mapping;

import java.util.Optional;

import org.example.asset.AssetBase;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.utils.TraceLogger;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface AssetCache {
    public void cacheAsset(Class<?> clazz, AssetBase asset, String[] attribs, boolean created);
    public void updateCache(String uuid, AssetBase asset, String[] attribsChanged);
    public void persistCacheOnChain() throws JsonProcessingFailureException;
    public void invalidateCache();
    public void removeAssetFromCache(String uuid);
    public boolean isCacheMiss(String uuid);
    public Optional<AssetBase> getCachedAsset(String uuid);
    public void init(AttributeMappingStrategy attmapper, DataLayer dataLayer, ObjectMapper objMapper, TraceLogger logger);
    public TraceLogger getLogger();
}

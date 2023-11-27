package org.example.mapping.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.example.asset.AssetBase;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.mapping.AssetCache;
import org.example.mapping.AttributeMappingStrategy;
import org.example.mapping.CacheObject;
import org.example.mapping.DataLayer;
import org.example.utils.LoggerFactory;
import org.example.utils.TraceLogger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SparseCachedContext extends Context implements AssetCache {

    private DataLayer dataLayer;
    private AttributeMappingStrategy mapper;
    private ObjectMapper objectMapper;
    private LinkedHashMap<String, CacheObject> cache;
    private TraceLogger traceLogger;
    private static Logger LOGGER = LoggerFactory.getLogger(SparseCachedContext.class.getName());

    public SparseCachedContext(ChaincodeStub stub) {
        super(stub);
        this.cache = new LinkedHashMap<>();
    }

    public DataLayer getDataLayer() {
        return dataLayer;
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void init(AttributeMappingStrategy attMapper, DataLayer dataLayer, ObjectMapper objMapper, TraceLogger logger) {
        this.mapper = attMapper;
        this.dataLayer = dataLayer;
        this.objectMapper = objMapper;
        this.traceLogger = logger;
    }

    @Override
    public void updateCache(String uuid, AssetBase asset, String[] attribsChanged) {
        if (!cache.containsKey(uuid))
            throw new IllegalArgumentException(String.format("No cached asset with uuid %s", uuid));
        cache.get(uuid).update(asset, attribsChanged);
    }

    @Override
    public boolean isCacheMiss(String key) {
        return !cache.containsKey(key);
    }

    @Override
    public void cacheAsset(Class<?> clazz, AssetBase asset, String[] attribs, boolean created) {
        if (cache.containsKey(asset.getUuid())) {
            throw new IllegalArgumentException(String.format("Asset %s is already in cache", asset.getUuid()));
        }
        cache.put(asset.getUuid(), new CacheObject(asset, clazz, attribs, created));
        LOGGER.finest(String.format("Cached asset %s", asset.getUuid()));
    }

    @Override
    public Optional<AssetBase> getCachedAsset(String uuid) {
        if (isCacheMiss(uuid))
            return Optional.empty();
        return Optional.of(cache.get(uuid).asset);
    }

    @Override
    public void persistCacheOnChain() throws JsonProcessingFailureException {
        LOGGER.finest("Writing cached objects to the ledger");
        try {
            cache.values().stream().forEach(cacheObj -> {
                LOGGER.finest(String.format("Persisting asset %s...", cacheObj.asset.getUuid()));
                try {
                    Map<String, String> assetMapped = mapper.mapAttributesToJsonObjects(cacheObj.assetType,
                            cacheObj.asset);
                    if (cacheObj.created) {
                        LOGGER.finest("Persisting newly created asset on chain...");
                        assetMapped.forEach((k, v) -> {
                            LOGGER.finest(String.format("writing %s's value on chain", k));
                            stub.putState(k, v.getBytes(UTF_8));
                        });
                        LOGGER.finest(
                                String.format("Writing newly created asset %s to ledger", cacheObj.asset.getUuid()));
                    } else {
                        LOGGER.finest("Persisting updates on existing assets...");
                        String[] attribKeys = Arrays.stream(cacheObj.attribsChanged).map(att -> {
                            LOGGER.finest(String.format("Attribute %s was changed on %s", att, cacheObj.asset.getUuid()));
                            try {
                                return mapper.mapAttributeToCompositeKey(cacheObj.assetType, cacheObj.asset, att)
                                        .toString();
                            } catch (NoSuchFieldException | SecurityException e) {
                                throw new RuntimeException(e);
                            }
                        }).toArray(String[]::new);
                        assetMapped.forEach((k, v) -> {
                            if (Arrays.stream(attribKeys).anyMatch(key -> k.equals(key))){
                                LOGGER.finest(String.format("Writing asset %s's changed %s attrib to ledger",
                                        cacheObj.asset.getUuid(), k));
                                stub.putState(k, v.getBytes(UTF_8));
                            }
                        });

                    }
                    LOGGER.finest(String.format("Persisted asset %s", cacheObj.asset.getUuid()));
                } catch (JsonProcessingFailureException e) {
                    throw new RuntimeException(e);
                }

            });
        } catch (RuntimeException e) {
            throw new JsonProcessingFailureException(e);
        }
        LOGGER.finest("Finished writing cached objects to the ledger");
    }

    @Override
    public void invalidateCache() {
        LOGGER.finest("Invalidating cache");
        cache.clear();
    }

    @Override
    public void removeAssetFromCache(String uuid) {
        cache.remove(uuid);
    }

    @Override
    public TraceLogger getLogger() {
        return traceLogger;
    }
}

package org.example.mapping.impl;

import org.example.asset.AssetBase;
import org.example.asset.annotation.AssetType;
import org.example.asset.annotation.Attribute;
import org.example.exceptions.ExceptionLogger;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.exceptions.MissingAnnotationException;
import org.example.exceptions.NonExistentAssetException;
import org.example.functions.AssetTransformFunc;
import org.example.functions.GetterFunc;
import org.example.functions.SetterFunc;
import org.example.mapping.AttributeMappingStrategy;
import org.example.mapping.DataLayer;
import org.example.utils.LoggerFactory;
import org.example.utils.ReflectionUtils;
import org.example.utils.TraceLogger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class SparseDataLayer implements DataLayer {

    private AttributeMappingStrategy mapper;
    private ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(SparseDataLayer.class.getName());
    private SparseCachedContext ctx;

    public SparseDataLayer(AttributeMappingStrategy mappingStrategy, ObjectMapper mapper, Context context) {
        if (!(context instanceof SparseCachedContext)) {
            throw new IllegalArgumentException("Provided context is not an instane of SparseCachedContext");
        }
        this.mapper = mappingStrategy;
        this.objectMapper = mapper;
        this.ctx = (SparseCachedContext) context;
    }

    // returns true if the asset exists on chain
    @Override
    public boolean assetExists(Class<?> clazz, String uuid) {
        LOGGER.finest(String.format("assetExists called with args %s, %s", clazz.getName(), uuid));
        if (ctx.isCacheMiss(uuid)) {
            ChaincodeStub stub = ctx.getStub();
            AssetType type = getAssetTypeOrThrow(clazz);

            CompositeKey key = stub.createCompositeKey(type.type().toUpperCase(), uuid);
            byte[] res = stub.getState(key.toString());
            byte[] keyBuffer = key.toString().getBytes(UTF_8);
            traceReadSetSize(keyBuffer, res);

            LOGGER.finest(String.format("Asset %s exists?=%s", uuid, (res != null && res.length > 0)));
            return (res != null && res.length > 0);
        }
        LOGGER.finest(String.format("Asset %s exists", uuid));
        return true;
    }

    // creates an asset in the cache
    // throws ChaincodeException if an asset exists with the same uuid
    @Override
    public <T extends AssetBase> T createAsset(Class<T> clazz, String uuid, AssetTransformFunc<T> transformer) {
        LOGGER.finest(String.format("createAsset called with args %s, %s", clazz.getName(), uuid));
        T newAsset = transformer.transfromFromBase(new AssetBase(ctx, uuid));
        if (assetExists(clazz, uuid))
            throw new ChaincodeException(String.format("Asset with uuid %s already exists", uuid));
        ctx.cacheAsset(clazz, newAsset, new String[] {}, true);
        return transformer.transfromFromBase(newAsset);
    }

    // tries to read an asset's value from the cache if present or from the chain if
    // not.
    // throws NonExistentAssetException if the asset does not exist
    @Override
    public <T extends AssetBase> T readAsset(Class<T> clazz, String uuid, AssetTransformFunc<T> trans,
            AssetBase optionalNesthost) {
        LOGGER.finest(String.format("readAsset called with args %s, %s, %s", clazz.getName(), uuid,
                trans.getClass().getName()));
        if (ctx.isCacheMiss(uuid)) {
            LOGGER.finest(String.format("WAS cache miss"));
            if (!assetExists(clazz, uuid)) {
                throw new NonExistentAssetException(uuid);
            }

            LOGGER.finest(String.format("readAsset returning with new faux-asset", clazz.getName(), uuid));
            if (optionalNesthost != null) {
                T asset = trans.transfromFromBase(new AssetBase(optionalNesthost));
                ctx.cacheAsset(clazz, asset, new String[] {}, false);
            } else {
                T asset = trans.transfromFromBase(new AssetBase(ctx, uuid));
                ctx.cacheAsset(clazz, asset, new String[] {}, false);
            }
        }
        LOGGER.finest(String.format("readAsset returning with cached asset", clazz.getName(), uuid));
        return trans.transfromFromBase(ctx.getCachedAsset(uuid).orElseThrow(() -> new NoSuchElementException()));
    }

    // overwrites a key's value in the cache
    // throws NonExistentAssetException if the asset does not exist
    @Override
    public void updateAsset(Class<?> clazz, AssetBase asset) {
        String uuid = asset.getUuid();
        if (!assetExists(clazz, uuid))
            throw new NonExistentAssetException(uuid);

        try {
            mapper.mapAttributesToJsonObjects(clazz, asset).forEach((key, value) -> {
                ctx.getStub().putState(key, value.getBytes(UTF_8));
            });
        } catch (JsonProcessingFailureException e) {
            throw new ChaincodeException(
                    String.format("Failed to serialize asset with uuid %s to json", uuid));
        }

    }

    // deletes an asset and all its attributes on chain, including nested assets,
    // by recursively calling deleteByPartialCompKey
    @Override
    public void deleteAsset(Class<?> clazz, String uuid) throws JsonProcessingFailureException {
        ChaincodeStub stub = ctx.getStub();
        AssetType type = getAssetTypeOrThrow(clazz);
        CompositeKey key = stub.createCompositeKey(type.type().toUpperCase(), uuid);

        deleteByPartialCompKey(key.toString());

        ctx.removeAssetFromCache(uuid);
    }

    @Override
    public <T> void setAttribute(Class<?> clazz, AssetBase on, T object, String attributeName, SetterFunc<T> setter) {
        LOGGER.finest(String.format("Setting attribute on Asset %s", on.getUuid()));
        if (!assetExists(clazz, on.getUuid())) {
            LOGGER.severe(String.format("Asset %s does not exist but setAttribute was called on it",
                    on.getUuid()));
            throw new NonExistentAssetException(on.getUuid());
        }
        setter.setAttribute(object);
        if (ctx.isCacheMiss(on.getUuid())) {
            ctx.cacheAsset(clazz, on, new String[] { attributeName }, false);
        } else {
            ctx.updateCache(on.getUuid(), on, new String[] { attributeName });
        }
        LOGGER.finest(String.format("Set attribute on asset %s", on.getUuid()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Class<?> assetClass, AssetBase on, String attributeName,
            GetterFunc<T> getter, AssetTransformFunc<T> trans)
            throws NoSuchFieldException, SecurityException, JsonProcessingFailureException {
        LOGGER.finest(String.format("Getting attribute %s on asset %s...", attributeName, on.getUuid()));

        ChaincodeStub stub = ctx.getStub();

        // read from "cache"
        T res = getter.getAttribute();
        if (res != null) {
            LOGGER.finest("Found attribute in cache!");
            return res;
        }

        LOGGER.finest(String.format("assetclass: %s", assetClass.getName()));
        Field attribField = findAttribField(assetClass, attributeName);
        performAnnotationCheck(assetClass, on, attribField, attributeName);

        Class<T> attribClass = (Class<T>) attribField.getType();

        // try to read from chain for which we need the attribute's comp key
        CompositeKey attribKey = mapper.mapAttributeToCompositeKey(assetClass, on, attributeName);
        LOGGER.finest("made comp key");
        if (!attribExists(stub, attribKey)) {
            LOGGER.severe(String.format("Failed to read %s from ledger", attribKey.toString()));
            throw new NonExistentAssetException(attribKey.toString());
        }
        LOGGER.finest("asset exists");

        byte[] buffer = stub.getState(attribKey.toString());
        byte[] keyBuffer = attribKey.toString().getBytes(UTF_8);

        traceReadSetSize(keyBuffer, keyBuffer);

        if (buffer == null || buffer.length == 0) {
            LOGGER.finest("read buffer was null");
            return null;
        }
        try {
            LOGGER.finest("returning non null value");
            res = objectMapper.readValue(new String(buffer, UTF_8), attribClass);
            LOGGER.finest("successfully");
        } catch (JsonProcessingException e) {
            throw new JsonProcessingFailureException(e);
        }

        LOGGER.finest("setting field...");
        trySetField(attribField, on, res);
        LOGGER.finest("done setting field");
        ctx.updateCache(on.getUuid(), on, new String[] {});
        LOGGER.finest("cache updated");
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AssetBase> T getAssetAttribute(Class<? extends AssetBase> assetClass, AssetBase on,
            String attributeName,
            GetterFunc<T> getter, AssetTransformFunc<T> trans)
            throws NoSuchFieldException, SecurityException {
        LOGGER.finest(String.format("Getting attribute %s on asset %s...", attributeName, on.getUuid()));

        // read from "cache" (if field was set on the obj)
        T res = getter.getAttribute();
        if (res != null) {
            LOGGER.finest("Found attribute in cache!");
            return res;
        }

        Field attribField = findAttribField(assetClass, attributeName);
        performAnnotationCheck(assetClass, on, attribField, attributeName);
        Class<T> attribClass = (Class<T>) attribField.getType();
        LOGGER.finest("found attrib class");

        AssetBase root = on.findRootAsset();
        AssetType type = assetClass.getAnnotation(AssetType.class);
        if (type == null) {
            LOGGER.severe(String.format("Couldn't get annotation for %s", assetClass.getName()));
            throw new MissingAnnotationException(AssetType.class, assetClass.getName());
        }

        // read from actual cache
        String assetUuid = type.type() + root.getUuid();
        if (!ctx.isCacheMiss(assetUuid)) {
            T asset = (T) ctx.getCachedAsset(assetUuid).orElseThrow(() -> new NoSuchElementException());
            trySetField(attribField, on, asset);
            return asset;
        }

        // read from chain
        if (!assetExists(attribClass, assetUuid))
            return null;
        T asset = readAsset(attribClass, assetUuid, trans, on);
        LOGGER.finest("readAsset returned successfully");

        trySetField(attribField, on, asset);
        LOGGER.finest("setNested field on asset successfully");
        ctx.updateCache(on.getUuid(), on, new String[] {});
        return asset;
    }

    private Field findAttribField(Class<?> onClass, String attributeName) throws NoSuchFieldException {
        return Arrays.stream(ReflectionUtils.getDeclaredFieldsForClassAndSuperClasses(onClass))
                .filter(field -> field.getName().equals(attributeName)).findFirst()
                .orElseThrow(() -> new NoSuchFieldException(
                        String.format("Couldn't find field %s on class %s or any of it's parent classes",
                                attributeName, onClass.getName())));
    }

    private void trySetField(Field field, AssetBase on, Object to) {
        field.setAccessible(true);
        try {
            field.set(on, to);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.severe(
                    String.format(
                            "could not set field %s on %s after reading from ledger",
                            field.getName(),
                            on.getUuid()));
            throw new ChaincodeException(e);
        }
    }

    private void performAnnotationCheck(Class<?> assetClass, AssetBase on, Field attribField, String attributeName)
            throws NoSuchFieldException {
        // if attribute is not annotated, it is not persisted on chain, therefore
        // can not be read, therefore an exception is thrown
        if (attribField.getAnnotation(Attribute.class) == null) {
            LOGGER.severe("getAssetAttribute was called on unannotated attrib");
            throw new MissingAnnotationException(Attribute.class, assetClass.getName() + ":" + attributeName);
        }

    }

    private boolean attribExists(ChaincodeStub stub, CompositeKey key) {
        byte[] buffer = stub.getState(key.toString());
        return (buffer != null && buffer.length > 0);
    }

    private void deleteByPartialCompKey(String key) throws JsonProcessingFailureException {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> query = stub.getStateByPartialCompositeKey(key);
        Iterator<KeyValue> it = query.iterator();

        try {
            it.forEachRemaining(assetOnChain -> {
                String json = assetOnChain.getStringValue();
                try {
                    JsonNode node = objectMapper.readTree(json);
                    if (node.has("type") &&
                            node.get("type").asText().equals(AssetBase.class.getName())) {
                        node.fieldNames().forEachRemaining(fname -> {
                            if (fname.equals("type") || fname.equals("uuid"))
                                return;
                            String nestedAssetKey = node.get(fname).asText();
                            try {
                                deleteByPartialCompKey(nestedAssetKey);
                            } catch (JsonProcessingFailureException e) {
                                throw new RuntimeException();
                            }
                        });
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                stub.delState(assetOnChain.getKey());
            });
        } catch (RuntimeException e) {
            throw new JsonProcessingFailureException(e);
        }
    }

    private AssetType getAssetTypeOrThrow(Class<?> clazz) {
        AssetType type = clazz.getAnnotation(AssetType.class);
        if (type == null)
            throw new MissingAnnotationException(AssetType.class, clazz.getName());
        return type;
    }


    private void traceReadSetSize(byte[] keyBuffer, byte[] buffer){
        TraceLogger logger = ctx.getLogger();
        String totalReadSetSize = logger.getTraceValue("total_readset_size");
        if(totalReadSetSize==null){
            logger.putTrace("total_readset_size", String.format("%s",buffer.length+keyBuffer.length));
            logger.putTrace("read_keys_size", String.format("%s",keyBuffer.length));
            logger.putTrace("read_values_size", String.format("%s",buffer.length));
        }else{
            try {
                Integer totalRs = Integer.parseInt(logger.getTraceValue("total_readset_size"));
                Integer readKeys = Integer.parseInt(logger.getTraceValue("read_keys_size"));
                Integer readVals = Integer.parseInt(logger.getTraceValue("read_values_size"));
                logger.updateTraceValue("total_readset_size", String.format("%s",totalRs + buffer.length+keyBuffer.length));
                logger.updateTraceValue("read_keys_size", String.format("%s",readKeys + keyBuffer.length));
                logger.updateTraceValue("read_values_size", String.format("%s",readVals + buffer.length));
            } catch (Exception e) {
                ExceptionLogger.log(LOGGER, e);
                logger.putTrace("total_readset_size", String.format("%s",buffer.length+keyBuffer.length));
                logger.putTrace("read_keys_size", String.format("%s",keyBuffer.length));
                logger.putTrace("read_values_size", String.format("%s",buffer.length));
            }
        }
    } 

}

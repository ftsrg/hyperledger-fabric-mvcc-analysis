package org.example.mapping.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.example.asset.AssetBase;
import org.example.asset.annotation.AssetType;
import org.example.asset.annotation.Attribute;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.mapping.AttributeMappingStrategy;
import org.example.utils.LoggerFactory;
import org.example.utils.ReflectionUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SparseAttributeMappingStrategy implements AttributeMappingStrategy {
    private ObjectMapper mapper;
    private Context ctx;
    private static final Logger LOGGER = LoggerFactory.getLogger(SparseAttributeMappingStrategy.class.getName());

    public SparseAttributeMappingStrategy(ObjectMapper objectMapper, Context context) {
        this.mapper = objectMapper;
        this.ctx = context;
    }

    public LinkedHashMap<String, String> mapAttributesToJsonObjects(Class<?> clazz,
            AssetBase object) throws JsonProcessingFailureException {
        LOGGER.finest(String.format("mapAttributesToJsonObjects called on Asset %s", object.getUuid()));
        LOGGER.finest(String.format("mapAttributesToJsonObjects called on Asset %s", object.toString()));
        Field[] annotatedFields = Arrays.stream(
                ReflectionUtils.getDeclaredFieldsForClassAndSuperClasses(clazz))
                .filter(field -> field.getAnnotation(Attribute.class) != null).toArray(Field[]::new);
        CompositeKey assetKey = getCompositeKey(clazz, object, null);
        LinkedHashMap<String, String> ledgerJsonObjects = new LinkedHashMap<>();

        try {
            // maps the asset shell
            ledgerJsonObjects.put(assetKey.toString(), mapAssetToJson(object));
            // maps it's attributes, can recursively call mapAttributesToJsonObjects()
            // for nested Assets
            ledgerJsonObjects.putAll(mapFieldsToAttributes(clazz, object, annotatedFields));
        } catch (JsonProcessingException e) {
            throw new JsonProcessingFailureException(e);
        }

        LOGGER.finest(String.format("finished mapping of asset %s", object.getUuid()));
        ledgerJsonObjects.forEach((k, v) -> {
            LOGGER.finest(String.format("key: %s, value: %s", k, v));
        });
        return ledgerJsonObjects;
    }

    // maps each @Attribute annotated field into a separate map entry
    private LinkedHashMap<String, String> mapFieldsToAttributes(Class<?> clazz, AssetBase object, Field[] fields)
            throws JsonProcessingFailureException {
        LOGGER.finest(String.format("mapFieldsToAttributes called with %s fields", fields.length));
        LOGGER.finest(String.format("mapFieldsToAttributes called on Asset %s", object.toString()));
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        for (Field field : fields) {
            LOGGER.finest(String.format("Mapping attrib field with name %s", field.getName()));
            Class<?> fieldClass = field.getType();
            field.setAccessible(true);
            Object fieldValue = null;
            try {
                LOGGER.finest(mapper.writeValueAsString(object));

                fieldValue = field.get(object);
                LOGGER.finest("Could get fieldValue");
            } catch (IllegalAccessException | JsonProcessingException e) {
                LOGGER.severe(
                        String.format(
                                "IllegalAccessException occured while mapping field %s on class %s",
                                field.getName(),
                                clazz.getName()));
                LOGGER.severe(e.toString() + e.getMessage());
                throw new RuntimeException();
            }
            if (AssetBase.class.isAssignableFrom(fieldClass)) {
                LOGGER.finest("fieldValue WAS nested");

                final AssetBase asset = (AssetBase) fieldValue;
                if (asset != null) {
                    final Map<String, String> nests = mapAttributesToJsonObjects(
                            field.getType(), asset);
                    res.putAll(nests);
                }
            } else {
                // map each attribute as a separate json object on the ledger
                // using CompositeKeys to represent the class hierarchies
                try {
                    if (fieldValue == null) {
                        LOGGER.finest("fieldValue was null");
                    }
                    String json = mapper.writeValueAsString(fieldValue);
                    final String key = getCompositeKey(clazz, object, field.getName()).toString();
                    res.put(key, json);
                } catch (JsonProcessingException e) {
                    throw new JsonProcessingFailureException(e);
                }
            }
        }
        return res;
    }

    public CompositeKey mapAttributeToCompositeKey(Class<?> clazz, AssetBase asset,
            String attributeName)
            throws NoSuchFieldException, SecurityException {

        LOGGER.finest(String.format("mapAttributeToCompositeKey called for class %s on asset %s with attrib %s",
                clazz.getName(), asset.getUuid(), attributeName));
        Field attribField = Arrays.stream(ReflectionUtils.getDeclaredFieldsForClassAndSuperClasses(clazz))
                .filter(field -> field.getName().equals(attributeName)).findFirst()
                .orElseThrow(() -> new NoSuchFieldException(String.format(
                        "Could not find %s on %s or any of it's parentclasses", attributeName, clazz.getName())));
        Class<?> attributeClass = attribField.getType();

        if (attribField.getAnnotation(Attribute.class) == null)
            throw new RuntimeException(String.format("Attribute %s of %s asset is not annotated with %s", attribField,
                    asset, Attribute.class.getName()));

        // attribute is nested AssetBase of asset
        if (AssetBase.class.isAssignableFrom(attributeClass)) {
            return getCompositeKey(attributeClass, new AssetBase(asset), null);
        }
        // attribute is not nested Asset
        return getCompositeKey(clazz, asset, attributeName);
    }

    private String mapAssetToJson(AssetBase asset) throws JsonProcessingException {
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        // save the asset's own key for completeness
        res.put("uuid", asset.getUuid());
        final AssetType type = asset.getAssetType().orElseGet(null);
        // should not happen, but this is not crucial.finest
        if(type == null){
            res.put("type", null);
        }else{
            res.put("type", type.type());
        }
        // save the composite keys of the nested AssetBase attributes
        Arrays.stream(getNestedAssetFields(asset)).forEach(nestedAssetField -> {
            nestedAssetField.setAccessible(true);
            AssetBase nestedAsset;
            try {
                nestedAsset = (AssetBase) nestedAssetField.get(asset);
                res.put(nestedAssetField.getName(),
                        getCompositeKey(nestedAssetField.getType(), nestedAsset, null).toString());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // shouldn't happen
                e.printStackTrace();
            }
        });
        // save the host of this asset if nested
        if (asset.getNestHost().isPresent()) {
            AssetBase nestHost = asset.getNestHost().get();
            res.put("nestHost", getCompositeKey(nestHost.getClass(), nestHost, null).toString());
        }
        return mapper.writeValueAsString(res);
    }

    // returns a CompositeKey for an Asset's attribute with the following scheme:
    // <object's classname>_<object's uuid>_<attribute's name>
    //
    // if the optionalAttribute is null and the comp key is for an asset:
    //
    // returns a CompositeKey for an Asset with the following scheme:
    // <object's classname>_<object's uuid>
    private CompositeKey getCompositeKey(Class<?> clazz, AssetBase asset, String optionalAttribute) {
        LOGGER.finest(String.format("Creating comp key with args: %s, %s, %s", clazz.getName(), asset.getUuid(),
                optionalAttribute));

        if (asset.getUuid() == null)
            throw new ChaincodeException("Asset has no uuid");

        String uuid = asset.getUuid();
        AssetType type = clazz.getDeclaredAnnotation(AssetType.class);
        if (type == null)
            throw new ChaincodeException(
                    String.format("Asset with class %s has no type annotation", clazz.getName()));

        // if the comp key is for an Asset
        if (optionalAttribute == null)
            return ctx.getStub().createCompositeKey(type.type().toUpperCase(), uuid);

        // comp key is for an attribute
        return ctx.getStub().createCompositeKey(type.type().toUpperCase(), uuid, optionalAttribute);
    }

    private Field[] getNestedAssetFields(AssetBase asset) {
        Class<?> clazz = asset.getClass();
        Field[] fields = clazz.getDeclaredFields();
        return Arrays.stream(fields).filter(field -> field.getAnnotation(Attribute.class) != null
                && AssetBase.class.isAssignableFrom(field.getType())).toArray(Field[]::new);
    }
}

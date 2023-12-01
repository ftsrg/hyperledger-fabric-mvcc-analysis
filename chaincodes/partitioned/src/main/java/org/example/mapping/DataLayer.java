package org.example.mapping;

import org.example.asset.AssetBase;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.functions.AssetTransformFunc;
import org.example.functions.GetterFunc;
import org.example.functions.SetterFunc;

// TODO add doc to methods
public interface DataLayer {
    
    public boolean assetExists(Class<?> clazz, String uuid);

    public <T extends AssetBase> T createAsset(Class<T> clazz, String uuid, AssetTransformFunc<T> transformer);
    public <T extends AssetBase> T readAsset(Class<T> clazz, String uuid, AssetTransformFunc<T> trans,
            AssetBase optionalNesthost);
    public void updateAsset(Class<?> clazz, AssetBase asset);
    public void deleteAsset(Class<?> clazz, String uuid) throws JsonProcessingFailureException;
    public <T> void setAttribute(Class<?> clazz, AssetBase on, T object, String attributeName, SetterFunc<T> setter);
    public <T> T getAttribute(Class<?> assetClass, AssetBase on, String attributeName,
            GetterFunc<T> getter, AssetTransformFunc<T> trans)
            throws NoSuchFieldException, SecurityException, JsonProcessingFailureException;
    public <T extends AssetBase> T getAssetAttribute(Class<? extends AssetBase> assetClass, AssetBase on,
            String attributeName,
            GetterFunc<T> getter, AssetTransformFunc<T> trans)
            throws NoSuchFieldException, SecurityException;
}

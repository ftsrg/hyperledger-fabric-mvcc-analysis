package org.example.functions;

import org.example.asset.AssetBase;

@FunctionalInterface
public interface AssetTransformFunc<T> {
    T transfromFromBase(AssetBase asset);
}

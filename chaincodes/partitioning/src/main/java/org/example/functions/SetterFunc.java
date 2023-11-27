package org.example.functions;

@FunctionalInterface
public interface SetterFunc<T> {
    void setAttribute(T obj);
}

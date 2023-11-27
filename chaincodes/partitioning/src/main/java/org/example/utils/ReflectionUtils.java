package org.example.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static Field[] getDeclaredFieldsForClassAndSuperClasses(Class<?> clazz) {
        if (clazz.getSuperclass() == null) {
            return clazz.getDeclaredFields();
        }
        Field[] res = ArrayUtils.concatArrays(clazz.getDeclaredFields(),
                getDeclaredFieldsForClassAndSuperClasses(clazz.getSuperclass()));
        return res;
    }
}

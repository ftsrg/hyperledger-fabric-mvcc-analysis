package org.example.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

public class ArrayUtils {

    @SuppressWarnings("unchecked")
    public static <T> T[] concatArrays(T[] array1, T[] array2) {
        return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
                .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));
    }

}

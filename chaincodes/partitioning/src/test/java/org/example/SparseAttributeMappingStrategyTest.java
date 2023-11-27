package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.example.asset.AssetBase;
import org.example.asset.annotation.AssetType;
import org.example.asset.annotation.Attribute;
import org.example.exceptions.JsonProcessingFailureException;
import org.example.mapping.impl.SparseAttributeMappingStrategy;
import org.example.mapping.impl.SparseCachedContext;
import org.example.mapping.lifecycle.ObjectMapperFactory;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SparseAttributeMappingStrategyTest {

    public SparseAttributeMappingStrategy mappingStrategy;
    public SparseCachedContext mockContext;
    public ChaincodeStub mockStub;
    public ObjectMapper mapper;

    private void prettyPrintMap(Map<String, String> map) {
        System.out.println("{");
        map.forEach((k, v) -> System.out.println(
                String.format("key: %s, value: %s,", k, v)));
        System.out.println("}");
    }

    @BeforeEach
    public void init() {

        mockContext = mock(SparseCachedContext.class);
        mockStub = mock(ChaincodeStub.class);
        mappingStrategy = new SparseAttributeMappingStrategy(ObjectMapperFactory.getInstance().createObjectMapper(),mockContext);
        when(mockContext.getStub()).thenReturn(mockStub);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE); // turn off everything
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        when(mockStub.createCompositeKey(anyString(), any())).thenAnswer(new Answer<CompositeKey>() {

            @Override
            public CompositeKey answer(InvocationOnMock invocation) throws Throwable {
                Object[] params = invocation.getArguments();
                String[] keyparts = Arrays.stream(params).map(element -> (String) element)
                        .toArray(size -> (String[]) Array.newInstance(String.class, size));

                return new CompositeKey(keyparts[0], Arrays.copyOfRange(keyparts, 1, keyparts.length));
            }
        });
    }

    @AssetType(type = "TestClass")
    public class TestClass extends AssetBase {

        @Attribute
        String stringAttribute;

        @Attribute
        private NestedTestClass nested;

        TestClass(SparseCachedContext ctx, String uuid) {
            super(ctx, uuid);
            stringAttribute = "hello";
            nested = new NestedTestClass(this);
        }

        public NestedTestClass getNested() {
            return nested;
        }
    }

    @AssetType(type = "NestedTest")
    public class NestedTestClass extends AssetBase {

        @Attribute
        String stringAttribute;

        NestedTestClass(AssetBase nestHost) {
            super(nestHost);
            stringAttribute = "from the inner side";
        }
    }

    @Test
    public void AttributeMappingToJsonTestHappyPathWithNestedClass()
            throws JsonProcessingException, JsonProcessingFailureException {
        String rootType = "TestClass";
        String nestType = "NestedTest";
        TestClass object = new TestClass(mockContext,"uuid");
        LinkedHashMap<String, String> actualMap = mappingStrategy.mapAttributesToJsonObjects(TestClass.class, object);
        LinkedHashMap<String, String> expectedMap = new LinkedHashMap<>();

        String rootKey = new CompositeKey(rootType.toUpperCase(), object.getUuid()).toString();
        String nestKey = new CompositeKey(nestType.toUpperCase(), object.nested.getUuid()).toString();
        String rootStringAttKey = new CompositeKey(rootType.toUpperCase(), object.getUuid(),
                "stringAttribute").toString();
        String nestStringAttKey = new CompositeKey(nestType.toUpperCase(),
                object.nested.getUuid(), "stringAttribute").toString();

        LinkedHashMap<String, String> rootAsset = new LinkedHashMap<>();
        rootAsset.put("uuid", object.getUuid());
        rootAsset.put("type", AssetBase.class.getName());
        rootAsset.put("nested", nestKey);
        expectedMap.put(rootKey, mapper.writeValueAsString(rootAsset));
        expectedMap.put(rootStringAttKey, mapper.writeValueAsString(object.stringAttribute));

        LinkedHashMap<String, String> nestAsset = new LinkedHashMap<>();
        nestAsset.put("uuid", object.nested.getUuid());
        nestAsset.put("type", AssetBase.class.getName());
        nestAsset.put("nestHost", rootKey);
        expectedMap.put(nestKey, mapper.writeValueAsString(nestAsset));
        expectedMap.put(nestStringAttKey, mapper.writeValueAsString(object.nested.stringAttribute));

        System.out.println("Expected: ");
        prettyPrintMap(expectedMap);
        System.out.println("Actual: ");
        prettyPrintMap(actualMap);
        verify(mockStub, Mockito.times(6)).createCompositeKey(anyString(), any());

        assertEquals(actualMap, expectedMap);
    }

    @Test
    public void AttributeMappingToCompositeKeyHappyPathWithNestedClass()
            throws JsonProcessingException, NoSuchFieldException, SecurityException {

        String rootType = "TestClass";
        String nestType = "NestedTest";

        TestClass object = new TestClass(mockContext, "uuid");

        String testKey = new CompositeKey(rootType.toUpperCase(),
                object.getUuid(),
                "stringAttribute").toString();
        String nestedKey = new CompositeKey(nestType.toUpperCase(),
                object.nested.getUuid(), "stringAttribute").toString();

        CompositeKey actualTestKey = mappingStrategy.mapAttributeToCompositeKey(TestClass.class, object,
                "stringAttribute");
        CompositeKey actualNestedKey = mappingStrategy.mapAttributeToCompositeKey(NestedTestClass.class,
                object.getNested(),
                "stringAttribute");

        System.out.println("Expected: " + testKey.toString());
        System.out.println("Actual: " + actualTestKey.toString());

        System.out.println("Expected: " + nestedKey.toString());
        System.out.println("Actual: " + actualNestedKey.toString());

        assertEquals(testKey, actualTestKey.toString());
        assertEquals(nestedKey, actualNestedKey.toString());

        // Method getCompKey =
        // Arrays.stream(mappingStrategy.getClass().getDeclaredMethods())
        // .filter(m -> m.getName().equalsIgnoreCase("getCompositeKey")).map(m -> {
        // m.setAccessible(true);
        // return m;
        // }).findFirst().orElseThrow(() -> new NoSuchElementException());
    }
}

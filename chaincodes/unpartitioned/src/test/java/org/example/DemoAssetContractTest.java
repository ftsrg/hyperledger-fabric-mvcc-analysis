///*
// * SPDX-License-Identifier: Apache License 2.0
// */
//
//package org.example;
//import static java.nio.charset.StandardCharsets.UTF_8;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.nio.charset.StandardCharsets;
//
//import org.hyperledger.fabric.contract.Context;
//import org.hyperledger.fabric.shim.ChaincodeStub;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//
//public final class DemoAssetContractTest {
//
//    @Nested
//    class AssetExists {
//        @Test
//        public void noProperAsset() {
//
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//
//            when(stub.getState("10001")).thenReturn(new byte[] {});
//            boolean result = contract.demoAssetExists(ctx,"10001");
//
//            assertFalse(result);
//        }
//
//        @Test
//        public void assetExists() {
//
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//
//            when(stub.getState("10001")).thenReturn(new byte[] {42});
//            boolean result = contract.demoAssetExists(ctx,"10001");
//
//            assertTrue(result);
//
//        }
//
//        @Test
//        public void noKey() {
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//
//            when(stub.getState("10002")).thenReturn(null);
//            boolean result = contract.demoAssetExists(ctx,"10002");
//
//            assertFalse(result);
//
//        }
//
//    }
//
//    @Nested
//    class AssetCreates {
//
//        @Test
//        public void newAssetCreate() {
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//
//            String json = "{\"value\":\"TheDemoAsset\"}";
//
//            contract.createDemoAsset(ctx, "10001", "TheDemoAsset");
//
//            verify(stub).putState("10001", json.getBytes(UTF_8));
//        }
//
//        @Test
//        public void alreadyExists() {
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//
//            when(stub.getState("10002")).thenReturn(new byte[] { 42 });
//
//            Exception thrown = assertThrows(RuntimeException.class, () -> {
//                contract.createDemoAsset(ctx, "10002", "TheDemoAsset");
//            });
//
//            assertEquals(thrown.getMessage(), "The asset 10002 already exists");
//
//        }
//
//    }
//
//    @Test
//    public void assetRead() {
//        DemoAssetContract contract = new  DemoAssetContract();
//        Context ctx = mock(Context.class);
//        ChaincodeStub stub = mock(ChaincodeStub.class);
//        when(ctx.getStub()).thenReturn(stub);
//
//        DemoAsset asset = new  DemoAsset();
//        asset.setValue("Valuable");
//
//        String json = asset.toJSONString();
//        when(stub.getState("10001")).thenReturn(json.getBytes(StandardCharsets.UTF_8));
//
//        DemoAsset returnedAsset = contract.readDemoAsset(ctx, "10001");
//        assertEquals(returnedAsset.getValue(), asset.getValue());
//    }
//
//    @Nested
//    class AssetUpdates {
//        @Test
//        public void updateExisting() {
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getState("10001")).thenReturn(new byte[] { 42 });
//
//            contract.updateDemoAsset(ctx, "10001", "updates");
//
//            String json = "{\"value\":\"updates\"}";
//            verify(stub).putState("10001", json.getBytes(UTF_8));
//        }
//
//        @Test
//        public void updateMissing() {
//            DemoAssetContract contract = new  DemoAssetContract();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//
//            when(stub.getState("10001")).thenReturn(null);
//
//            Exception thrown = assertThrows(RuntimeException.class, () -> {
//                contract.updateDemoAsset(ctx, "10001", "TheDemoAsset");
//            });
//
//            assertEquals(thrown.getMessage(), "The asset 10001 does not exist");
//        }
//
//    }
//
//    @Test
//    public void assetDelete() {
//        DemoAssetContract contract = new  DemoAssetContract();
//        Context ctx = mock(Context.class);
//        ChaincodeStub stub = mock(ChaincodeStub.class);
//        when(ctx.getStub()).thenReturn(stub);
//        when(stub.getState("10001")).thenReturn(null);
//
//        Exception thrown = assertThrows(RuntimeException.class, () -> {
//            contract.deleteDemoAsset(ctx, "10001");
//        });
//
//        assertEquals(thrown.getMessage(), "The asset 10001 does not exist");
//    }
//
//}
//
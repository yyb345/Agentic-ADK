///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.mcp;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.langengine.mcp.client.Client;
//import com.alibaba.langengine.mcp.client.backup.ClientOptions;
//import com.alibaba.langengine.mcp.client.backup.StdioClientTransportOld;
//import com.alibaba.langengine.mcp.spec.*;
//import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceRequest;
//import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceResult;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//@Slf4j
//public class ClientApplication {
//
//    public static void main(String[] args) throws InterruptedException {
//        Implementation clientInfo = new Implementation("example-client", "1.0.0");
//        ClientCapabilities clientCapabilities = new ClientCapabilities(new JSONObject(), new JSONObject(), null);
//        ClientOptions options = new ClientOptions(clientCapabilities);
//        Client client = new Client(clientInfo, options);
//
//        InputStream processInputStream = System.in;
//        OutputStream processOutputStream = System.out;
//
//        StdioClientTransportOld transport = new StdioClientTransportOld(processInputStream, processOutputStream);
//
//        CompletableFuture<Void> future = client.connect(transport);
//
////        try {
////            future.get();
////        } catch (ExecutionException e) {
////            throw new RuntimeException(e);
////        }
//
//        log.info("Client running on stdio");
//
//        // List available resources
////        CompletableFuture<ListResourcesResult> resources = client.listResources();
////        System.out.println("Resources: " + resources);
//
//        // Read a specific resource
//        ReadResourceRequest request = new ReadResourceRequest("file:///example.txt");
//        CompletableFuture<ReadResourceResult> resourceContent = client.readResource(request, null);
//        try {
//            ReadResourceResult readResourceResult = resourceContent.get();
//            log.info("Resource Content: " + JSONObject.toJSONString(readResourceResult));
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//
//
////        Server server = configureServer();
////        StdioServerTransport transport = new StdioServerTransport();
////        server.connect(transport);
////
////        System.out.println("Server running on stdio");
////        CountDownLatch done = new CountDownLatch(1);
////
////        new Thread(() -> {
////            try {
////                // 在这里执行异步任务
//////                Thread.sleep(5000);
////                server.setOnCloseCallback(() -> {
////                    done.countDown();
////                });
//////                done.countDown();
////            } catch (Throwable e) {
////                e.printStackTrace();
////            }
////        }).start();
////
////        done.await();
////        System.out.println("Server closed");
//    }
//}

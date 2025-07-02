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
//import java.io.*;
//import java.util.concurrent.*;
//
//public class StdioServer {
//    private ExecutorService executor;
//
//    public StdioServer() {
//        executor = Executors.newFixedThreadPool(2); // 创建一个线程池来处理输入和输出
//    }
//
//    public void runServer() {
//        CompletableFuture<Void> readTask = CompletableFuture.runAsync(this::processStdin, executor);
//        CompletableFuture<Void> writeTask = CompletableFuture.runAsync(this::processStdout, executor);
//
//        CompletableFuture<Void> serverTask = CompletableFuture.allOf(readTask, writeTask);
//        try {
//            serverTask.get(); // 等待所有任务完成
//        } catch (InterruptedException | ExecutionException e) {
//            System.err.println("Server failed: " + e.getMessage());
//        } finally {
//            shutdown();
//        }
//    }
//
//    private void processStdin() {
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // 处理接收到的每一行输入
//                System.out.println("Message received: " + line);
//                // 假设我们解析JSON并作一些处理
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading from stdin: " + e.getMessage());
//        }
//    }
//
//    private void processStdout() {
//        try {
//            while (true) {
//                // 模拟发送一些数据到stdout
//                System.out.println("Sending message to stdout");
//                Thread.sleep(1000); // 等待1秒
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    private void shutdown() {
//        executor.shutdown();
//    }
//
//    public static void main(String[] args) {
//        StdioServer server = new StdioServer();
//        server.runServer();
//    }
//}

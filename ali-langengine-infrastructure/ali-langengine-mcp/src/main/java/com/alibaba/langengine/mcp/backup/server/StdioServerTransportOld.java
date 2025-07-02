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
//package com.alibaba.langengine.mcp.server.backup;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.langengine.mcp.shared.ReadBuffer;
//import com.alibaba.langengine.mcp.shared.Transport;
//import com.alibaba.langengine.mcp.spec.JSONRPCMessage;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.util.Arrays;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//@Data
//@Slf4j
//public class StdioServerTransportOld implements Transport {
//
//    private final BufferedInputStream inputStream;
//    private final PrintStream outputStream;
//    private final AtomicBoolean initialized = new AtomicBoolean(false);
//    private final ExecutorService executorService = Executors.newFixedThreadPool(2);  // For reading and processing
//
//    private final BlockingQueue<byte[]> readQueue = new LinkedBlockingQueue<>();
//    private ReadBuffer readBuffer = new ReadBuffer();
//
//    private Runnable onClose = null;
//    private Consumer<Throwable> onError = null;
//    private Function<JSONRPCMessage, CompletableFuture<Void>> onMessage = null;
//
//    public StdioServerTransportOld() {
//        this(new BufferedInputStream(System.in), System.out);
//    }
//
//    public StdioServerTransportOld(BufferedInputStream inputStream, PrintStream outputStream) {
//        this.inputStream = inputStream;
//        this.outputStream = outputStream;
//    }
//
//    public CompletableFuture<Void> start() {
//        if (!initialized.compareAndSet(false, true)) {
//            throw new IllegalStateException("StdioServerTransport already started!");
//        }
//
////        log.info("Starting StdioServerTransport");
//
//        return CompletableFuture.runAsync(() -> {
//            executorService.submit(this::readFromStdin);
//            executorService.submit(this::processReadBuffer);
//        });
//    }
//
//    private void readFromStdin() {
//        byte[] buf = new byte[8192];
//        int bytesRead;
//        try {
//            while ((bytesRead = inputStream.read(buf)) != -1) {
//                if (bytesRead > 0) {
//                    readQueue.put(Arrays.copyOf(buf, bytesRead));
//                }
//            }
//        } catch (IOException | InterruptedException e) {
//            log.error("Error reading from stdin: " + e.getMessage());
//            onError(e);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        } finally {
//            close();
//        }
//    }
//
//    private void processReadBuffer() {
//        try {
//            while (true) {
//                byte[] chunk = readQueue.take();
//                readBuffer.append(chunk);
//                JSONRPCMessage message = readBuffer.readMessage();
//                log.info("server processReadBuffer message:" + JSON.toJSONString(message));
//                if (message != null && onMessage != null) {
//                    onMessage.apply(message);
//                }
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } catch (Exception e) {
//            if (onError != null) onError.accept(e);
//            log.error("Error processing read buffer: " + e.getMessage());
//        }
//    }
//
//    private void onError(Throwable e) {
//        // Error handling logic
//    }
//
//    public  CompletableFuture<Void> close() {
//        if (!initialized.compareAndSet(true, false)) {
//            return CompletableFuture.runAsync(() -> {});
//        }
//
//        return CompletableFuture.runAsync(() -> {
//            executorService.shutdownNow();
//        });
//    }
//
//    public CompletableFuture<Void> send(JSONRPCMessage message) {
//        return CompletableFuture.runAsync(() -> {
//            String json = JSON.toJSONString(message);
//            log.info("server send message:" + json);
//            synchronized (outputStream) {
//                outputStream.print(json);
//                outputStream.flush();
//            }
//        });
//    }
//}

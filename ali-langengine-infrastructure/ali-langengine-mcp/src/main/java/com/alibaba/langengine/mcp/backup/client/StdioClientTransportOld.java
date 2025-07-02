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
//package com.alibaba.langengine.mcp.client.backup;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.langengine.mcp.client.transport.ServerParameters;
//import com.alibaba.langengine.mcp.shared.ReadBuffer;
//import com.alibaba.langengine.mcp.shared.Transport;
//import com.alibaba.langengine.mcp.spec.JSONRPCMessage;
//import io.reactivex.Scheduler;
//import io.reactivex.schedulers.Schedulers;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Mono;
//import reactor.core.publisher.Sinks;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//@Data
//@Slf4j
//public class StdioClientTransportOld implements Transport {
//
//    private InputStream input;
//    private OutputStream output;
//    private final ExecutorService executorService = Executors.newCachedThreadPool();
//    private final BlockingQueue<JSONRPCMessage> sendQueue = new LinkedBlockingQueue<>();
//    private final AtomicBoolean initialized = new AtomicBoolean(false);
//    private final ReadBuffer readBuffer = new ReadBuffer();
//    private Future<?> readFuture;
//    private Future<?> writeFuture;
//
//    private Runnable onClose;
//    private Consumer<Throwable> onError;
//    private Function<JSONRPCMessage, CompletableFuture<Void>> onMessage;
//
//    private Sinks.Many<JSONRPCMessage> inboundSink;
//
//    private Sinks.Many<JSONRPCMessage> outboundSink;
//
//    /** Scheduler for handling inbound messages from the server process */
//    private Scheduler inboundScheduler;
//
//    /** Scheduler for handling outbound messages to the server process */
//    private Scheduler outboundScheduler;
//
//    /** Scheduler for handling error messages from the server process */
//    private Scheduler errorScheduler;
//
//    private ServerParameters parameters;
//
//    private Sinks.Many<String> errorSink;
//
//    public StdioClientTransportOld(InputStream input, OutputStream output) {
//        this.input = input;
//        this.output = output;
//    }
//
//    public StdioClientTransportOld(ServerParameters parameters) {
//        this.inboundSink = Sinks.many().unicast().onBackpressureBuffer();
//        this.outboundSink = Sinks.many().unicast().onBackpressureBuffer();
//
//        this.parameters = parameters;
//
//        this.errorSink = Sinks.many().unicast().onBackpressureBuffer();
//
//        // Start threads
//        this.inboundScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
//        this.outboundScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
//        this.errorScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
//    }
//
//    private void handleIncomingMessages(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> inboundMessageHandler) {
//        this.inboundSink.asFlux()
//                .flatMap(message -> Mono.just(message)
//                        .transform(inboundMessageHandler)
//                        .contextWrite(ctx -> ctx.put("observation", "myObservation")))
//                .subscribe();
//    }
//
//
//    public CompletableFuture<Void> start() {
//        if (!initialized.compareAndSet(false, true)) {
//            throw new IllegalStateException("StdioClientTransport already started!");
//        }
//
////        log.info("Starting StdioClientTransport");
//
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
//
//        return CompletableFuture.runAsync(() -> {
//            readFuture = executorService.submit(() -> {
//                try {
//                    byte[] buffer = new byte[8192];
//                    int bytesRead;
//                    while ((bytesRead = input.read(buffer)) != -1) {
//                        if (bytesRead > 0) {
//                            readBuffer.append(Arrays.copyOf(buffer, bytesRead));
//                            processReadBuffer();
//                        }
//                    }
//                } catch (Exception e) {
//                    if (onError != null) onError.accept(e);
//                    log.info("Error reading from input stream: " + e.getMessage());
//                } finally {
//                    close();
//                }
//            });
//
//            writeFuture = executorService.submit(() -> {
//                try {
//                    JSONRPCMessage message;
//                    while ((message = sendQueue.take()) != null) {
//                        String json = serializeMessage(message);
//                        writer.write(json);
//                        writer.flush();
//                        log.info("sendQueue serializeMessage:{}", json);
//                    }
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } catch (IOException e) {
//                    if (onError != null) onError.accept(e);
//                    log.info("Error writing to output stream: " + e.getMessage());
//                } finally {
//                    try {
//                        output.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        });
//    }
//
//    private void processReadBuffer() {
//        JSONRPCMessage message;
//        while ((message = readBuffer.readMessage()) != null) {
//            try {
//                log.info("client processReadBuffer message:" + JSON.toJSONString(message));
//                if (onMessage != null) {
//                    onMessage.apply(message);
//                }
//            } catch (Throwable e) {
//                if (onError != null) onError.accept(e);
//                log.info("Error processing message: " + e.getMessage());
//            }
//        }
//    }
//
//    public CompletableFuture<Void> send(JSONRPCMessage message) {
//        if (!initialized.get()) {
//            throw new IllegalStateException("Transport not started");
//        }
//        return CompletableFuture.runAsync(() -> {
//            try {
//                sendQueue.put(message);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    public CompletableFuture<Void> close() {
//        if (!initialized.compareAndSet(true, false)) {
//            throw new IllegalStateException("Transport is already closed");
//        }
//        return CompletableFuture.runAsync(() -> {
//            readFuture.cancel(true);
//            writeFuture.cancel(true);
//            executorService.shutdownNow();
//            try {
//                input.close();
//            } catch (IOException e) {
//                extracted(e);
//            }
//            try {
//                output.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            readBuffer.clear();
//            if (onClose != null) onClose.run();
//        });
//    }
//
//    private static void extracted(IOException e) {
//        throw new RuntimeException(e);
//    }
//
//    private String serializeMessage(JSONRPCMessage message) {
//        return readBuffer.serializeMessage(message);
//    }
//}

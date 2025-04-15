/*
 * Copyright 2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.modelcontextprotocol.client.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alibaba.langengine.modelcontextprotocol.spec.JSONRPCMessage;
import com.alibaba.langengine.modelcontextprotocol.spec.McpClientTransport;
import com.alibaba.langengine.modelcontextprotocol.spec.McpSchema;
import com.alibaba.langengine.modelcontextprotocol.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of the MCP Stdio transport that communicates with a server process using
 * standard input/output streams. Messages are exchanged as newline-delimited JSON-RPC
 * messages over stdin/stdout, with errors and debug information sent to stderr.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 * @author aihe.ah
 */
public class StdioClientTransport implements McpClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(StdioClientTransport.class);

    // 共享的定时任务线程池，用于处理超时
    private static final java.util.concurrent.ScheduledExecutorService SHARED_SCHEDULER =
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mcp-timeout-scheduler");
            t.setDaemon(true); // 设置为守护线程，这样不会阻止JVM退出
            return t;
        });

    /**
     * 关闭共享的线程池
     * 这个方法应该在应用程序退出前调用，以确保线程池正确关闭
     */
    public static void shutdownSharedScheduler() {
        try {
            logger.info("Shutting down shared scheduler");
            SHARED_SCHEDULER.shutdownNow();
            // 等待线程池关闭
            SHARED_SCHEDULER.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error shutting down shared scheduler", e);
        }
    }



    private final Sinks.Many<JSONRPCMessage> inboundSink;

    private final Sinks.Many<JSONRPCMessage> outboundSink;

    /**
     * The server process being communicated with
     */
    private Process process;

    private ObjectMapper objectMapper;

    /**
     * Scheduler for handling inbound messages from the server process
     */
    private Scheduler inboundScheduler;

    /**
     * Scheduler for handling outbound messages to the server process
     */
    private Scheduler outboundScheduler;

    /**
     * Scheduler for handling error messages from the server process
     */
    private Scheduler errorScheduler;

    /**
     * Parameters for configuring and starting the server process
     */
    private final ServerParameters params;

    private final Sinks.Many<String> errorSink;

    private volatile boolean isClosing = false;

    // visible for tests
    private Consumer<String> stdErrorHandler = new Consumer<String>() {
        @Override
        public void accept(String error) {
            // 根据错误消息的内容决定日志级别
            if (error == null || error.trim().isEmpty()) {
                return;
            }

            // 常见的非关键错误，降级为DEBUG
            if (error.contains("EPERM: operation not permitted") ||
                error.contains("node:fs:") ||
                error.contains("Error: ENOENT: no such file or directory") ||
                error.contains("Warning:") ||
                error.startsWith("^") ||
                error.trim().isEmpty()) {

                logger.debug("STDERR Message received: {}", error);
            }
            // 关键错误，保持WARN级别
            else if (error.contains("Error:") ||
                     error.contains("Exception:") ||
                     error.contains("Failed:")) {

                logger.warn("STDERR Error: {}", error);
            }
            // 其他错误信息，使用INFO级别
            else {
                logger.info("STDERR Message: {}", error);
            }
        }
    };

    /**
     * Creates a new StdioClientTransport with the specified parameters and default
     * ObjectMapper.
     *
     * @param params The parameters for configuring the server process
     */
    public StdioClientTransport(ServerParameters params) {
        this(params, new ObjectMapper());
    }

    /**
     * Creates a new StdioClientTransport with the specified parameters and ObjectMapper.
     *
     * @param params       The parameters for configuring the server process
     * @param objectMapper The ObjectMapper to use for JSON serialization/deserialization
     */
    public StdioClientTransport(ServerParameters params, ObjectMapper objectMapper) {
        Assert.notNull(params, "The params can not be null");
        Assert.notNull(objectMapper, "The ObjectMapper can not be null");

        this.inboundSink = Sinks.many().unicast().onBackpressureBuffer();
        this.outboundSink = Sinks.many().unicast().onBackpressureBuffer();

        this.params = params;

        this.objectMapper = objectMapper;

        this.errorSink = Sinks.many().unicast().onBackpressureBuffer();

        // 使用共享的线程工厂创建线程池
        this.inboundScheduler = Schedulers.fromExecutorService(
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "mcp-inbound");
                t.setDaemon(true);
                return t;
            }), "inbound");
        this.outboundScheduler = Schedulers.fromExecutorService(
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "mcp-outbound");
                t.setDaemon(true);
                return t;
            }), "outbound");
        this.errorScheduler = Schedulers.fromExecutorService(
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "mcp-error");
                t.setDaemon(true);
                return t;
            }), "error");
    }

    /**
     * Starts the server process and initializes the message processing streams. This
     * method sets up the process with the configured command, arguments, and environment,
     * then starts the inbound, outbound, and error processing threads.
     *
     * @throws RuntimeException if the process fails to start or if the process streams
     *                          are null
     */
    // Default timeout for connection in seconds
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;

    @Override
    public CompletableFuture<Void> connect(Function<CompletableFuture<JSONRPCMessage>, CompletableFuture<JSONRPCMessage>> handler) {
        CompletableFuture<Void> connectionFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set up message handlers first
                    handleIncomingMessages(handler);
                    handleIncomingErrors();

                    // Prepare command and environment
                    List<String> fullCommand = new ArrayList<>();
                    fullCommand.add(params.getCommand());
                    fullCommand.addAll(params.getArgs());

                    logger.info("Starting process with command: {}", fullCommand);

                    ProcessBuilder processBuilder = getProcessBuilder();
                    processBuilder.command(fullCommand);
                    processBuilder.environment().putAll(params.getEnv());

                    // Redirect error stream to ensure it's properly handled
                    processBuilder.redirectErrorStream(false);

                    // Start the process
                    try {
                        process = processBuilder.start();
                        logger.info("Process started successfully");
                    } catch (IOException e) {
                        logger.error("Failed to start process with command: {}", fullCommand, e);
                        throw new RuntimeException("Failed to start process with command: " + fullCommand, e);
                    }

                    // Validate process streams
                    if (process.getInputStream() == null || process.getOutputStream() == null) {
                        process.destroy();
                        logger.error("Process input or output stream is null");
                        throw new RuntimeException("Process input or output stream is null");
                    }

                    // Start threads for processing I/O
                    startInboundProcessing();
                    startOutboundProcessing();
                    startErrorProcessing();

                    // Check if process is alive after starting
                    if (!process.isAlive()) {
                        try {
                            int exitCode = process.exitValue();
                            logger.error("Process exited immediately with code: {}", exitCode);
                            throw new RuntimeException("Process exited immediately with code: " + exitCode);
                        } catch (IllegalThreadStateException e) {
                            // This shouldn't happen since we checked !isAlive()
                            logger.warn("Unexpected process state", e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error during connection setup", e);
                    throw e;
                }
            }
        });

        // Add timeout to the connection future
        CompletableFuture<Void> timeoutCompletableFuture = new CompletableFuture<>();

        // 使用共享的定时任务线程池来处理超时
        java.util.concurrent.ScheduledFuture<?> timeoutFuture = SHARED_SCHEDULER.schedule(() -> {
            if (!connectionFuture.isDone()) {
                logger.warn("Connection timed out after {} seconds", DEFAULT_CONNECT_TIMEOUT_SECONDS);
                CompletableFuture.runAsync(() -> {
                    // If we timeout, make sure to clean up resources
                    if (process != null) {
                        logger.warn("Terminating process due to connection timeout");
                        process.destroyForcibly();
                    }
                });
                timeoutCompletableFuture.completeExceptionally(
                    new java.util.concurrent.TimeoutException("Connection timed out after " +
                        DEFAULT_CONNECT_TIMEOUT_SECONDS + " seconds"));
            }
        }, DEFAULT_CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

        // Either the connection completes or we timeout
        connectionFuture.whenComplete((result, ex) -> {
            // 取消超时任务
            timeoutFuture.cancel(false);

            if (ex == null) {
                timeoutCompletableFuture.complete(null);
            } else {
                timeoutCompletableFuture.completeExceptionally(ex);
            }
        });

        return timeoutCompletableFuture;
    }

    /**
     * Creates and returns a new ProcessBuilder instance. Protected to allow overriding in
     * tests.
     *
     * @return A new ProcessBuilder instance
     */
    protected ProcessBuilder getProcessBuilder() {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Ensure we don't inherit IO streams from parent process
        // This is important for IntelliJ IDEA which might have different stream handling
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);

        return processBuilder;
    }

    /**
     * Sets the handler for processing transport-level errors.
     *
     * <p>
     * The provided handler will be called when errors occur during transport operations,
     * such as connection failures or protocol violations.
     * </p>
     *
     * @param errorHandler a consumer that processes error messages
     */
    public void setStdErrorHandler(Consumer<String> errorHandler) {
        this.stdErrorHandler = errorHandler;
    }

    /**
     * Waits for the server process to exit.
     *
     * @throws RuntimeException if the process is interrupted while waiting
     */
    public void awaitForExit() {
        try {
            this.process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Process interrupted", e);
        }
    }

    /**
     * Starts the error processing thread that reads from the process's error stream.
     * Error messages are logged and emitted to the error sink.
     */
    private void startErrorProcessing() {
        this.errorScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                BufferedReader processErrorReader = null;
                try {
                    logger.info("Starting error processing thread");
                    processErrorReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
                    String line;

                    // Check if process is still alive before starting to read
                    if (!process.isAlive()) {
                        logger.warn("Process is not alive before starting to read error stream");
                        return;
                    }

                    logger.info("Error processing thread ready to read");
                    while (!isClosing && process.isAlive() && (line = processErrorReader.readLine()) != null) {
                        final String errorLine = line;
                        logger.debug("Received error line: {}", errorLine);
                        try {
                            if (!errorSink.tryEmitNext(errorLine).isSuccess()) {
                                if (!isClosing) {
                                    logger.error("Failed to emit error message");
                                }
                                break;
                            }
                        } catch (Exception e) {
                            if (!isClosing) {
                                logger.error("Error processing error message", e);
                            }
                            break;
                        }
                    }

                    logger.info("Exited error processing loop. isClosing={}, process.isAlive={}",
                               isClosing, process != null ? process.isAlive() : "process is null");

                } catch (IOException e) {
                    if (!isClosing) {
                        logger.error("Error reading from error stream", e);
                    }
                } finally {
                    if (!isClosing) {
                        isClosing = true;
                    }
                    errorSink.tryEmitComplete();
                    if (processErrorReader != null) {
                        try {
                            processErrorReader.close();
                        } catch (IOException e) {
                            logger.error("Error closing error stream reader", e);
                        }
                    }
                    logger.info("Error processing thread terminated");
                }
            }
        });
    }

    private void handleIncomingMessages(final Function<CompletableFuture<JSONRPCMessage>, CompletableFuture<JSONRPCMessage>> inboundMessageHandler) {
        this.inboundSink.asFlux()
                .subscribe(new Consumer<JSONRPCMessage>() {
                    @Override
                    public void accept(final JSONRPCMessage message) {
                        if (message != null && !isClosing) {
                            CompletableFuture<JSONRPCMessage> messageFuture = CompletableFuture.completedFuture(message);
                            try {
                                CompletableFuture<JSONRPCMessage> resultFuture = inboundMessageHandler.apply(messageFuture);
                                // Add a handler for the result future to catch any errors
                                resultFuture.exceptionally(error -> {
                                    if (!isClosing) {
                                        logger.error("Error in message handler result", error);
                                    }
                                    return null;
                                });
                            } catch (Exception e) {
                                if (!isClosing) {
                                    logger.error("Error processing inbound message", e);
                                }
                            }
                        }
                    }
                });
    }

    private void handleIncomingErrors() {
        this.errorSink.asFlux().subscribe(new Consumer<String>() {
            @Override
            public void accept(String e) {
                stdErrorHandler.accept(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> sendMessage(JSONRPCMessage message) {
        if (this.outboundSink.tryEmitNext(message).isSuccess()) {
            // TODO: essentially we could reschedule ourselves in some time and make
            // another attempt with the already read data but pause reading until
            // success
            // In this approach we delegate the retry and the backpressure onto the
            // caller. This might be enough for most cases.
            return CompletableFuture.completedFuture(null);
        } else {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(new RuntimeException("Failed to enqueue message"));
            return future;
        }
    }

    /**
     * Starts the inbound processing thread that reads JSON-RPC messages from the
     * process's input stream. Messages are deserialized and emitted to the inbound sink.
     */
    private void startInboundProcessing() {
        this.inboundScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                BufferedReader processReader = null;
                try {
                    logger.info("Starting inbound processing thread");
                    processReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    String line;

                    // Check if process is still alive before starting to read
                    if (!process.isAlive()) {
                        logger.warn("Process is not alive before starting to read input stream");
                        return;
                    }

                    logger.info("Inbound processing thread ready to read");
                    while (!isClosing && process.isAlive() && (line = processReader.readLine()) != null) {
                        final String currentLine = line;
                        logger.debug("Received line: {}", currentLine);

                        if (currentLine.trim().isEmpty()) {
                            logger.debug("Skipping empty line");
                            continue;
                        }

                        try {
                            JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(objectMapper, currentLine);
                            logger.debug("Deserialized message: {}", message);

                            if (!inboundSink.tryEmitNext(message).isSuccess()) {
                                if (!isClosing) {
                                    logger.error("Failed to enqueue inbound message: {}", message);
                                }
                                break;
                            }
                        } catch (Exception e) {
                            if (!isClosing) {
                                logger.error("Error processing inbound message for line: " + currentLine, e);
                            }
                            // Don't break here, try to continue processing other messages
                        }
                    }

                    logger.info("Exited inbound processing loop. isClosing={}, process.isAlive={}",
                               isClosing, process != null ? process.isAlive() : "process is null");

                } catch (IOException e) {
                    if (!isClosing) {
                        logger.error("Error reading from input stream", e);
                    }
                } finally {
                    if (!isClosing) {
                        isClosing = true;
                    }
                    inboundSink.tryEmitComplete();
                    if (processReader != null) {
                        try {
                            processReader.close();
                        } catch (IOException e) {
                            logger.error("Error closing input stream reader", e);
                        }
                    }
                    logger.info("Inbound processing thread terminated");
                }
            }
        });
    }

    /**
     * Starts the outbound processing thread that writes JSON-RPC messages to the
     * process's output stream. Messages are serialized to JSON and written with a newline
     * delimiter.
     */
    private void startOutboundProcessing() {
        this.handleOutbound(new Function<Flux<JSONRPCMessage>, Flux<JSONRPCMessage>>() {
            @Override
            public Flux<JSONRPCMessage> apply(Flux<JSONRPCMessage> messages) {
                logger.info("Starting outbound processing");
                return messages
                        // this bit is important since writes come from user threads and we
                        // want to ensure that the actual writing happens on a dedicated thread
                        .publishOn(outboundScheduler)
                        .handle(new BiConsumer<JSONRPCMessage, SynchronousSink<JSONRPCMessage>>() {
                            @Override
                            public void accept(JSONRPCMessage message, SynchronousSink<JSONRPCMessage> s) {
                                if (message != null && !isClosing && process != null && process.isAlive()) {
                                    try {
                                        logger.debug("Serializing outbound message: {}", message);
                                        String jsonMessage = objectMapper.writeValueAsString(message);
                                        // Escape any embedded newlines in the JSON message as per spec:
                                        // https://spec.modelcontextprotocol.io/specification/basic/transports/#stdio
                                        // - Messages are delimited by newlines, and MUST NOT contain
                                        // embedded newlines.
                                        jsonMessage = jsonMessage.replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n");

                                        logger.debug("Sending message: {}", jsonMessage);

                                        final java.io.OutputStream os = process.getOutputStream();
                                        if (os != null) {
                                            synchronized (os) {
                                                os.write(jsonMessage.getBytes(StandardCharsets.UTF_8));
                                                os.write("\n".getBytes(StandardCharsets.UTF_8));
                                                os.flush();
                                                logger.debug("Message sent successfully");
                                            }
                                            s.next(message);
                                        } else {
                                            logger.error("Output stream is null");
                                            s.error(new RuntimeException("Output stream is null"));
                                        }
                                    } catch (IOException e) {
                                        logger.error("Error writing to output stream", e);
                                        s.error(new RuntimeException(e));
                                    }
                                } else {
                                    if (isClosing) {
                                        logger.debug("Skipping message send because transport is closing");
                                    } else if (process == null) {
                                        logger.error("Process is null");
                                        s.error(new RuntimeException("Process is null"));
                                    } else if (!process.isAlive()) {
                                        logger.error("Process is not alive");
                                        s.error(new RuntimeException("Process is not alive"));
                                    }
                                }
                            }
                        });
            }
        });
    }

    protected void handleOutbound(Function<Flux<JSONRPCMessage>, Flux<JSONRPCMessage>> outboundConsumer) {
        outboundConsumer.apply(outboundSink.asFlux())
                .doOnComplete(new Runnable() {
                    @Override
                    public void run() {
                        isClosing = true;
                        outboundSink.tryEmitComplete();
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) {
                        if (!isClosing) {
                            logger.error("Error in outbound processing", e);
                            isClosing = true;
                            outboundSink.tryEmitComplete();
                        }
                    }
                })
                .subscribe();
    }

    /**
     * Gracefully closes the transport by destroying the process and disposing of the
     * schedulers. This method sends a TERM signal to the process and waits for it to exit
     * before cleaning up resources.
     *
     * @return A CompletableFuture that completes when the transport is closed
     */
    @Override
    public CompletableFuture<Void> closeGracefully() {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        // 创建一个单独的线程池来处理关闭操作
        java.util.concurrent.ExecutorService executor =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "mcp-shutdown");
                t.setDaemon(false); // 使用非守护线程，确保它能完成
                return t;
            });

        // Run the shutdown process in a separate thread
        CompletableFuture.runAsync(() -> {
            try {
                isClosing = true;
                logger.info("Initiating graceful shutdown");

                // First complete all sinks to stop accepting new messages
                inboundSink.tryEmitComplete();
                outboundSink.tryEmitComplete();
                errorSink.tryEmitComplete();

                // Give a short time for any pending messages to be processed
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                logger.info("Sending TERM to process");
                if (process != null) {
                    // First try a graceful shutdown
                    process.destroy();

                    // Wait for the process to exit with a timeout
                    boolean exited = false;
                    try {
                        exited = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
                        if (exited) {
                            if (process.exitValue() != 0) {
                                logger.warn("Process terminated with code " + process.exitValue());
                            } else {
                                logger.info("Process terminated normally");
                            }
                        } else {
                            // Process didn't exit within timeout, force kill
                            logger.warn("Process did not exit within timeout, forcing termination");
                            process.destroyForcibly();

                            // Wait again with timeout
                            exited = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                            if (exited) {
                                logger.info("Process forcibly terminated with code " + process.exitValue());
                            } else {
                                logger.error("Failed to terminate process even with force");
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Process wait interrupted", e);
                    }
                } else {
                    logger.warn("Process not started");
                }

                // The Threads are blocked on readLine so disposeGracefully would not
                // interrupt them, therefore we issue an async hard dispose.
                logger.info("Disposing schedulers");
                inboundScheduler.dispose();
                errorScheduler.dispose();
                outboundScheduler.dispose();

                // 注意：我们不在这里关闭SHARED_SCHEDULER，因为它是静态共享的
                // 它将在JVM退出时由我们注册的关闭钩子关闭

                logger.info("Graceful shutdown completed");
                future.complete(null);
            } catch (Exception e) {
                logger.error("Error during graceful shutdown", e);
                future.completeExceptionally(e);
            } finally {
                // 关闭执行器
                executor.shutdown();
                try {
                    // 等待执行器关闭
                    if (!executor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                        logger.warn("Executor did not terminate in the specified time.");
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    executor.shutdownNow();
                }
            }
        }, executor);

        return future;
    }

    public Sinks.Many<String> getErrorSink() {
        return this.errorSink;
    }

    @Override
    public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
        return this.objectMapper.convertValue(data, typeRef);
    }

    /**
     * Configures the ObjectMapper to handle non-standard getter methods.
     *
     * @param objectMapper the ObjectMapper to configure
     * @return the configured ObjectMapper
     */
    private ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
        // In the JDK17 version, we don't modify the ObjectMapper
        // Let's just return the original mapper to match the JDK17 behavior
        return objectMapper;
    }

}

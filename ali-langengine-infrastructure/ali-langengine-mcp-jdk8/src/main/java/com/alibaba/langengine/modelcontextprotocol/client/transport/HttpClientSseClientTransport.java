/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package com.alibaba.langengine.modelcontextprotocol.client.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alibaba.langengine.modelcontextprotocol.client.transport.FlowSseClient.SseEvent;
import com.alibaba.langengine.modelcontextprotocol.spec.JSONRPCMessage;
import com.alibaba.langengine.modelcontextprotocol.spec.McpClientTransport;
import com.alibaba.langengine.modelcontextprotocol.spec.McpError;
import com.alibaba.langengine.modelcontextprotocol.spec.McpSchema;
import com.alibaba.langengine.modelcontextprotocol.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Server-Sent Events (SSE) implementation of the
 * {@link com.alibaba.langengine.modelcontextprotocol.spec.McpTransport} that follows the MCP HTTP with SSE
 * transport specification, using Java's HttpClient.
 *
 * <p>
 * This transport implementation establishes a bidirectional communication channel between
 * client and server using SSE for server-to-client messages and HTTP POST requests for
 * client-to-server messages. The transport:
 * <ul>
 * <li>Establishes an SSE connection to receive server messages</li>
 * <li>Handles endpoint discovery through SSE events</li>
 * <li>Manages message serialization/deserialization using Jackson</li>
 * <li>Provides graceful connection termination</li>
 * <li>Supports custom HTTP headers for authentication and authorization</li>
 * </ul>
 *
 * <p>
 * The transport supports two types of SSE events:
 * <ul>
 * <li>'endpoint' - Contains the URL for sending client messages</li>
 * <li>'message' - Contains JSON-RPC message payload</li>
 * </ul>
 *
 * <p>
 * Custom headers can be provided through the constructor or builder to support
 * authentication and authorization requirements. These headers will be sent with
 * both the SSE connection and individual POST requests.
 *
 * @author Christian Tzolov
 * @see com.alibaba.langengine.modelcontextprotocol.spec.McpTransport
 * @see McpClientTransport
 */
@Slf4j
public class HttpClientSseClientTransport implements McpClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientSseClientTransport.class);

    /**
     * Default timeout for endpoint discovery in seconds
     */
    private static final long DEFAULT_ENDPOINT_DISCOVERY_TIMEOUT_SECONDS = 10;

    /**
     * SSE event type for JSON-RPC messages
     */
    private static final String MESSAGE_EVENT_TYPE = "message";

    /**
     * SSE event type for endpoint discovery
     */
    private static final String ENDPOINT_EVENT_TYPE = "endpoint";

    /**
     * Default SSE endpoint path
     */
    private static final String DEFAULT_SSE_ENDPOINT = "/sse";

    /**
     * Shared thread pool for all transport instances
     */
    private static final ExecutorService SHARED_EXECUTOR_SERVICE;

    static {
        // 创建一个固定大小的线程池，可以根据实际需求调整参数
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, // 核心线程数
            50, // 最大线程数
            60L, // 空闲线程存活时间
            TimeUnit.SECONDS, // 时间单位
            new LinkedBlockingQueue<>(1000), // 工作队列
            new ThreadFactory() {
                private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = defaultFactory.newThread(r);
                    thread.setName("mcp-transport-" + thread.getName());
                    thread.setDaemon(true); // 设置为守护线程
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程处理
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        SHARED_EXECUTOR_SERVICE = executor;
    }

    /**
     * Base URI for the MCP server
     */
    private final String baseUri;

    /**
     * SSE endpoint path
     */
    private final String sseEndpoint;

    /**
     * SSE client for handling server-sent events
     */
    private final FlowSseClient sseClient;

    /**
     * JSON object mapper for message serialization/deserialization
     */
    protected ObjectMapper objectMapper;

    /**
     * Custom HTTP headers for authentication and authorization
     */
    private final Map<String, String> headers;

    /**
     * Flag indicating if the transport is in closing state
     */
    private volatile boolean isClosing = false;

    /**
     * Holds the discovered message endpoint URL
     */
    private final AtomicReference<String> messageEndpoint = new AtomicReference<String>();

    /**
     * Future that completes when the endpoint is discovered
     */
    private final CompletableFuture<String> endpointDiscoveryFuture = new CompletableFuture<>();

    /**
     * Holds the SSE connection future
     */
    private final AtomicReference<CompletableFuture<Void>> connectionFuture = new AtomicReference<CompletableFuture<Void>>();

    /**
     * Connection timeout in milliseconds
     */
    private final int connectTimeoutMs;

    /**
     * Endpoint discovery timeout in seconds
     */
    private final long endpointDiscoveryTimeoutSeconds;

    /**
     * Creates a new transport instance with default settings.
     *
     * @param baseUri the base URI of the MCP server
     */
    public HttpClientSseClientTransport(String baseUri) {
        this(baseUri, new ObjectMapper());
    }

    /**
     * Creates a new transport instance with custom object mapper.
     *
     * @param baseUri      the base URI of the MCP server
     * @param objectMapper the object mapper for JSON serialization/deserialization
     */
    public HttpClientSseClientTransport(String baseUri, ObjectMapper objectMapper) {
        this(SHARED_EXECUTOR_SERVICE, baseUri, DEFAULT_SSE_ENDPOINT, objectMapper, 10000, Collections.emptyMap(), DEFAULT_ENDPOINT_DISCOVERY_TIMEOUT_SECONDS);
    }

    /**
     * Creates a new transport instance with custom settings.
     *
     * @param executorService  the executor service to use
     * @param baseUri         the base URI of the MCP server
     * @param sseEndpoint     the SSE endpoint path
     * @param objectMapper    the object mapper for JSON serialization/deserialization
     * @param connectTimeoutMs connection timeout in milliseconds
     * @param headers         custom HTTP headers for authentication and authorization
     * @param endpointDiscoveryTimeoutSeconds timeout for endpoint discovery in seconds
     */
    public HttpClientSseClientTransport(ExecutorService executorService, String baseUri, String sseEndpoint,
                                      ObjectMapper objectMapper, int connectTimeoutMs, Map<String, String> headers,
                                      long endpointDiscoveryTimeoutSeconds) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        Assert.hasText(baseUri, "baseUri must not be empty");
        Assert.notNull(executorService, "executorService must not be null");
        Assert.isTrue(endpointDiscoveryTimeoutSeconds > 0, "endpointDiscoveryTimeoutSeconds must be positive");
        
        this.baseUri = baseUri;
        this.sseEndpoint = sseEndpoint;
        this.objectMapper = configureObjectMapper(objectMapper);
        this.connectTimeoutMs = connectTimeoutMs;
        this.headers = new HashMap<>(headers != null ? headers : Collections.emptyMap());
        this.endpointDiscoveryTimeoutSeconds = endpointDiscoveryTimeoutSeconds;
        this.sseClient = new FlowSseClient(executorService);
    }

    /**
     * Creates a new builder for {@link HttpClientSseClientTransport}.
     *
     * @param baseUri the base URI of the MCP server
     * @return a new builder instance
     */
    public static Builder builder(String baseUri) {
        return new Builder(baseUri);
    }

    /**
     * Builder for {@link HttpClientSseClientTransport}.
     */
    public static class Builder {
        private final String baseUri;
        private String sseEndpoint = DEFAULT_SSE_ENDPOINT;
        private ObjectMapper objectMapper = new ObjectMapper();
        private int connectTimeoutMs = 10000;
        private Map<String, String> headers = new HashMap<>();
        private long endpointDiscoveryTimeoutSeconds = DEFAULT_ENDPOINT_DISCOVERY_TIMEOUT_SECONDS;

        public Builder(String baseUri) {
            Assert.hasText(baseUri, "baseUri must not be empty");
            this.baseUri = baseUri;
        }

        public Builder sseEndpoint(String sseEndpoint) {
            this.sseEndpoint = sseEndpoint;
            return this;
        }

        public Builder connectTimeout(int connectTimeoutMs) {
            Assert.isTrue(connectTimeoutMs > 0, "connectTimeoutMs must be positive");
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            Assert.notNull(objectMapper, "objectMapper must not be null");
            this.objectMapper = objectMapper;
            return this;
        }
        
        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers = new HashMap<>(headers);
            }
            return this;
        }
        
        public Builder header(String name, String value) {
            Assert.hasText(name, "Header name must not be empty");
            Assert.notNull(value, "Header value must not be null");
            this.headers.put(name, value);
            return this;
        }

        public Builder endpointDiscoveryTimeout(long timeoutSeconds) {
            Assert.isTrue(timeoutSeconds > 0, "timeoutSeconds must be positive");
            this.endpointDiscoveryTimeoutSeconds = timeoutSeconds;
            return this;
        }

        public HttpClientSseClientTransport build() {
            return new HttpClientSseClientTransport(
                SHARED_EXECUTOR_SERVICE,
                baseUri,
                sseEndpoint,
                objectMapper,
                connectTimeoutMs,
                headers,
                endpointDiscoveryTimeoutSeconds
            );
        }
    }

    /**
     * Establishes the SSE connection with the server and sets up message handling.
     *
     * <p>
     * This method:
     * <ul>
     * <li>Initiates the SSE connection</li>
     * <li>Handles endpoint discovery events</li>
     * <li>Processes incoming JSON-RPC messages</li>
     * </ul>
     *
     * @param handler the function to process received JSON-RPC messages
     * @return a CompletableFuture that completes when the connection is established
     */
    @Override
    public CompletableFuture<Void> connect(final Function<CompletableFuture<JSONRPCMessage>, CompletableFuture<JSONRPCMessage>> handler) {
        final CompletableFuture<Void> future = new CompletableFuture<Void>();
        connectionFuture.set(future);

        sseClient.subscribe(this.baseUri + this.sseEndpoint, this.headers, new FlowSseClient.SseEventHandler() {
            @Override
            public void onEvent(SseEvent event) {
                if (isClosing) {
                    return;
                }

                try {
                    if (ENDPOINT_EVENT_TYPE.equals(event.getType())) {
                        String endpoint = event.getData();
                        messageEndpoint.set(endpoint);
                        endpointDiscoveryFuture.complete(endpoint);
                        future.complete(null);
                    } else if (MESSAGE_EVENT_TYPE.equals(event.getType())) {
                        JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(objectMapper, event.getData());
                        CompletableFuture<JSONRPCMessage> messageFuture = CompletableFuture.completedFuture(message);
                        handler.apply(messageFuture);
                    } else {
                        logger.error("Received unrecognized SSE event type: {}", event.getType());
                    }
                } catch (IOException e) {
                    logger.error("Error processing SSE event", e);
                    future.completeExceptionally(e);
                    endpointDiscoveryFuture.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                if (!isClosing) {
                    logger.error("SSE connection error", error);
                    future.completeExceptionally(error);
                    endpointDiscoveryFuture.completeExceptionally(error);
                }
            }
        });

        // 添加超时处理
        SHARED_EXECUTOR_SERVICE.execute(() -> {
            try {
                endpointDiscoveryFuture.get(endpointDiscoveryTimeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                String errorMsg = String.format("Endpoint discovery timed out after %d seconds", endpointDiscoveryTimeoutSeconds);
                logger.error(errorMsg);
                future.completeExceptionally(new McpError(errorMsg));
            } catch (Exception e) {
                logger.error("Error during endpoint discovery", e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Sends a JSON-RPC message to the server.
     *
     * <p>
     * This method waits for the message endpoint to be discovered before sending the
     * message. The message is serialized to JSON and sent as an HTTP POST request.
     *
     * @param message the JSON-RPC message to send
     * @return a CompletableFuture that completes when the message is sent
     * @throws McpError if the message endpoint is not available or the wait times out
     */
    @Override
    public CompletableFuture<Void> sendMessage(final JSONRPCMessage message) {
        if (isClosing) {
            return CompletableFuture.completedFuture(null);
        }

        return endpointDiscoveryFuture.thenCompose(endpoint -> {
            if (endpoint == null) {
                CompletableFuture<Void> errorFuture = new CompletableFuture<>();
                errorFuture.completeExceptionally(new McpError("No message endpoint available"));
                return errorFuture;
            }

            final CompletableFuture<Void> future = new CompletableFuture<>();

            SHARED_EXECUTOR_SERVICE.submit(() -> {
                HttpURLConnection connection = null;
                try {
                    logger.debug("Sending message to endpoint {}: {}", endpoint, message);
                    String jsonText = objectMapper.writeValueAsString(message);
                    URL url = new URL(baseUri + endpoint);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setConnectTimeout(1200000);
                    connection.setDoOutput(true);
                    connection.setReadTimeout(1200000);

                    log.info("connection readTimeout is 1200000");

                    // Apply custom headers
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }

                    try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                        outputStream.write(jsonText.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }

                    int statusCode = connection.getResponseCode();

                    // 这些都认为是失败的
                    if (!(statusCode >= 200 && statusCode < 300)) {
                        String errorMsg = String.format("Failed to send message, status code: %d", statusCode);
                        logger.error(errorMsg);
                        future.completeExceptionally(new McpError(errorMsg));
                        return;
                    }

                    future.complete(null);
                } catch (Exception e) {
                    if (!isClosing) {
                        logger.error("Failed to send message", e);
                        future.completeExceptionally(new McpError("Failed to send message: " + e.getMessage()));
                    } else {
                        future.complete(null);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });

            return future;
        });
    }

    /**
     * Gracefully closes the transport connection.
     *
     * <p>
     * Sets the closing flag and cancels any pending connection future. This prevents new
     * messages from being sent and allows ongoing operations to complete.
     *
     * @return a CompletableFuture that completes when the closing process is initiated
     */
    @Override
    public CompletableFuture<Void> closeGracefully() {
        return CompletableFuture.runAsync(() -> {
            isClosing = true;
            CompletableFuture<Void> future = connectionFuture.get();
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }

            if (!endpointDiscoveryFuture.isDone()) {
                endpointDiscoveryFuture.completeExceptionally(new McpError("Connection closing"));
            }

            if (sseClient != null) {
                sseClient.close();
            }
        }, SHARED_EXECUTOR_SERVICE);
    }

    /**
     * Unmarshals data to the specified type using the configured object mapper.
     *
     * @param data    the data to unmarshal
     * @param typeRef the type reference for the target type
     * @param <T>     the target type
     * @return the unmarshalled object
     */
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
        // Create a copy of the ObjectMapper to avoid modifying the original
        ObjectMapper configuredMapper = objectMapper.copy();
        
        // Configure visibility for methods without 'get' prefix
        configuredMapper.setVisibility(com.fasterxml.jackson.annotation.PropertyAccessor.GETTER, 
                                      com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY);
        configuredMapper.setVisibility(com.fasterxml.jackson.annotation.PropertyAccessor.FIELD, 
                                      com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY);
        
        return configuredMapper;
    }

}

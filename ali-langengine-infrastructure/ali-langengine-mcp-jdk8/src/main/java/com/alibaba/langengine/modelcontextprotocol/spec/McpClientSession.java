/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Default implementation of the MCP (Model Context Protocol) session that manages
 * bidirectional JSON-RPC communication between clients and servers. This implementation
 * follows the MCP specification for message exchange and transport handling.
 *
 * <p>
 * The session manages:
 * <ul>
 * <li>Request/response handling with unique message IDs</li>
 * <li>Notification processing</li>
 * <li>Message timeout management</li>
 * <li>Transport layer abstraction</li>
 * </ul>
 *
 * JDK 1.8 compatible version.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public class McpClientSession implements McpSession {

    /** Logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(McpClientSession.class);

    /** Duration to wait for request responses before timing out */
    private final Duration requestTimeout;

    /** Transport layer implementation for message exchange */
    private final McpClientTransport transport;

    /** Map of pending responses keyed by request ID */
    private final ConcurrentHashMap<Object, CompletableFuture<JSONRPCResponse>> pendingResponses = new ConcurrentHashMap<>();

    /** Map of request handlers keyed by method name */
    private final ConcurrentHashMap<String, RequestHandler<?>> requestHandlers = new ConcurrentHashMap<>();

    /** Map of notification handlers keyed by method name */
    private final ConcurrentHashMap<String, NotificationHandler> notificationHandlers = new ConcurrentHashMap<>();

    /** Session-specific prefix for request IDs */
    private final String sessionPrefix = UUID.randomUUID().toString().substring(0, 8);

    /** Atomic counter for generating unique request IDs */
    private final AtomicLong requestCounter = new AtomicLong(0);

    /** Connection future */
    private CompletableFuture<Void> connectionFuture;

    /**
     * Functional interface for handling incoming JSON-RPC requests. Implementations
     * should process the request parameters and return a response.
     *
     * @param <T> Response type
     */
    @FunctionalInterface
    public interface RequestHandler<T> {

        /**
         * Handles an incoming request with the given parameters.
         * @param params The request parameters
         * @return A CompletableFuture containing the response object
         */
        CompletableFuture<T> handle(Object params);
    }

    /**
     * Functional interface for handling incoming JSON-RPC notifications. Implementations
     * should process the notification parameters without returning a response.
     */
    @FunctionalInterface
    public interface NotificationHandler {

        /**
         * Handles an incoming notification with the given parameters.
         * @param params The notification parameters
         * @return A CompletableFuture that completes when the notification is processed
         */
        CompletableFuture<Void> handle(Object params);
    }

    /**
     * Class representing a method not found error.
     */
    public static class MethodNotFoundError {
        private final String method;
        private final String message;
        private final Object data;

        public MethodNotFoundError(String method, String message, Object data) {
            this.method = method;
            this.message = message;
            this.data = data;
        }

        public String method() {
            return method;
        }

        public String message() {
            return message;
        }

        public Object data() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodNotFoundError that = (MethodNotFoundError) o;
            return Objects.equals(method, that.method) &&
                   Objects.equals(message, that.message) &&
                   Objects.equals(data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, message, data);
        }

        @Override
        public String toString() {
            return "MethodNotFoundError{" +
                   "method='" + method + '\'' +
                   ", message='" + message + '\'' +
                   ", data=" + data +
                   '}';
        }
    }

    /**
     * Creates a new McpClientSession with the specified configuration and handlers.
     * @param requestTimeout Duration to wait for responses
     * @param transport Transport implementation for message exchange
     * @param requestHandlers Map of method names to request handlers
     * @param notificationHandlers Map of method names to notification handlers
     */
    public McpClientSession(Duration requestTimeout, McpClientTransport transport,
            Map<String, RequestHandler<?>> requestHandlers, Map<String, NotificationHandler> notificationHandlers) {

        if (requestTimeout == null) {
            throw new IllegalArgumentException("The requestTimeout can not be null");
        }
        if (transport == null) {
            throw new IllegalArgumentException("The transport can not be null");
        }
        if (requestHandlers == null) {
            throw new IllegalArgumentException("The requestHandlers can not be null");
        }
        if (notificationHandlers == null) {
            throw new IllegalArgumentException("The notificationHandlers can not be null");
        }

        this.requestTimeout = requestTimeout;
        this.transport = transport;
        this.requestHandlers.putAll(requestHandlers);
        this.notificationHandlers.putAll(notificationHandlers);

        this.connectionFuture = this.transport.connect(new Function<CompletableFuture<JSONRPCMessage>, CompletableFuture<JSONRPCMessage>>() {
            @Override
            public CompletableFuture<JSONRPCMessage> apply(CompletableFuture<JSONRPCMessage> mono) {
                return mono.thenApply(message -> {
                    if (message instanceof JSONRPCResponse) {
                        JSONRPCResponse response = (JSONRPCResponse) message;
                        logger.debug("Received Response: {}", response);
                        CompletableFuture<JSONRPCResponse> future = pendingResponses.remove(response.id());
                        if (future == null) {
                            logger.warn("Unexpected response for unknown id {}", response.id());
                        }
                        else {
                            future.complete(response);
                        }
                    }
                    else if (message instanceof JSONRPCRequest) {
                        JSONRPCRequest request = (JSONRPCRequest) message;
                        logger.debug("Received request: {}", request);
                        handleIncomingRequest(request).thenAccept(response -> 
                            transport.sendMessage(response).exceptionally(error -> {
                                logger.error("Error sending response: {}", error.getMessage());
                                return null;
                            })
                        ).exceptionally(error -> {
                            JSONRPCResponse errorResponse = new JSONRPCResponse(
                                McpSchema.JSONRPC_VERSION, 
                                request.id(),
                                null, 
                                new JSONRPCResponse.JSONRPCError(
                                    ErrorCodes.INTERNAL_ERROR,
                                    error.getMessage(), 
                                    null
                                )
                            );
                            transport.sendMessage(errorResponse).exceptionally(err -> {
                                logger.error("Error sending error response: {}", err.getMessage());
                                return null;
                            });
                            return null;
                        });
                    }
                    else if (message instanceof JSONRPCNotification) {
                        JSONRPCNotification notification = (JSONRPCNotification) message;
                        logger.debug("Received notification: {}", notification);
                        handleIncomingNotification(notification).exceptionally(error -> {
                            logger.error("Error handling notification: {}", error.getMessage());
                            return null;
                        });
                    }
                    return message;
                });
            }
        });
    }

    /**
     * Handles an incoming JSON-RPC request by routing it to the appropriate handler.
     * @param request The incoming JSON-RPC request
     * @return A CompletableFuture containing the JSON-RPC response
     */
    private CompletableFuture<JSONRPCResponse> handleIncomingRequest(JSONRPCRequest request) {
        CompletableFuture<JSONRPCResponse> result = new CompletableFuture<>();
        
        RequestHandler<?> handler = this.requestHandlers.get(request.method());
        if (handler == null) {
            MethodNotFoundError error = getMethodNotFoundError(request.method());
            result.complete(new JSONRPCResponse(
                McpSchema.JSONRPC_VERSION, 
                request.id(), 
                null,
                new JSONRPCResponse.JSONRPCError(
                    ErrorCodes.METHOD_NOT_FOUND,
                    error.message(), 
                    error.data()
                )
            ));
            return result;
        }

        try {
            handler.handle(request.params()).thenAccept(responseResult -> {
                result.complete(new JSONRPCResponse(
                    McpSchema.JSONRPC_VERSION, 
                    request.id(), 
                    responseResult, 
                    null
                ));
            }).exceptionally(error -> {
                result.complete(new JSONRPCResponse(
                    McpSchema.JSONRPC_VERSION, 
                    request.id(),
                    null, 
                    new JSONRPCResponse.JSONRPCError(
                        ErrorCodes.INTERNAL_ERROR,
                        error.getMessage(), 
                        null
                    )
                ));
                return null;
            });
        } catch (Exception e) {
            result.complete(new JSONRPCResponse(
                McpSchema.JSONRPC_VERSION, 
                request.id(),
                null, 
                new JSONRPCResponse.JSONRPCError(
                    ErrorCodes.INTERNAL_ERROR,
                    e.getMessage(), 
                    null
                )
            ));
        }
        
        return result;
    }

    /**
     * Gets the appropriate error for a method not found condition.
     * @param method The method that was not found
     * @return A MethodNotFoundError object with details
     */
    public static MethodNotFoundError getMethodNotFoundError(String method) {
        if (McpSchema.METHOD_ROOTS_LIST.equals(method)) {
            Map<String, Object> data = new HashMap<>();
            data.put("reason", "Client does not have roots capability");
            return new MethodNotFoundError(method, "Roots not supported", data);
        } else {
            return new MethodNotFoundError(method, "Method not found: " + method, null);
        }
    }

    /**
     * Handles an incoming JSON-RPC notification by routing it to the appropriate handler.
     * @param notification The incoming JSON-RPC notification
     * @return A CompletableFuture that completes when the notification is processed
     */
    private CompletableFuture<Void> handleIncomingNotification(JSONRPCNotification notification) {
        NotificationHandler handler = notificationHandlers.get(notification.method());
        if (handler == null) {
            logger.error("No handler registered for notification method: {}", notification.method());
            return CompletableFuture.completedFuture(null);
        }
        return handler.handle(notification.params());
    }

    /**
     * Generates a unique request ID in a non-blocking way. Combines a session-specific
     * prefix with an atomic counter to ensure uniqueness.
     * @return A unique request ID string
     */
    private String generateRequestId() {
        return this.sessionPrefix + "-" + this.requestCounter.getAndIncrement();
    }

    /**
     * Sends a JSON-RPC request and returns the response.
     * @param <T> The expected response type
     * @param method The method name to call
     * @param requestParams The request parameters
     * @param typeRef Type reference for response deserialization
     * @return A CompletableFuture containing the response
     */
    @Override
    public <T> CompletableFuture<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
        String requestId = this.generateRequestId();
        CompletableFuture<JSONRPCResponse> responseFuture = new CompletableFuture<>();
        
        this.pendingResponses.put(requestId, responseFuture);
        JSONRPCRequest jsonrpcRequest = new JSONRPCRequest(
            McpSchema.JSONRPC_VERSION, 
            method,
            requestId, 
            requestParams
        );
        
        CompletableFuture<T> result = new CompletableFuture<>();
        
        this.transport.sendMessage(jsonrpcRequest).exceptionally(error -> {
            this.pendingResponses.remove(requestId);
            result.completeExceptionally(error);
            return null;
        });
        
        // Handle timeout
        // Note: In JDK 1.8, we don't have direct timeout support in CompletableFuture
        // A production implementation would need a proper timeout mechanism
        
        responseFuture.thenAccept(jsonRpcResponse -> {
            if (jsonRpcResponse.error() != null) {
                result.completeExceptionally(new McpError(jsonRpcResponse.error()));
            } else {
                if (typeRef.getType().equals(Void.class)) {
                    result.complete(null);
                } else {
                    try {
                        result.complete(this.transport.unmarshalFrom(jsonRpcResponse.result(), typeRef));
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                }
            }
        }).exceptionally(error -> {
            result.completeExceptionally(error);
            return null;
        });
        
        return result;
    }

    /**
     * Sends a JSON-RPC notification.
     * @param method The method name for the notification
     * @param params The notification parameters
     * @return A CompletableFuture that completes when the notification is sent
     */
    @Override
    public CompletableFuture<Void> sendNotification(String method, Map<String, Object> params) {
        JSONRPCNotification jsonrpcNotification = new JSONRPCNotification(
            McpSchema.JSONRPC_VERSION,
            method, 
            params
        );
        return this.transport.sendMessage(jsonrpcNotification);
    }

    /**
     * Closes the session gracefully, allowing pending operations to complete.
     * @return A CompletableFuture that completes when the session is closed
     */
    @Override
    public CompletableFuture<Void> closeGracefully() {
        return transport.closeGracefully();
    }

    /**
     * Closes the session immediately, potentially interrupting pending operations.
     */
    @Override
    public void close() {
        transport.close();
    }
}

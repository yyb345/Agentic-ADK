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
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a Model Control Protocol (MCP) session on the server side. It manages
 * bidirectional JSON-RPC communication with the client.
 *
 * JDK 1.8 compatible version.
 */
public class McpServerSession implements McpSession {

    private static final Logger logger = LoggerFactory.getLogger(McpServerSession.class);

    private final ConcurrentHashMap<Object, CompletableFuture<JSONRPCResponse>> pendingResponses = new ConcurrentHashMap<>();

    private final String id;

    private final AtomicLong requestCounter = new AtomicLong(0);

    private final InitRequestHandler initRequestHandler;

    private final InitNotificationHandler initNotificationHandler;

    private final Map<String, RequestHandler<?>> requestHandlers;

    private final Map<String, NotificationHandler> notificationHandlers;

    private final McpServerTransport transport;

    private final CompletableFuture<McpServerExchange> exchangeFuture = new CompletableFuture<>();

    private final AtomicReference<ClientCapabilities> clientCapabilities = new AtomicReference<>();

    private final AtomicReference<Implementation> clientInfo = new AtomicReference<>();

    private static final int STATE_UNINITIALIZED = 0;

    private static final int STATE_INITIALIZING = 1;

    private static final int STATE_INITIALIZED = 2;

    private final AtomicInteger state = new AtomicInteger(STATE_UNINITIALIZED);

    /**
     * Creates a new server session with the given parameters and the transport to use.
     * @param id session id
     * @param transport the transport to use
     * @param initHandler called when a
     * {@link InitializeRequest} is received by the
     * server
     * @param initNotificationHandler called when a
     * {@link McpSchema.METHOD_NOTIFICATION_INITIALIZED} is received.
     * @param requestHandlers map of request handlers to use
     * @param notificationHandlers map of notification handlers to use
     */
    public McpServerSession(String id, McpServerTransport transport, InitRequestHandler initHandler,
            InitNotificationHandler initNotificationHandler, Map<String, RequestHandler<?>> requestHandlers,
            Map<String, NotificationHandler> notificationHandlers) {
        this.id = id;
        this.transport = transport;
        this.initRequestHandler = initHandler;
        this.initNotificationHandler = initNotificationHandler;
        this.requestHandlers = requestHandlers;
        this.notificationHandlers = notificationHandlers;
    }

    /**
     * Retrieve the session id.
     * @return session id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Called upon successful initialization sequence between the client and the server
     * with the client capabilities and information.
     *
     * <a href=
     * "https://github.com/modelcontextprotocol/specification/blob/main/docs/specification/basic/lifecycle.md#initialization">Initialization
     * Spec</a>
     * @param clientCapabilities the capabilities the connected client provides
     * @param clientInfo the information about the connected client
     */
    public void init(ClientCapabilities clientCapabilities, Implementation clientInfo) {
        this.clientCapabilities.lazySet(clientCapabilities);
        this.clientInfo.lazySet(clientInfo);
    }

    private String generateRequestId() {
        return this.id + "-" + this.requestCounter.getAndIncrement();
    }

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
     * Called by the McpServerTransportProvider once the session is determined.
     * The purpose of this method is to dispatch the message to an appropriate handler as
     * specified by the MCP server implementation via
     * {@link Factory} that the server creates.
     * @param message the incoming JSON-RPC message
     * @return a CompletableFuture that completes when the message is processed
     */
    public CompletableFuture<Void> handle(JSONRPCMessage message) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        
        try {
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
                result.complete(null);
            }
            else if (message instanceof JSONRPCRequest) {
                JSONRPCRequest request = (JSONRPCRequest) message;
                logger.debug("Received request: {}", request);
                handleIncomingRequest(request).thenAccept(response -> 
                    this.transport.sendMessage(response).thenAccept(v -> result.complete(null))
                    .exceptionally(error -> {
                        result.completeExceptionally(error);
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
                    this.transport.sendMessage(errorResponse).thenAccept(v -> result.complete(null))
                    .exceptionally(err -> {
                        result.completeExceptionally(err);
                        return null;
                    });
                    return null;
                });
            }
            else if (message instanceof JSONRPCNotification) {
                JSONRPCNotification notification = (JSONRPCNotification) message;
                logger.debug("Received notification: {}", notification);
                handleIncomingNotification(notification).thenAccept(v -> result.complete(null))
                .exceptionally(error -> {
                    logger.error("Error handling notification: {}", error.getMessage());
                    result.complete(null); // Complete normally even on error
                    return null;
                });
            }
            else {
                logger.warn("Received unknown message type: {}", message);
                result.complete(null);
            }
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        
        return result;
    }

    /**
     * Handles an incoming JSON-RPC request by routing it to the appropriate handler.
     * @param request The incoming JSON-RPC request
     * @return A CompletableFuture containing the JSON-RPC response
     */
    private CompletableFuture<JSONRPCResponse> handleIncomingRequest(JSONRPCRequest request) {
        CompletableFuture<JSONRPCResponse> result = new CompletableFuture<>();
        
        try {
            CompletableFuture<?> resultFuture;
            
            if (McpSchema.METHOD_INITIALIZE.equals(request.method())) {
                // Handle initialization request
                InitializeRequest initializeRequest = transport.unmarshalFrom(
                    request.params(),
                    new TypeReference<InitializeRequest>() {}
                );

                this.state.lazySet(STATE_INITIALIZING);
                this.init(initializeRequest.capabilities(), initializeRequest.clientInfo());
                resultFuture = this.initRequestHandler.handle(initializeRequest);
            }
            else {
                // Handle regular request
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

                resultFuture = this.exchangeFuture.thenCompose(exchange -> 
                    handler.handle(exchange, request.params())
                );
            }
            
            resultFuture.thenAccept(responseResult -> {
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
     * Handles an incoming JSON-RPC notification by routing it to the appropriate handler.
     * @param notification The incoming JSON-RPC notification
     * @return A CompletableFuture that completes when the notification is processed
     */
    private CompletableFuture<Void> handleIncomingNotification(JSONRPCNotification notification) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        
        try {
            if (McpSchema.METHOD_NOTIFICATION_INITIALIZED.equals(notification.method())) {
                this.state.lazySet(STATE_INITIALIZED);
                McpServerExchange exchange = new McpServerExchange(this, clientCapabilities.get(), clientInfo.get());
                this.exchangeFuture.complete(exchange);
                this.initNotificationHandler.handle().thenAccept(v -> result.complete(null))
                .exceptionally(error -> {
                    result.completeExceptionally(error);
                    return null;
                });
            } else {
                NotificationHandler handler = notificationHandlers.get(notification.method());
                if (handler == null) {
                    logger.error("No handler registered for notification method: {}", notification.method());
                    result.complete(null);
                } else {
                    this.exchangeFuture.thenCompose(exchange -> 
                        handler.handle(exchange, notification.params())
                    ).thenAccept(v -> result.complete(null))
                    .exceptionally(error -> {
                        result.completeExceptionally(error);
                        return null;
                    });
                }
            }
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        
        return result;
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
     * Gets the appropriate error for a method not found condition.
     * @param method The method that was not found
     * @return A MethodNotFoundError object with details
     */
    static MethodNotFoundError getMethodNotFoundError(String method) {
        if (McpSchema.METHOD_ROOTS_LIST.equals(method)) {
            Map<String, Object> data = new HashMap<>();
            data.put("reason", "Client does not have roots capability");
            return new MethodNotFoundError(method, "Roots not supported", data);
        } else {
            return new MethodNotFoundError(method, "Method not found: " + method, null);
        }
    }

    @Override
    public CompletableFuture<Void> closeGracefully() {
        return this.transport.closeGracefully();
    }

    @Override
    public void close() {
        this.transport.close();
    }

    /**
     * Request handler for the initialization request.
     */
    public interface InitRequestHandler {

        /**
         * Handles the initialization request.
         * @param initializeRequest the initialization request by the client
         * @return a CompletableFuture that will emit the result of the initialization
         */
        CompletableFuture<InitializeResult> handle(InitializeRequest initializeRequest);
    }

    /**
     * Notification handler for the initialization notification from the client.
     */
    public interface InitNotificationHandler {

        /**
         * Specifies an action to take upon successful initialization.
         * @return a CompletableFuture that will complete when the initialization is acted upon.
         */
        CompletableFuture<Void> handle();
    }

    /**
     * A handler for client-initiated notifications.
     */
    public interface NotificationHandler {

        /**
         * Handles a notification from the client.
         * @param exchange the exchange associated with the client that allows calling
         * back to the connected client or inspecting its capabilities.
         * @param params the parameters of the notification.
         * @return a CompletableFuture that completes once the notification is handled.
         */
        CompletableFuture<Void> handle(McpServerExchange exchange, Object params);
    }

    /**
     * A handler for client-initiated requests.
     *
     * @param <T> the type of the response that is expected as a result of handling the
     * request.
     */
    public interface RequestHandler<T> {

        /**
         * Handles a request from the client.
         * @param exchange the exchange associated with the client that allows calling
         * back to the connected client or inspecting its capabilities.
         * @param params the parameters of the request.
         * @return a CompletableFuture that will emit the response to the request.
         */
        CompletableFuture<T> handle(McpServerExchange exchange, Object params);
    }

    /**
     * Factory for creating server sessions which delegate to a provided 1:1 transport
     * with a connected client.
     */
    @FunctionalInterface
    public interface Factory {

        /**
         * Creates a new 1:1 representation of the client-server interaction.
         * @param sessionTransport the transport to use for communication with the client.
         * @return a new server session.
         */
        McpServerSession create(McpServerTransport sessionTransport);
    }
}

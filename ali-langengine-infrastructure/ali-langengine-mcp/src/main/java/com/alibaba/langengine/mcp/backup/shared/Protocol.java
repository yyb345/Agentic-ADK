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
//package com.alibaba.langengine.mcp.shared;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.langengine.mcp.spec.*;
//import com.alibaba.langengine.mcp.spec.schema.PingRequest;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.function.BiConsumer;
//import java.util.function.BiFunction;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
///**
// * Implements MCP protocol framing on top of a pluggable transport, including
// * features like request/response linking, notifications, and progress.
// */
//@Slf4j
//@Data
//public abstract class Protocol {
//
//    private ProtocolOptions options;
//    private Transport transport;
//    private Map<String, BiFunction<JSONRPCRequest, RequestHandlerExtra, CompletableFuture<RequestResult>>> requestHandlers = new ConcurrentHashMap<>();
//    private Map<String, Consumer<JSONRPCNotification>> notificationHandlers = new ConcurrentHashMap<>();
//    private Map<Long, BiConsumer<JSONRPCResponse, Exception>> responseHandlers = new ConcurrentHashMap<>();
//    private Map<Long, ProgressCallback> progressHandlers = new HashMap<>();
//
//    public void onclose() {
//
//    }
//
//    public void onerror(Throwable error) {
//
//    }
//
//    private BiFunction<JSONRPCRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> fallbackRequestHandler;
//    private Consumer<JSONRPCNotification> fallbackNotificationHandler;
//
//    public Protocol(ProtocolOptions options) {
//        this.options = options;
//        initProtocol();
//    }
//
//    private void initProtocol() {
//        Function<ProgressNotification, CompletableFuture<Void>> notificationHandler = notification -> {
//            onProgress(notification);
//            return CompletableFuture.completedFuture(null);
//        };
//        setNotificationHandler(MethodDefined.NotificationsProgress.getValue(), notificationHandler);
//
//        BiFunction<PingRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> handler = (request, extra) ->
//            CompletableFuture.completedFuture(new EmptyRequestResult());
//        setRequestHandler(PingRequest.class, MethodDefined.Ping.getValue(), handler);
//
//
////        BiFunction<JSONRPCRequest, RequestHandlerExtra, CompletableFuture<SendResultT>> requestHandler = fallbackRequestHandler;
////        setRequestHandler(MethodDefined.Ping, requestHandler);
//
////        setRequestHandler(Method.Defined.Ping， { request, _ ->
////                @Suppress("UNCHECKED_CAST")
////                        EmptyRequestResult() as SendResultT
////        }
//
////        setRequestHandler<PingRequest>(Method.Defined.Ping) { request, _ ->
////                @Suppress("UNCHECKED_CAST")
////                        EmptyRequestResult() as SendResultT
////        }
//
////        this.notificationHandler = notification -> {
////            onProgress(notification);
////            return COMPLETED;
////        };
////
////        this.requestHandler = (request, extra) -> {
////            return (SendResultT) new EmptyRequestResult();
////        };
//    }
//
////    public <T extends Request> void setRequestHandler(
////            Method method,
////            BiFunction<T, RequestHandlerExtra, SendResultT> block) {
////        setRequestHandler(T.class, method, block);
////    }
//
////    private <T extends Notification> void setNotificationHandler(Method method, Function<T, CompletableFuture<Void>> handler) {
////        this.notificationHandlers.put(method.getValue(), notification -> {
//////            @SuppressWarnings("unchecked")
//////            return handle.apply(notification.fromJSON((Class<T>) notification.getClass()));
////        });
////    }
//
//    public <T extends Notification> void setNotificationHandler(String method, Function<T, CompletableFuture<Void>> handler) {
//        notificationHandlers.put(method, (notification -> {
//            @SuppressWarnings("unchecked")
//            T castedNotification = (T) notification;
//            handler.apply(castedNotification);
//        }));
//    }
//
//    public <T extends Request> void setRequestHandler(Type requestType,
//                                                      String method,
//                                                      BiFunction<T, RequestHandlerExtra, CompletableFuture<RequestResult>> handler) {
//        requestHandlers.put(method, (request, extraHandler) -> {
//            T result = null;
//            if (request.getParams() != null) {
//                result = JSON.parseObject(request.getParams().toJSONString(), requestType);
//            }
//
//            if (result != null) {
//                return handler.apply(result, extraHandler);
//            } else {
//                return CompletableFuture.completedFuture(new EmptyRequestResult());
//            }
//        });
//    }
//
////    public CompletableFuture<SendResultT> handleRequest(Method method, Request request, RequestHandlerExtra extra) {
////        if (requestHandlers.containsKey(method.getValue())) {
////            return requestHandlers.get(method.getValue()).apply(request, extra);
////        }
////        throw new IllegalArgumentException("No handler registered for method: " + method.getValue());
////    }
//
////    private <T extends Notification> void setNotificationHandler(Method method, Function<T, CompletableFuture<Void>> handler) {
////        this.notificationHandlers.put(method.getValue(), notification -> {
//////            @SuppressWarnings("unchecked")
//////            return handle.apply(notification.fromJSON((Class<T>) notification.getClass()));
////        });
////    }
//
////    private ObjectMapper objectMapper = new ObjectMapper();
////
////    public <T extends Request> void setRequestHandler(
////            TypeReference<T> requestTypeRef,
////            Method method,
////            BiFunction<T, RequestHandlerExtra, SendResultT> block
////    ) {
////        assertRequestHandlerCapability(method);
////
////        requestHandlers.put(method.getValue(), (request, extraHandler) -> {
////            T result = request.getParams() != null ? objectMapper.readValue(request.getParams(), requestTypeRef) : null;
////            SendResultT response;
////            if (result != null) {
////                response = block.apply(result, extraHandler);
////            } else {
////                response = (SendResultT)new EmptyRequestResult();
////            }
////
////            return response;
////        });
////    }
//
//    public CompletableFuture<Void> connect(Transport transport) {
//        this.transport = transport;
//
//        transport.setOnClose(this::onClose);
//        transport.setOnError(this::onError);
//
//        transport.setOnMessage(message -> {
//            log.info("transport:{}, onMessage:{}", transport.getClass().getName(), JSON.toJSONString(message));
//            if (message instanceof JSONRPCResponse) {
//                onResponse((JSONRPCResponse) message, null);
//            } else if (message instanceof JSONRPCRequest) {
//                onRequest((JSONRPCRequest) message);
//            } else if (message instanceof JSONRPCNotification) {
//                onNotification((JSONRPCNotification) message);
//            } else if (message instanceof JSONRPCError) {
//                onResponse(null, (JSONRPCError) message);
//            }
//            return CompletableFuture.completedFuture(null);
//        });
//
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                transport.start();
//                return null;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    private void onClose() {
//        McpErrorException error = new McpErrorException(ErrorCodeDefined.ConnectionClosed.getCode(), "Connection closed");
//        responseHandlers.forEach((id, handler) -> handler.accept(null, error));
//        responseHandlers.clear();
//        progressHandlers.clear();
//        transport = null;
//        onclose();
//    }
//
//    private void onError(Throwable error) {
//        onerror(error);
//    }
//
//    private CompletableFuture<Void> onNotification(JSONRPCNotification notification) {
//        log.info("Received notification: {}", notification.getMethod());
//        Consumer<JSONRPCNotification> function = notificationHandlers.get(notification.getMethod());
//        Consumer<JSONRPCNotification> handler = (function != null) ? function : fallbackNotificationHandler;
//
//        if (handler == null) {
//            log.error("No handler found for notification: {}", notification.getMethod());
//            return CompletableFuture.completedFuture(null);
//        }
//
//        return CompletableFuture.runAsync(() -> {
//            try {
//                handler.accept(notification);
//            } catch (Throwable cause) {
//                log.error("Error handling notification: {}", notification.getMethod(), cause);
//                onError(cause);
//            }
//        });
//    }
//
//    private CompletableFuture<Void> onRequest(JSONRPCRequest request) {
//        log.info("Received request: {} (id: {})", request.getMethod(), request.getId());
//        BiFunction<JSONRPCRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> handler = requestHandlers.getOrDefault(request.getMethod(), fallbackRequestHandler);
//
//        if (handler == null) {
//            log.error("No handler found for request: {}", request.getMethod());
//            try {
//                JSONRPCError jsonrpcError = new JSONRPCError();
//                jsonrpcError.setCode(ErrorCodeDefined.MethodNotFound.getCode());
//                jsonrpcError.setMessage("Server does not support " + request.getMethod());
//                JSONRPCResponse response = new JSONRPCResponse();
//                response.setId(request.getId());
//                response.setError(jsonrpcError);
//                transport.send(response);
//            } catch (Throwable cause) {
//                log.error("Error sending method not found response", cause);
//                onError(cause);
//            }
//        }
//
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                Object result = handler.apply(request, new RequestHandlerExtra());
//                log.info("Request handled successfully: {} (id: {})", request.getMethod(), request.getId());
//                JSONRPCResponse jsonrpcResponse = new JSONRPCResponse();
//                jsonrpcResponse.setId(request.getId());
//                jsonrpcResponse.setResult(result);
//                return jsonrpcResponse;
//            } catch (Throwable cause) {
//                log.error("Error handling request: {} (id: {})", request.getMethod(), request.getId(), cause);
//                JSONRPCError jsonrpcError = new JSONRPCError();
//                jsonrpcError.setCode(ErrorCodeDefined.InternalError.getCode());
//                jsonrpcError.setMessage(cause.getMessage() != null ? cause.getMessage() : "Internal error");
//                JSONRPCResponse jsonrpcResponse = new JSONRPCResponse();
//                jsonrpcResponse.setId(request.getId());
//                jsonrpcResponse.setError(jsonrpcError);
//                return jsonrpcResponse;
//            }
//        }).thenAccept(response -> {
//            try {
//                transport.send(response);
//            } catch (Throwable cause) {
//                log.error("Error sending response", cause);
//                onError(cause);
//            }
//        });
//    }
//
//    private void onProgress(ProgressNotification notification) {
//        log.trace("Received progress notification: token={}, progress={}/{}",
//                notification.getProgressToken(), notification.getProgress(), notification.getTotal());
//
//        Integer progress = notification.getProgress();
//        Double total = notification.getTotal();
//        Long progressToken = notification.getProgressToken();
//
//        ProgressCallback handler = progressHandlers.get(progressToken);
//        if (handler == null) {
//            String errorJson = JSON.toJSONString(notification); // Assuming McpJson is some utility to encode objects to JSON
//            String errorMessage = "Received a progress notification for an unknown token: " + errorJson;
//            log.error(errorMessage);
//            onError(new Error(errorMessage));
//            return;
//        }
//
//        Progress progressObj = new Progress();
//        progressObj.setProgress(progress);
//        progressObj.setTotal(total);
//        handler.accept(progressObj);
//    }
//
//    private void onResponse(JSONRPCResponse response, JSONRPCError error) {
//        String messageId = response != null ? response.getId().toString() : null;
//        BiConsumer<JSONRPCResponse, Exception> handler = responseHandlers.get(messageId);
//
//        if (handler == null) {
//            onError(new Exception("Received a response for an unknown message ID: " + JSON.toJSONString(response)));
//            return;
//        }
//
//        responseHandlers.remove(messageId);
//        progressHandlers.remove(messageId);
//
//        if (response != null) {
//            handler.accept(response, null);
//        } else {
//            if(error != null) {
//                McpErrorException mcpErrorException = new McpErrorException(error.getCode(), error.getMessage());
//                handler.accept(null, mcpErrorException);
//            }
//        }
//    }
//
//    public CompletableFuture<Void> close() {
//        if (transport != null) {
//            return CompletableFuture.runAsync(() -> {
//                try {
//                    transport.close();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        } else {
//            return CompletableFuture.completedFuture(null);
//        }
//    }
//
//    public abstract void assertCapabilityForMethod(Method method);
//
//    public abstract void assertNotificationCapability(Method method);
//
//    public abstract void assertRequestHandlerCapability(Method method);
//
//    public <T extends RequestResult> CompletableFuture<T> request(Request request,
//            RequestOptions options) {
//        log.info("Sending request: {}", request.getMethod());
//
//        if (transport == null) {
//            throw new IllegalStateException("Not connected");
//        }
//
//        if (this.options != null && this.options.getEnforceStrictCapabilities()) {
//            assertCapabilityForMethod(request.getMethod());
//        }
//
//        JSONRPCRequest message = new JSONRPCRequest(request.getMethod().getValue(), JSON.parseObject(JSON.toJSONString(request)));
//        Long messageId = message.getId();
//
//        CompletableFuture<T> result = new CompletableFuture<>();
//
//        if (options != null && options.getOnProgress() != null) {
//            log.info("Registering progress handler for request id: {}", messageId);
//            progressHandlers.put(messageId, options.getOnProgress());
//        }
//
//        responseHandlers.put(messageId, (response, error) -> {
//            if (error != null) {
//                result.completeExceptionally(error);
//                return;
//            }
//
//            if (response.getError() != null) {
//                result.completeExceptionally(new IllegalStateException(response.getError().toString()));
//                return;
//            }
//
//            try {
//                result.complete((T) response.getResult());
//            } catch (Throwable e) {
//                result.completeExceptionally(e);
//            }
//        });
//
//        long timeout = options != null ? options.getTimeout() : 60000;
//
//        CompletableFuture.runAsync(() -> {
//            try {
//                log.info("Sending request message with id: {}, method: {}", messageId, message.getMethod());
//                transport.send(message);
//                if (!result.isDone()) {
//                    result.get(timeout, TimeUnit.MILLISECONDS);
//                }
//            } catch (TimeoutException | InterruptedException e) {
//                log.error("Request timed out after {}ms: {}", timeout, request.getMethod());
//                Map<String, Object> data = new HashMap<>();
//                data.put("timeout", timeout);
//                McpErrorException mcpErrorException = new McpErrorException(ErrorCodeDefined.RequestTimeout.getCode(), "Request timed out", data);
//                cancelRequest(messageId, mcpErrorException);
//                result.completeExceptionally(new TimeoutException("Request timed out after " + timeout + "ms"));
//            } catch (Exception e) {
//                result.completeExceptionally(e);
//            }
//        });
//
//        return result;
//    }
//
//    public CompletableFuture<Void> notification(Notification notification) {
//        log.info("Sending notification: ${notification.method}", notification.getMethod());
//        Transport transport = this.transport;
//        if (transport == null) {
//            throw new IllegalStateException("Not connected");
//        }
//        assertNotificationCapability(notification.getMethod());
//
//        return CompletableFuture.runAsync(() -> {
//            JSONRPCNotification message = new JSONRPCNotification(notification.getMethod().getValue(), (JSONObject) JSON.toJSON(notification), null);
//            transport.send(message);
//        });
//    }
//
//    private void cancelRequest(Long messageId, McpErrorException reason) {
//        responseHandlers.remove(messageId);
//        progressHandlers.remove(messageId);
//
//        CancelledNotification notification = new CancelledNotification(messageId, reason.getMessage() != null ? reason.getMessage() : "Unknown");
//        JSONRPCNotification serialized = new JSONRPCNotification(notification.getMethod().getValue(), (JSONObject) JSON.toJSON(notification), null);
//        transport.send(serialized);
//    }
//
//    public void removeRequestHandler(Method method) {
//        requestHandlers.remove(method.getValue());
//    }
//
//    public void removeNotificationHandler(Method method) {
//        this.notificationHandlers.remove(method.getValue());
//    }
//}

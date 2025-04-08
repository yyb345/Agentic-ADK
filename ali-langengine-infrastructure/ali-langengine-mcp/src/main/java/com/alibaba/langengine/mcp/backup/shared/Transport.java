//package com.alibaba.langengine.mcp.shared;
//
//import com.alibaba.langengine.mcp.spec.JSONRPCMessage;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//public interface Transport {
//
//    /**
//     * Starts processing messages on the transport, including any connection steps that might need to be taken.
//     *
//     * This method should only be called after callbacks are installed, or else messages may be lost.
//     *
//     * NOTE: This method should not be called explicitly when using Client, Server, or Protocol classes,
//     * as they will implicitly call start().
//     *
//     * @return CompletableFuture representing the pending completion of the task.
//     */
//    CompletableFuture<Void> start();
//
//    /**
//     * Sends a JSON-RPC message (request or response).
//     *
//     * @param message JSONRPCMessage object to be sent.
//     * @return CompletableFuture representing the pending completion of the task.
//     */
//    CompletableFuture<Void> send(JSONRPCMessage message);
//
//    /**
//     * Closes the connection.
//     *
//     * @return CompletableFuture representing the pending completion of the task.
//     */
//    CompletableFuture<Void> close();
//
//    Runnable getOnClose();
//
//    Consumer<Throwable> getOnError();
//
//    Function<JSONRPCMessage, CompletableFuture<Void>> getOnMessage();
//
//    void setOnClose(Runnable onClose);
//    void setOnError(Consumer<Throwable> onError);
//    void setOnMessage(Function<JSONRPCMessage, CompletableFuture<Void>> onMessage);
//}

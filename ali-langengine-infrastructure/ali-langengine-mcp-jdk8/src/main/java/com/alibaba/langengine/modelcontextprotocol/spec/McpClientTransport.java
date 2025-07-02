/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Marker interface for the client-side MCP transport.
 *
 * JDK 1.8 compatible version.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public interface McpClientTransport extends McpTransport {

    /**
     * Connects to the server and sets up a message handler.
     * 
     * @param handler a function that processes incoming messages and returns responses
     * @return a CompletableFuture that completes when the connection is established
     */
    CompletableFuture<Void> connect(Function<CompletableFuture<JSONRPCMessage>, CompletableFuture<JSONRPCMessage>> handler);
}

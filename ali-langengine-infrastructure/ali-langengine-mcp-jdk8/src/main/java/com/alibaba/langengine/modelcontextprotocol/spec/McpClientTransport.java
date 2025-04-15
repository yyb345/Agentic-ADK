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

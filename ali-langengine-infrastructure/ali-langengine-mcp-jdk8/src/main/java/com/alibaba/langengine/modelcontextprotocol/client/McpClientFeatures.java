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
package com.alibaba.langengine.modelcontextprotocol.client;

import com.alibaba.langengine.modelcontextprotocol.spec.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Features configuration for MCP clients. This class provides a clean separation between
 * the client implementation and the feature configuration.
 *
 * JDK 1.8 compatible version.
 * 
 * @author aihe.ah
 */
public class McpClientFeatures {

    /**
     * Features for synchronous clients.
     */
    public static class Sync {
        private final ClientCapabilities clientCapabilities;
        private final Implementation clientInfo;
        private final Map<String, Root> roots;
        private final List<Consumer<List<Tool>>> toolsChangeConsumers;
        private final List<Consumer<List<Resource>>> resourcesChangeConsumers;
        private final List<Consumer<List<Prompt>>> promptsChangeConsumers;
        private final List<Consumer<LoggingMessageNotification>> loggingConsumers;
        private final Function<CreateMessageRequest, CreateMessageResult> samplingHandler;

        public Sync(ClientCapabilities clientCapabilities, Implementation clientInfo,
                Map<String, Root> roots, List<Consumer<List<Tool>>> toolsChangeConsumers,
                List<Consumer<List<Resource>>> resourcesChangeConsumers,
                List<Consumer<List<Prompt>>> promptsChangeConsumers,
                List<Consumer<LoggingMessageNotification>> loggingConsumers,
                Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {
            this.clientCapabilities = clientCapabilities;
            this.clientInfo = clientInfo;
            this.roots = roots;
            this.toolsChangeConsumers = toolsChangeConsumers;
            this.resourcesChangeConsumers = resourcesChangeConsumers;
            this.promptsChangeConsumers = promptsChangeConsumers;
            this.loggingConsumers = loggingConsumers;
            this.samplingHandler = samplingHandler;
        }

        public ClientCapabilities clientCapabilities() {
            return clientCapabilities;
        }

        public Implementation clientInfo() {
            return clientInfo;
        }

        public Map<String, Root> roots() {
            return roots != null ? roots : new HashMap<>();
        }

        public List<Consumer<List<Tool>>> toolsChangeConsumers() {
            return toolsChangeConsumers;
        }

        public List<Consumer<List<Resource>>> resourcesChangeConsumers() {
            return resourcesChangeConsumers;
        }

        public List<Consumer<List<Prompt>>> promptsChangeConsumers() {
            return promptsChangeConsumers;
        }

        public List<Consumer<LoggingMessageNotification>> loggingConsumers() {
            return loggingConsumers;
        }

        public Function<CreateMessageRequest, CreateMessageResult> samplingHandler() {
            return samplingHandler;
        }
    }

    /**
     * Features for asynchronous clients.
     */
    public static class Async {
        private final ClientCapabilities clientCapabilities;
        private final Implementation clientInfo;
        private final Map<String, Root> roots;
        private final List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers;
        private final List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers;
        private final List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers;
        private final List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers;
        private final Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler;

        public Async(ClientCapabilities clientCapabilities, Implementation clientInfo,
                Map<String, Root> roots,
                List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers,
                List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers,
                List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers,
                List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers,
                Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler) {
            this.clientCapabilities = clientCapabilities;
            this.clientInfo = clientInfo;
            this.roots = roots;
            this.toolsChangeConsumers = toolsChangeConsumers;
            this.resourcesChangeConsumers = resourcesChangeConsumers;
            this.promptsChangeConsumers = promptsChangeConsumers;
            this.loggingConsumers = loggingConsumers;
            this.samplingHandler = samplingHandler;
        }

        public ClientCapabilities clientCapabilities() {
            return clientCapabilities;
        }

        public Implementation clientInfo() {
            return clientInfo;
        }

        public Map<String, Root> roots() {
            return roots != null ? roots : new HashMap<>();
        }

        public List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers() {
            return toolsChangeConsumers;
        }

        public List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers() {
            return resourcesChangeConsumers;
        }

        public List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers() {
            return promptsChangeConsumers;
        }

        public List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers() {
            return loggingConsumers;
        }

        public Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler() {
            return samplingHandler;
        }
        
        /**
         * Convert a Sync features object to an Async features object.
         * 
         * @param sync the synchronous features
         * @return the asynchronous features
         */
        public static Async fromSync(Sync sync) {
            List<Function<List<Tool>, CompletableFuture<Void>>> asyncToolsChangeConsumers = 
                    toAsyncConsumers(sync.toolsChangeConsumers());
            
            List<Function<List<Resource>, CompletableFuture<Void>>> asyncResourcesChangeConsumers = 
                    toAsyncConsumers(sync.resourcesChangeConsumers());
            
            List<Function<List<Prompt>, CompletableFuture<Void>>> asyncPromptsChangeConsumers = 
                    toAsyncConsumers(sync.promptsChangeConsumers());
            
            List<Function<LoggingMessageNotification, CompletableFuture<Void>>> asyncLoggingConsumers = 
                    toAsyncConsumers(sync.loggingConsumers());
            
            Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> asyncSamplingHandler = null;
            if (sync.samplingHandler() != null) {
                asyncSamplingHandler = request -> {
                    CreateMessageResult result = sync.samplingHandler().apply(request);
                    return CompletableFuture.completedFuture(result);
                };
            }
            
            return new Async(
                    sync.clientCapabilities(),
                    sync.clientInfo(),
                    sync.roots(),
                    asyncToolsChangeConsumers,
                    asyncResourcesChangeConsumers,
                    asyncPromptsChangeConsumers,
                    asyncLoggingConsumers,
                    asyncSamplingHandler);
        }
    }
    
    /**
     * Convert a list of synchronous consumers to a list of asynchronous consumers.
     * 
     * @param <T> the type of the consumer input
     * @param consumers the synchronous consumers
     * @return the asynchronous consumers
     */
    public static <T> List<Function<T, CompletableFuture<Void>>> toAsyncConsumers(List<Consumer<T>> consumers) {
        if (consumers == null) {
            return new ArrayList<>();
        }
        
        List<Function<T, CompletableFuture<Void>>> asyncConsumers = new ArrayList<>();
        for (Consumer<T> consumer : consumers) {
            asyncConsumers.add(input -> {
                consumer.accept(input);
                return CompletableFuture.completedFuture(null);
            });
        }
        
        return asyncConsumers;
    }
}

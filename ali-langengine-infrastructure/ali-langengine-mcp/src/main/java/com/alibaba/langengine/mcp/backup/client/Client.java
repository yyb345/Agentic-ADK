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
//package com.alibaba.langengine.mcp.client;
//
//import com.alibaba.langengine.mcp.shared.Protocol;
//import com.alibaba.langengine.mcp.shared.Transport;
//import com.alibaba.langengine.mcp.spec.*;
//import com.alibaba.langengine.mcp.spec.schema.*;
//import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptRequest;
//import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptResult;
//import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
//import com.alibaba.langengine.mcp.spec.schema.tools.CallToolResult;
//
//import java.util.concurrent.CompletableFuture;
//
//import static com.alibaba.langengine.mcp.spec.Schema.LATEST_PROTOCOL_VERSION;
//import static com.alibaba.langengine.mcp.spec.Schema.SUPPORTED_PROTOCOL_VERSIONS;
//
///**
// * An MCP client on top of a pluggable transport.
// *
// * The client automatically performs the initialization handshake with the server when [connect] is called.
// * After initialization, [getServerCapabilities] and [getServerVersion] provide details about the connected server.
// *
// * You can extend this class with custom request/notification/result types if needed.
// */
//public class Client extends Protocol {
//
//    /**
//     * Information about the client implementation (name, version).
//     */
//    private Implementation clientInfo;
//
//    /**
//     * Configuration options for the client.
//     */
//    private ClientOptions options;
//
//    public Client(Implementation clientInfo, ClientOptions options) {
//        super(options);
//        this.clientInfo = clientInfo;
//        this.options = options;
//        this.capabilities = options.getCapabilities();
//    }
//
//    /**
//     * The server's reported capabilities after initialization.
//     */
//    private ServerCapabilities serverCapabilities;
//
//    /**
//     * The server's version information after initialization.
//     */
//    private Implementation serverVersion;
//
//    private ClientCapabilities capabilities;
//
//    public CompletableFuture<Void> connect(Transport transport) {
//        return CompletableFuture.runAsync(() -> {
//            super.connect(transport);
//
//            InitializeRequest message = new InitializeRequest(
//                    LATEST_PROTOCOL_VERSION,
//                    getCapabilities(),
//                    getClientInfo()
//            );
//
//            try {
//                CompletableFuture<InitializeResult> result = request(message, null);
//                InitializeResult initializeResult = result.get();
//
//                if (!SUPPORTED_PROTOCOL_VERSIONS.contains(initializeResult.getProtocolVersion())) {
//                    throw new IllegalStateException(
//                            "Server's protocol version is not supported: " + initializeResult.getProtocolVersion()
//                    );
//                }
//
//                serverCapabilities = initializeResult.getCapabilities();
//                serverVersion = initializeResult.getServerInfo();
//
//                notification(new InitializedNotification());
//            } catch (Exception e) {
//                close();
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    @Override
//    public void assertCapabilityForMethod(Method method) {
//
//    }
//
//    @Override
//    public void assertNotificationCapability(Method method) {
//
//    }
//
//    @Override
//    public void assertRequestHandlerCapability(Method method) {
//
//    }
//
//    public CompletableFuture<EmptyRequestResult> ping() {
//        PingRequest pingRequest = new PingRequest();
//        return request(pingRequest, null);
//    }
//
//    public CompletableFuture<CompleteResult> complete(CompleteRequest params, RequestOptions options) {
//        return request(params, options);
//    }
//
//    public CompletableFuture<EmptyRequestResult> setLoggingLevel(LoggingLevel level, RequestOptions options) {
//        return request(new LoggingMessageNotification.SetLevelRequest(level), options);
//    }
//
//    public CompletableFuture<GetPromptResult> getPrompt(GetPromptRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<ListPromptsResult> listPrompts(ListPromptsRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<ListResourcesResult> listResources(ListResourcesRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<ListResourceTemplatesResult> listResourceTemplates(ListResourceTemplatesRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<ReadResourceResult> readResource(ReadResourceRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<EmptyRequestResult> subscribeResource(SubscribeRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<EmptyRequestResult> unsubscribeResource(UnsubscribeRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<CallToolResult> callTool(CallToolRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<ListToolsResult> listTools(ListToolsRequest request, RequestOptions options) {
//        return request(request, options);
//    }
//
//    public CompletableFuture<Void> sendRootsListChanged() {
//        return notification(new RootsListChangedNotification());
//    }
//
//    public Implementation getClientInfo() {
//        return clientInfo;
//    }
//
//    public void setClientInfo(Implementation clientInfo) {
//        this.clientInfo = clientInfo;
//    }
//
//    @Override
//    public ClientOptions getOptions() {
//        return options;
//    }
//
//    public void setOptions(ClientOptions options) {
//        this.options = options;
//    }
//
//    public ServerCapabilities getServerCapabilities() {
//        return serverCapabilities;
//    }
//
//    public void setServerCapabilities(ServerCapabilities serverCapabilities) {
//        this.serverCapabilities = serverCapabilities;
//    }
//
//    public Implementation getServerVersion() {
//        return serverVersion;
//    }
//
//    public void setServerVersion(Implementation serverVersion) {
//        this.serverVersion = serverVersion;
//    }
//
//    public ClientCapabilities getCapabilities() {
//        return capabilities;
//    }
//
//    public void setCapabilities(ClientCapabilities capabilities) {
//        this.capabilities = capabilities;
//    }
//}

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
//package com.alibaba.langengine.mcp.server;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.langengine.mcp.shared.RequestHandlerExtra;
//import com.alibaba.langengine.mcp.spec.schema.*;
//import com.alibaba.langengine.mcp.shared.Protocol;
//import com.alibaba.langengine.mcp.spec.*;
//import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptRequest;
//import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptResult;
//import com.alibaba.langengine.mcp.spec.schema.prompts.Prompt;
//import com.alibaba.langengine.mcp.spec.schema.prompts.PromptArgument;
//import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
//import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
//import com.alibaba.langengine.mcp.spec.schema.tools.CallToolResult;
//import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import static com.alibaba.langengine.mcp.spec.Schema.LATEST_PROTOCOL_VERSION;
//import static com.alibaba.langengine.mcp.spec.Schema.SUPPORTED_PROTOCOL_VERSIONS;
//
///**
// * An MCP server on top of a pluggable transport.
// *
// * This server automatically responds to the initialization flow as initiated by the client.
// * You can register tools, prompts, and resources using [addTool], [addPrompt], and [addResource].
// * The server will then automatically handle listing and retrieval requests from the client.
// */
//@Slf4j
//public class Server extends Protocol {
//
//    /**
//     * Information about this server implementation (name, version).
//     */
//    private Implementation serverInfo;
//
//    /**
//     * Configuration options for the server.
//     */
//    private ServerOptions options;
//
//    /**
//     * A callback invoked when the server connection closes.
//     */
//    private Runnable onCloseCallback;
//
//    public Server(Implementation serverInfo, ServerOptions options) {
//        super(options);
//        this.serverInfo = serverInfo;
//        this.options = options;
//        this.capabilities = options.getCapabilities();
//        init();
//    }
//
//    private void init() {
//        // Core protocol handlers
//        BiFunction<InitializeRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> initializeHandler = (request, extra) ->
//            handleInitialize(request);
//        setRequestHandler(InitializeRequest.class, MethodDefined.Initialize.getValue(), initializeHandler);
//
//        Function<ProgressNotification, CompletableFuture<Void>> notificationHandler = notification -> {
//            if(onInitialized != null) {
//                onInitialized.run();
//            }
//            return CompletableFuture.completedFuture(null);
//        };
//        setNotificationHandler(MethodDefined.NotificationsInitialized.getValue(), notificationHandler);
//
//        // Internal handlers for tools
//        if (capabilities.getTools() != null) {
//            BiFunction<ListToolsRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> toolsListHandler = (request, extra) ->
//                    handleListTools();
//            setRequestHandler(ListToolsRequest.class, MethodDefined.ToolsList.getValue(), toolsListHandler);
//
//            BiFunction<CallToolRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> toolsCallHandler = (request, extra) ->
//                    handleCallTool(request);
//            setRequestHandler(CallToolRequest.class, MethodDefined.ToolsCall.getValue(), toolsCallHandler);
//        }
//
//        // Internal handlers for prompts
//        if (capabilities.getPrompts() != null) {
//            BiFunction<ListPromptsRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> promptsListHandler = (request, extra) ->
//                    handleListPrompts();
//            setRequestHandler(ListPromptsRequest.class, MethodDefined.PromptsList.getValue(), promptsListHandler);
//
//            BiFunction<GetPromptRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> promptsGetHandler = (request, extra) ->
//                    handleGetPrompt(request);
//            setRequestHandler(GetPromptRequest.class, MethodDefined.PromptsGet.getValue(), promptsGetHandler);
//        }
//
//        // Internal handlers for resources
//        if (capabilities.getResources() != null) {
//            BiFunction<ListResourcesRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> resourcesListHandler = (request, extra) ->
//                    handleListResources();
//            setRequestHandler(ListResourcesRequest.class, MethodDefined.ResourcesList.getValue(), resourcesListHandler);
//
//            BiFunction<ReadResourceRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> resourcesReadHandler = (request, extra) ->
//                    handleReadResource(request);
//            setRequestHandler(ReadResourceRequest.class, MethodDefined.ResourcesRead.getValue(), resourcesReadHandler);
//
//            BiFunction<ListResourceTemplatesRequest, RequestHandlerExtra, CompletableFuture<RequestResult>> resourcesTemplatesListHandler = (request, extra) ->
//                    handleListResourceTemplates();
//            setRequestHandler(ListResourceTemplatesRequest.class, MethodDefined.ResourcesTemplatesList.getValue(), resourcesTemplatesListHandler);
//        }
//    }
//
//    /**
//     * The client's reported capabilities after initialization.
//     */
//    private ClientCapabilities clientCapabilities;
//
//    /**
//     * The client's version information after initialization.
//     */
//    private Implementation clientVersion;
//
//    private ServerCapabilities capabilities;
//
//    private Map<String, RegisteredTool> tools = new HashMap<>();
//    private Map<String, RegisteredPrompt> prompts = new HashMap<>();
//    private Map<String, RegisteredResource> resources = new HashMap<>();
//
//    /**
//     * A callback invoked when the server has completed the initialization sequence.
//     * After initialization, the server is ready to handle requests.
//     */
//    private Runnable onInitialized;
//
//    @Override
//    public void onclose() {
//        log.info("Server connection closing");
//        if (onCloseCallback != null) {
//            onCloseCallback.run();
//        }
//    }
//
//    public void addTool(String name,
//            String description,
//            Tool.Input inputSchema,
//            Function<CallToolRequest, CompletableFuture<CallToolResult>> handler) {
//        if (capabilities.getTools() == null) {
//            log.error("Failed to add tool '" + name + "': Server does not support tools capability");
//            throw new IllegalStateException("Server does not support tools capability. Enable it in ServerOptions.");
//        }
//        log.info("Registering tool: " + name);
//        tools.put(name, new RegisteredTool(new Tool(name, description, inputSchema), handler));
//    }
//
//    public void addTools(List<RegisteredTool> toolsToAdd) {
//        if (capabilities.getTools() == null) {
//            log.error("Failed to add tools: Server does not support tools capability");
//            throw new IllegalStateException("Server does not support tools capability.");
//        }
//        log.info("Registering " + toolsToAdd.size() + " tools");
//        for (RegisteredTool rt : toolsToAdd) {
//            log.info("Registering tool: " + rt.getTool().getName());
//            tools.put(rt.getTool().getName(), rt);
//        }
//    }
//
//    public void addPrompt(Prompt prompt, Function<GetPromptRequest, CompletableFuture<GetPromptResult>> promptProvider) {
//        if (capabilities.getPrompts() == null) {
//            log.error("Failed to add prompt '" + prompt.getName() + "': Server does not support prompts capability");
//            throw new IllegalStateException("Server does not support prompts capability.");
//        }
//        log.info("Registering prompt: " + prompt.getName());
//        prompts.put(prompt.getName(), new RegisteredPrompt(prompt, promptProvider));
//    }
//
//    public void addPrompt(String name, String description, List<PromptArgument> arguments,
//                          Function<GetPromptRequest, CompletableFuture<GetPromptResult>> promptProvider) {
//        Prompt prompt = new Prompt(name, description, arguments);
//        addPrompt(prompt, promptProvider);
//    }
//
//    public void addPrompts(List<RegisteredPrompt> promptsToAdd) {
//        if (capabilities.getPrompts() == null) {
//            log.error("Failed to add prompts: Server does not support prompts capability");
//            throw new IllegalStateException("Server does not support prompts capability.");
//        }
//        log.info("Registering " + promptsToAdd.size() + " prompts");
//        for (RegisteredPrompt rp : promptsToAdd) {
//            log.info("Registering prompt: " + rp.getPrompt().getName());
//            prompts.put(rp.getPrompt().getName(), rp);
//        }
//    }
//
//    public void addResource(String uri, String name, String description, String mimeType,
//                            Function<ReadResourceRequest, CompletableFuture<ReadResourceResult>> readHandler) {
//        if (capabilities.getResources() == null) {
//            log.error("Failed to add resource '" + name + "': Server does not support resources capability");
//            throw new IllegalStateException("Server does not support resources capability.");
//        }
//        log.info("Registering resource: " + name + " (" + uri + ")");
//        resources.put(uri, new RegisteredResource(new Resource(uri, name, description, mimeType), readHandler));
//    }
//
//    public void addResource(String uri, String name, String description,
//                            Function<ReadResourceRequest, CompletableFuture<ReadResourceResult>> readHandler) {
//        addResource(uri, name, description, "text/html", readHandler);
//    }
//
//    public void addResources(List<RegisteredResource> resourcesToAdd) {
//        if (capabilities.getResources() == null) {
//            log.error("Failed to add resources: Server does not support resources capability");
//            throw new IllegalStateException("Server does not support resources capability.");
//        }
//        log.info("Registering " + resourcesToAdd.size() + " resources");
//        for (RegisteredResource r : resourcesToAdd) {
//            log.info("Registering resource: " + r.getResource().getName() + " (" + r.getResource().getUri() + ")");
//            resources.put(r.getResource().getUri(), r);
//        }
//    }
//
//    public CompletableFuture<EmptyRequestResult> ping() {
//        PingRequest pingRequest = new PingRequest();
//        return request(pingRequest, null);
//    }
//
//    public CompletableFuture<CreateMessageResult> createMessage(CreateMessageRequest params, RequestOptions options) {
//        log.info("Creating message with params: " + params);
//        return request(params, options);
//    }
//
//    public CompletableFuture<ListRootsResult> listRoots(JSONObject params, RequestOptions options) {
//        if (params == null) {
//            params = new JSONObject();
//        }
//        log.info("Listing roots with params: " + params);
//        ListRootsRequest request = new ListRootsRequest(params);
//        return request(request, options);
//    }
//
//    public CompletableFuture<Void> sendLoggingMessage(LoggingMessageNotification params) {
//        log.info("Sending logging message: " + params.getData());
//        return notification(params);
//    }
//
//    public CompletableFuture<Void> sendResourceUpdated(ResourceUpdatedNotification params) {
//        log.info("Sending resource updated notification for: " + params.getUri());
//        return notification(params);
//    }
//
//    /**
//     * Sends a notification to the client indicating that the list of resources has changed.
//     */
//    public CompletableFuture<Void> sendResourceListChanged() {
//        log.info("Sending resource list changed notification");
//        return notification(new ResourceListChangedNotification());
//    }
//
//    /**
//     * Sends a notification to the client indicating that the list of tools has changed.
//     */
//    public CompletableFuture<Void> sendToolListChanged() {
//        log.info("Sending tool list changed notification");
//        return notification(new ToolListChangedNotification());
//    }
//
//    /**
//     * Sends a notification to the client indicating that the list of prompts has changed.
//     */
//    public CompletableFuture<Void> sendPromptListChanged() {
//        log.info("Sending prompt list changed notification");
//        return notification(new PromptListChangedNotification());
//    }
//
//    private CompletableFuture<RequestResult> handleInitialize(InitializeRequest request) {
//        log.info("Handling initialize request from client " + JSONObject.toJSONString(request));
//        clientCapabilities = request.getCapabilities();
//        clientVersion = request.getClientInfo();
//
//        String requestedVersion = request.getProtocolVersion();
//        String protocolVersion;
//        if (SUPPORTED_PROTOCOL_VERSIONS.contains(requestedVersion)) {
//            protocolVersion = requestedVersion;
//        } else {
//            log.warn("Client requested unsupported protocol version " + requestedVersion +
//                    ", falling back to " + LATEST_PROTOCOL_VERSION);
//            protocolVersion = LATEST_PROTOCOL_VERSION;
//        }
//
//        InitializeResult result = new InitializeResult(
//                protocolVersion,
//                capabilities,
//                serverInfo
//        );
//
//        return CompletableFuture.completedFuture(result);
//    }
//
//    private CompletableFuture<RequestResult> handleListTools() {
//        return CompletableFuture.supplyAsync(() -> {
//            List<Tool> toolList = tools.values().stream()
//                    .map(e -> e.getTool())
//                    .collect(Collectors.toList());
//            return new ListToolsResult(toolList, null);
//        });
//    }
//
//    private CompletableFuture<RequestResult> handleCallTool(CallToolRequest request) {
//        log.info("Handling tool call request for tool: " + request.getName());
//
//        RegisteredTool registeredTool = tools.get(request.getName());
//        if (registeredTool == null) {
//            log.error("Tool not found: " + request.getName());
//            CompletableFuture<RequestResult> future = new CompletableFuture<>();
//            future.completeExceptionally(new IllegalArgumentException("Tool not found: " + request.getName()));
//            return future;
//        }
//
//        log.info("Executing tool " + request.getName() + " with input: " + request.getArguments());
//        CompletableFuture<RequestResult> completableFuture = registeredTool.getHandler().apply(request).thenApply(callToolResult -> {
//            return callToolResult;
//        });
//        return completableFuture;
//    }
//
//    private CompletableFuture<RequestResult> handleListPrompts() {
//        log.info("Handling list prompts request");
//
//        return CompletableFuture.supplyAsync(() -> {
//            List<Prompt> promptList = prompts.values().stream()
//                    .map(e -> e.getPrompt())
//                    .collect(Collectors.toList());
//            return new ListPromptsResult(promptList, null);
//        });
//    }
//
//    private CompletableFuture<RequestResult> handleGetPrompt(GetPromptRequest request) {
//        log.info("Handling get prompt request for: " + request.getName());
//
//        RegisteredPrompt registeredPrompt = prompts.get(request.getName());
//        if (registeredPrompt == null) {
//            log.error("Prompt not found: " + request.getName());
//            CompletableFuture<RequestResult> future = new CompletableFuture<>();
//            future.completeExceptionally(new IllegalArgumentException("Prompt not found: " + request.getName()));
//            return future;
//        }
//
//        return registeredPrompt.getMessageProvider().apply(request).thenApply(getPromptResult -> {
//            return getPromptResult;
//        });
//    }
//
//    private CompletableFuture<RequestResult> handleListResources() {
//        log.info("Handling list resources request");
//
//        return CompletableFuture.supplyAsync(() -> {
//            List<Resource> resourceList = resources.values().stream()
//                    .map(e -> e.getResource())
//                    .collect(Collectors.toList());
//            return new ListResourcesResult(resourceList, null);
//        });
//    }
//
//    private CompletableFuture<RequestResult> handleReadResource(ReadResourceRequest request) {
//        log.info("Handling read resource request for: " + request.getUri());
//
//        RegisteredResource registeredResource = resources.get(request.getUri());
//        if (registeredResource == null) {
//            log.error("Resource not found: " + request.getUri());
//            CompletableFuture<RequestResult> future = new CompletableFuture<>();
//            future.completeExceptionally(new IllegalArgumentException("Resource not found: " + request.getUri()));
//            return future;
//        }
//
//        return registeredResource.getReadHandler().apply(request).thenApply(readResourceResult -> {
//            return readResourceResult;
//        });
//    }
//
//    private CompletableFuture<RequestResult> handleListResourceTemplates() {
//        return CompletableFuture.supplyAsync(() -> new ListResourceTemplatesResult(Collections.emptyList(), null));
//    }
//
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
//    public Implementation getServerInfo() {
//        return serverInfo;
//    }
//
//    public void setServerInfo(Implementation serverInfo) {
//        this.serverInfo = serverInfo;
//    }
//
//    @Override
//    public ServerOptions getOptions() {
//        return options;
//    }
//
//    public void setOptions(ServerOptions options) {
//        this.options = options;
//    }
//
//
//    public Runnable getOnCloseCallback() {
//        return onCloseCallback;
//    }
//
//    public void setOnCloseCallback(Runnable onCloseCallback) {
//        this.onCloseCallback = onCloseCallback;
//    }
//
//    public ServerCapabilities getCapabilities() {
//        return capabilities;
//    }
//
//    public ClientCapabilities getClientCapabilities() {
//        return clientCapabilities;
//    }
//
//    public void setClientCapabilities(ClientCapabilities clientCapabilities) {
//        this.clientCapabilities = clientCapabilities;
//    }
//
//    public Implementation getClientVersion() {
//        return clientVersion;
//    }
//
//    public void setClientVersion(Implementation clientVersion) {
//        this.clientVersion = clientVersion;
//    }
//
//    public void setCapabilities(ServerCapabilities capabilities) {
//        this.capabilities = capabilities;
//    }
//}

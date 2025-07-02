/*
 * Copyright 2024-2024 the original author or authors.
 */
package com.alibaba.langengine.modelcontextprotocol.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.alibaba.langengine.modelcontextprotocol.spec.*;
import com.alibaba.langengine.modelcontextprotocol.spec.McpClientSession.NotificationHandler;
import com.alibaba.langengine.modelcontextprotocol.spec.McpClientSession.RequestHandler;
import com.alibaba.langengine.modelcontextprotocol.util.Assert;
import com.alibaba.langengine.modelcontextprotocol.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.langengine.modelcontextprotocol.spec.McpSchema.*;

/**
 * The Model Context Protocol (MCP) client implementation that provides asynchronous
 * communication with MCP servers using CompletableFuture.
 *
 * <p>
 * This client implements the MCP specification, enabling AI models to interact with
 * external tools and resources through a standardized interface. Key features include:
 * <ul>
 * <li>Asynchronous communication using CompletableFuture
 * <li>Tool discovery and invocation for server-provided functionality
 * <li>Resource access and management with URI-based addressing
 * <li>Prompt template handling for standardized AI interactions
 * <li>Real-time notifications for tools, resources, and prompts changes
 * <li>Structured logging with configurable severity levels
 * <li>Message sampling for AI model interactions
 * </ul>
 *
 * <p>
 * The client follows a lifecycle:
 * <ol>
 * <li>Initialization - Establishes connection and negotiates capabilities
 * <li>Normal Operation - Handles requests and notifications
 * <li>Graceful Shutdown - Ensures clean connection termination
 * </ol>
 *
 * <p>
 * This implementation uses CompletableFuture for non-blocking operations, making it
 * suitable for high-throughput scenarios and asynchronous applications.
 * <p>
 * JDK 1.8 compatible version.
 *
 * @author Dariusz Jędrzejczyk
 * @author Christian Tzolov
 * @see McpClient
 * @see McpSchema
 * @see McpClientSession
 */
public class McpAsyncClient {

    private static final Logger logger = LoggerFactory.getLogger(McpAsyncClient.class);

    private static final TypeReference<Void> VOID_TYPE_REFERENCE = new TypeReference<Void>() {
    };

    private final CompletableFuture<InitializeResult> initializedFuture = new CompletableFuture<>();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * The max timeout to await for the client-server connection to be initialized.
     */
    private final Duration initializationTimeout;

    /**
     * The MCP session implementation that manages bidirectional JSON-RPC communication
     * between clients and servers.
     */
    private final McpClientSession mcpSession;

    /**
     * Client capabilities.
     */
    private final ClientCapabilities clientCapabilities;

    /**
     * Client implementation information.
     */
    private final Implementation clientInfo;

    /**
     * Server capabilities.
     */
    private ServerCapabilities serverCapabilities;

    /**
     * Server implementation information.
     */
    private Implementation serverInfo;

    /**
     * Roots define the boundaries of where servers can operate within the filesystem,
     * allowing them to understand which directories and files they have access to.
     * Servers can request the list of roots from supporting clients and receive
     * notifications when that list changes.
     */
    private final ConcurrentHashMap<String, Root> roots;

    /**
     * MCP provides a standardized way for servers to request LLM sampling ("completions"
     * or "generations") from language models via clients. This flow allows clients to
     * maintain control over model access, selection, and permissions while enabling
     * servers to leverage AI capabilities—with no server API keys necessary. Servers can
     * request text or image-based interactions and optionally include context from MCP
     * servers in their prompts.
     */
    private Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler;

    /**
     * Client transport implementation.
     */
    private final McpClientTransport transport;

    /**
     * Supported protocol versions.
     */
    private List<String> protocolVersions = new ArrayList<>();

    // Type references for various request/response types
    private static final TypeReference<CallToolResult> CALL_TOOL_RESULT_TYPE_REF =
            new TypeReference<CallToolResult>() {
            };
    private static final TypeReference<ListToolsResult> LIST_TOOLS_RESULT_TYPE_REF =
            new TypeReference<ListToolsResult>() {
            };
    private static final TypeReference<ListResourcesResult> LIST_RESOURCES_RESULT_TYPE_REF =
            new TypeReference<ListResourcesResult>() {
            };
    private static final TypeReference<ReadResourceResult> READ_RESOURCE_RESULT_TYPE_REF =
            new TypeReference<ReadResourceResult>() {
            };
    private static final TypeReference<ListResourceTemplatesResult> LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF =
            new TypeReference<ListResourceTemplatesResult>() {
            };
    private static final TypeReference<ListPromptsResult> LIST_PROMPTS_RESULT_TYPE_REF =
            new TypeReference<ListPromptsResult>() {
            };
    private static final TypeReference<GetPromptResult> GET_PROMPT_RESULT_TYPE_REF =
            new TypeReference<GetPromptResult>() {
            };

    /**
     * Create a new McpAsyncClient with the given transport and session request-response
     * timeout.
     *
     * @param transport                the transport to use.
     * @param requestTimeout           the session request-response timeout.
     * @param initializationTimeout    the max timeout to await for the client-server
     * @param clientCapabilities       the client capabilities.
     * @param clientInfo               the client implementation information.
     * @param roots                    the client roots.
     * @param toolsChangeConsumers     the tools change consumers.
     * @param resourcesChangeConsumers the resources change consumers.
     * @param promptsChangeConsumers   the prompts change consumers.
     * @param loggingConsumers         the logging consumers.
     * @param samplingHandler          the sampling handler.
     */
    McpAsyncClient(McpClientTransport transport, Duration requestTimeout, Duration initializationTimeout,
                   ClientCapabilities clientCapabilities, Implementation clientInfo,
                   List<Root> roots,
                   List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers,
                   List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers,
                   List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers,
                   List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers,
                   Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler) {

        Assert.notNull(transport, "Transport must not be null");
        Assert.notNull(requestTimeout, "Request timeout must not be null");
        Assert.notNull(initializationTimeout, "Initialization timeout must not be null");

        this.clientInfo = clientInfo;
        this.clientCapabilities = clientCapabilities;
        this.transport = transport;
        this.roots = new ConcurrentHashMap<>();
        if (roots != null) {
            for (Root root : roots) {
                this.roots.put(root.uri(), root);
            }
        }
        this.initializationTimeout = initializationTimeout;
        this.protocolVersions.add(LATEST_PROTOCOL_VERSION);

        // Request Handlers
        Map<String, RequestHandler<?>> requestHandlers = new HashMap<>();

        // Roots List Request Handler
        if (this.clientCapabilities.roots() != null) {
            requestHandlers.put(METHOD_ROOTS_LIST, rootsListRequestHandler());
        }

        // Sampling Handler
        if (this.clientCapabilities.sampling() != null) {
            if (samplingHandler == null) {
                throw new McpError("Sampling handler must not be null when client capabilities include sampling");
            }
            this.samplingHandler = samplingHandler;
            requestHandlers.put(METHOD_SAMPLING_CREATE_MESSAGE, samplingCreateMessageHandler());
        }

        // Notification Handlers
        Map<String, NotificationHandler> notificationHandlers = new HashMap<>();

        // Tools Change Notification
        List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumersFinal = new ArrayList<>();
        toolsChangeConsumersFinal.add(notification -> {
            logger.debug("Tools changed: {}", notification);
            return CompletableFuture.completedFuture(null);
        });

        if (!Utils.isEmpty(toolsChangeConsumers)) {
            toolsChangeConsumersFinal.addAll(toolsChangeConsumers);
        }
        notificationHandlers.put(METHOD_NOTIFICATION_TOOLS_LIST_CHANGED,
                asyncToolsChangeNotificationHandler(toolsChangeConsumersFinal));

        // Resources Change Notification
        List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumersFinal = new ArrayList<>();
        resourcesChangeConsumersFinal.add(notification -> {
            logger.debug("Resources changed: {}", notification);
            return CompletableFuture.completedFuture(null);
        });

        if (!Utils.isEmpty(resourcesChangeConsumers)) {
            resourcesChangeConsumersFinal.addAll(resourcesChangeConsumers);
        }

        notificationHandlers.put(METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED,
                asyncResourcesChangeNotificationHandler(resourcesChangeConsumersFinal));

        // Prompts Change Notification
        List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumersFinal = new ArrayList<>();
        promptsChangeConsumersFinal.add(notification -> {
            logger.debug("Prompts changed: {}", notification);
            return CompletableFuture.completedFuture(null);
        });

        if (!Utils.isEmpty(promptsChangeConsumers)) {
            promptsChangeConsumersFinal.addAll(promptsChangeConsumers);
        }

        notificationHandlers.put(METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED,
                asyncPromptsChangeNotificationHandler(promptsChangeConsumersFinal));

        // Utility Logging Notification
        List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumersFinal = new ArrayList<>();
        loggingConsumersFinal.add(notification -> {
            logger.debug("Logging: {}", notification);
            return CompletableFuture.completedFuture(null);
        });

        if (!Utils.isEmpty(loggingConsumers)) {
            loggingConsumersFinal.addAll(loggingConsumers);
        }

        notificationHandlers.put(METHOD_NOTIFICATION_MESSAGE,
                asyncLoggingNotificationHandler(loggingConsumersFinal));

        this.mcpSession = new McpClientSession(requestTimeout, transport, requestHandlers, notificationHandlers);
    }

    /**
     * Get the server capabilities that define the supported features and functionality.
     *
     * @return The server capabilities
     */
    public ServerCapabilities getServerCapabilities() {
        return serverCapabilities;
    }

    /**
     * Get the server implementation information.
     *
     * @return The server implementation details
     */
    public Implementation getServerInfo() {
        return serverInfo;
    }

    /**
     * Check if the client-server connection is initialized.
     *
     * @return true if the client-server connection is initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Get the client capabilities that define the supported features and functionality.
     *
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return clientCapabilities;
    }

    /**
     * Get the client implementation information.
     *
     * @return The client implementation details
     */
    public Implementation getClientInfo() {
        return clientInfo;
    }

    /**
     * Closes the client connection immediately.
     */
    public void close() {
        mcpSession.close();
    }

    /**
     * Gracefully closes the client connection.
     *
     * @return A CompletableFuture that completes when the connection is closed
     */
    public CompletableFuture<Void> closeGracefully() {
        return mcpSession.closeGracefully();
    }

    /**
     * The initialization phase MUST be the first interaction between client and server.
     * During this phase, the client and server:
     * <ul>
     * <li>Establish protocol version compatibility</li>
     * <li>Exchange and negotiate capabilities</li>
     * <li>Share implementation details</li>
     * </ul>
     * <br/>
     * The client MUST initiate this phase by sending an initialize request containing:
     * The protocol version the client supports, client's capabilities and clients
     * implementation information.
     * <p/>
     * The server MUST respond with its own capabilities and information.
     * <p/>
     * After successful initialization, the client MUST send an initialized notification
     * to indicate it is ready to begin normal operations.
     *
     * @return the initialize result.
     * @see <a href=
     * "https://github.com/modelcontextprotocol/specification/blob/main/docs/specification/basic/lifecycle.md#initialization">MCP
     * Initialization Spec</a>
     */
    public CompletableFuture<InitializeResult> initialize() {
        if (initialized.get()) {
            return CompletableFuture.completedFuture(
                    new InitializeResult(
                            protocolVersions.get(0),
                            serverCapabilities,
                            serverInfo,
                            null
                    )
            );
        }

        InitializeRequest initializeRequest = new com.alibaba.langengine.modelcontextprotocol.spec.InitializeRequest(
                protocolVersions.get(0),
                clientCapabilities,
                clientInfo
        );

        return mcpSession.sendRequest(
                METHOD_INITIALIZE,
                initializeRequest,
                new TypeReference<InitializeResult>() {
                }
        ).thenCompose(result -> {
            serverCapabilities = result.capabilities();
            serverInfo = result.serverInfo();

            return mcpSession.sendNotification(METHOD_NOTIFICATION_INITIALIZED, null)
                    .thenApply(v -> {
                        initialized.set(true);
                        initializedFuture.complete(result);
                        return result;
                    });
        }).exceptionally(error -> {
            initializedFuture.completeExceptionally(error);
            throw new RuntimeException("Failed to initialize client", error);
        });
    }

    /**
     * Utility method to handle the common pattern of checking initialization before
     * executing an operation.
     *
     * @param <T>        The type of the result CompletableFuture
     * @param actionName The action to perform if the client is initialized
     * @param operation  The operation to execute if the client is initialized
     * @return A CompletableFuture that completes with the result of the operation
     */
    private <T> CompletableFuture<T> withInitializationCheck(String actionName,
                                                             Function<InitializeResult, CompletableFuture<T>> operation) {

        if (initialized.get()) {
            try {
                return operation.apply(initializedFuture.getNow(null));
            } catch (Exception e) {
                CompletableFuture<T> result = new CompletableFuture<T>();
                result.completeExceptionally(e);
                return result;
            }
        }

        final CompletableFuture<T> result = new CompletableFuture<T>();

        try {
            // In JDK 1.8, we can't copy the future, so we'll use the original and add our own timeout
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            final ScheduledFuture<?> timeoutTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    if (!result.isDone()) {
                        result.completeExceptionally(
                                new McpError("Timed out waiting for initialization to complete before " + actionName)
                        );
                    }
                }
            }, initializationTimeout.toMillis(), TimeUnit.MILLISECONDS);
            
            initializedFuture.thenCompose(new Function<InitializeResult, CompletableFuture<T>>() {
                @Override
                public CompletableFuture<T> apply(InitializeResult initResult) {
                    return operation.apply(initResult);
                }
            }).thenAccept(new Consumer<T>() {
                @Override
                public void accept(T t) {
                    timeoutTask.cancel(false);
                    scheduler.shutdown();
                    result.complete(t);
                }
            }).exceptionally(new Function<Throwable, Void>() {
                @Override
                public Void apply(Throwable error) {
                    timeoutTask.cancel(false);
                    scheduler.shutdown();
                    result.completeExceptionally(error);
                    return null;
                }
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    /**
     * Sends a ping request to the server.
     *
     * @return A CompletableFuture that completes with the server's ping response
     */
    public CompletableFuture<Object> ping() {
        return withInitializationCheck("ping", initResult ->
                mcpSession.sendRequest(METHOD_PING, null, new TypeReference<Object>() {
                })
        );
    }

    /**
     * Adds a new root to the client's root list.
     *
     * @param root The root to add.
     * @return A CompletableFuture that completes when the root is added and notifications are sent.
     */
    public CompletableFuture<Void> addRoot(Root root) {
        Assert.notNull(root, "Root must not be null");

        this.roots.put(root.uri(), root);

        if (initialized.get()) {
            return rootsListChangedNotification();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Removes a root from the client's root list.
     *
     * @param rootUri The URI of the root to remove.
     * @return A CompletableFuture that completes when the root is removed and notifications are sent.
     */
    public CompletableFuture<Void> removeRoot(String rootUri) {
        Assert.hasText(rootUri, "Root URI must not be empty");

        this.roots.remove(rootUri);

        if (initialized.get()) {
            return rootsListChangedNotification();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Manually sends a roots/list_changed notification. The addRoot and removeRoot
     * methods automatically send the roots/list_changed notification if the client is in
     * an initialized state.
     *
     * @return A CompletableFuture that completes when the notification is sent.
     */
    public CompletableFuture<Void> rootsListChangedNotification() {
        Map<String, Object> params = new HashMap<>();
        params.put("listChanged", true);

        return mcpSession.sendNotification(METHOD_NOTIFICATION_ROOTS_LIST_CHANGED, params);
    }

    /**
     * Handler for roots/list requests.
     *
     * @return The request handler
     */
    private RequestHandler<ListRootsResult> rootsListRequestHandler() {
        return params -> {
            List<Root> rootsList = new ArrayList<>(roots.values());
            ListRootsResult result = new ListRootsResult(rootsList);
            return CompletableFuture.completedFuture(result);
        };
    }

    /**
     * Handler for sampling/createMessage requests.
     *
     * @return The request handler
     */
    private RequestHandler<CreateMessageResult> samplingCreateMessageHandler() {
        return params -> {
            CreateMessageRequest createMessageRequest = transport.unmarshalFrom(
                    params,
                    new TypeReference<CreateMessageRequest>() {
                    }
            );

            return samplingHandler.apply(createMessageRequest);
        };
    }

    /**
     * Calls a tool provided by the server. Tools enable servers to expose executable
     * functionality that can interact with external systems, perform computations, and
     * take actions in the real world.
     *
     * @param callToolRequest The request containing the tool name and input parameters.
     * @return A CompletableFuture that emits the result of the tool call, including the output and any
     * errors.
     * @see CallToolRequest
     * @see CallToolResult
     * @see #listTools()
     */
    public CompletableFuture<CallToolResult> callTool(CallToolRequest callToolRequest) {
        Assert.notNull(callToolRequest, "Call tool request must not be null");

        return withInitializationCheck("call tool", initResult ->
                mcpSession.sendRequest(METHOD_TOOLS_CALL, callToolRequest, CALL_TOOL_RESULT_TYPE_REF)
        );
    }

    /**
     * Retrieves the list of all tools provided by the server.
     *
     * @return A CompletableFuture that emits the list of tools result.
     */
    public CompletableFuture<ListToolsResult> listTools() {
        return listTools(null);
    }

    /**
     * Retrieves a paginated list of tools provided by the server.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return A CompletableFuture that emits the list of tools result
     */
    public CompletableFuture<ListToolsResult> listTools(String cursor) {
        return withInitializationCheck("list tools", initResult ->
                mcpSession.sendRequest(
                        METHOD_TOOLS_LIST,
                        new PaginatedRequest(cursor),
                        LIST_TOOLS_RESULT_TYPE_REF
                )
        );
    }

    /**
     * Handler for tools/list_changed notifications.
     *
     * @param toolsChangeConsumers The consumers to notify when tools change
     * @return The notification handler
     */
    private NotificationHandler asyncToolsChangeNotificationHandler(
            List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers) {
        return params -> {
            if (params == null) {
                return CompletableFuture.completedFuture(null);
            }

            try {
                @SuppressWarnings("unchecked")
                List<Tool> tools = (List<Tool>) params;

                CompletableFuture<Void> result = CompletableFuture.completedFuture(null);

                for (Function<List<Tool>, CompletableFuture<Void>> consumer : toolsChangeConsumers) {
                    result = result.thenCompose(v -> consumer.apply(tools));
                }

                return result;
            } catch (Exception e) {
                logger.error("Error handling tools change notification", e);
                CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
        };
    }

    /**
     * Retrieves the list of all resources provided by the server. Resources represent any
     * kind of UTF-8 encoded data that an MCP server makes available to clients, such as
     * database records, API responses, log files, and more.
     *
     * @return A CompletableFuture that completes with the list of resources result.
     * @see ListResourcesResult
     * @see #readResource(Resource)
     */
    public CompletableFuture<ListResourcesResult> listResources() {
        return listResources(null);
    }

    /**
     * Retrieves a paginated list of resources provided by the server. Resources represent
     * any kind of UTF-8 encoded data that an MCP server makes available to clients, such
     * as database records, API responses, log files, and more.
     *
     * @param cursor Optional pagination cursor from a previous list request.
     * @return A CompletableFuture that completes with the list of resources result.
     * @see ListResourcesResult
     * @see #readResource(Resource)
     */
    public CompletableFuture<ListResourcesResult> listResources(String cursor) {
        return withInitializationCheck("list resources", initResult ->
                mcpSession.sendRequest(
                        METHOD_RESOURCES_LIST,
                        new PaginatedRequest(cursor),
                        LIST_RESOURCES_RESULT_TYPE_REF
                )
        );
    }

    /**
     * Reads the content of a specific resource identified by the provided Resource
     * object. This method fetches the actual data that the resource represents.
     *
     * @param resource The resource to read, containing the URI that identifies the
     *                 resource.
     * @return A CompletableFuture that completes with the resource content.
     * @see Resource
     * @see ReadResourceResult
     */
    public CompletableFuture<ReadResourceResult> readResource(Resource resource) {
        Assert.notNull(resource, "Resource must not be null");
        Assert.hasText(resource.uri(), "Resource URI must not be empty");

        ReadResourceRequest request = new ReadResourceRequest(resource.uri());
        return readResource(request);
    }

    /**
     * Reads the content of a specific resource identified by the provided request. This
     * method fetches the actual data that the resource represents.
     *
     * @param readResourceRequest The request containing the URI of the resource to read
     * @return A CompletableFuture that completes with the resource content.
     * @see ReadResourceRequest
     * @see ReadResourceResult
     */
    public CompletableFuture<ReadResourceResult> readResource(ReadResourceRequest readResourceRequest) {
        Assert.notNull(readResourceRequest, "Read resource request must not be null");

        return withInitializationCheck("read resource", initResult ->
                mcpSession.sendRequest(
                        METHOD_RESOURCES_READ,
                        readResourceRequest,
                        READ_RESOURCE_RESULT_TYPE_REF
                )
        );
    }

    /**
     * Retrieves the list of all resource templates provided by the server. Resource
     * templates allow servers to expose parameterized resources using URI templates,
     * enabling dynamic resource access based on variable parameters.
     *
     * @return A CompletableFuture that completes with the list of resource templates result.
     * @see ListResourceTemplatesResult
     */
    public CompletableFuture<ListResourceTemplatesResult> listResourceTemplates() {
        return listResourceTemplates(null);
    }

    /**
     * Retrieves a paginated list of resource templates provided by the server. Resource
     * templates allow servers to expose parameterized resources using URI templates,
     * enabling dynamic resource access based on variable parameters.
     *
     * @param cursor Optional pagination cursor from a previous list request.
     * @return A CompletableFuture that completes with the list of resource templates result.
     * @see ListResourceTemplatesResult
     */
    public CompletableFuture<ListResourceTemplatesResult> listResourceTemplates(String cursor) {
        return withInitializationCheck("list resource templates", initResult ->
                mcpSession.sendRequest(
                        METHOD_RESOURCES_TEMPLATES_LIST,
                        new PaginatedRequest(cursor),
                        LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF
                )
        );
    }

    /**
     * Subscribes to changes in a specific resource. When the resource changes on the
     * server, the client will receive notifications through the resources change
     * notification handler.
     *
     * @param subscribeRequest The subscribe request containing the URI of the resource.
     * @return A CompletableFuture that completes when the subscription is complete.
     * @see SubscribeRequest
     * @see #unsubscribeResource(UnsubscribeRequest)
     */
    public CompletableFuture<Void> subscribeResource(SubscribeRequest subscribeRequest) {
        Assert.notNull(subscribeRequest, "Subscribe request must not be null");

        return withInitializationCheck("subscribe resource", initResult ->
                mcpSession.sendRequest(
                        METHOD_RESOURCES_SUBSCRIBE,
                        subscribeRequest,
                        VOID_TYPE_REFERENCE
                )
        );
    }

    /**
     * Cancels an existing subscription to a resource. After unsubscribing, the client
     * will no longer receive notifications when the resource changes.
     *
     * @param unsubscribeRequest The unsubscribe request containing the URI of the
     *                           resource.
     * @return A CompletableFuture that completes when the unsubscription is complete.
     * @see UnsubscribeRequest
     * @see #subscribeResource(SubscribeRequest)
     */
    public CompletableFuture<Void> unsubscribeResource(UnsubscribeRequest unsubscribeRequest) {
        Assert.notNull(unsubscribeRequest, "Unsubscribe request must not be null");

        return withInitializationCheck("unsubscribe resource", initResult ->
                mcpSession.sendRequest(
                        METHOD_RESOURCES_UNSUBSCRIBE,
                        unsubscribeRequest,
                        VOID_TYPE_REFERENCE
                )
        );
    }

    /**
     * Handler for resources/list_changed notifications.
     *
     * @param resourcesChangeConsumers The consumers to notify when resources change
     * @return The notification handler
     */
    private NotificationHandler asyncResourcesChangeNotificationHandler(
            List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers) {
        return params -> {
            if (params == null) {
                return CompletableFuture.completedFuture(null);
            }

            try {
                @SuppressWarnings("unchecked")
                List<Resource> resources = (List<Resource>) params;

                CompletableFuture<Void> result = CompletableFuture.completedFuture(null);

                for (Function<List<Resource>, CompletableFuture<Void>> consumer : resourcesChangeConsumers) {
                    result = result.thenCompose(v -> consumer.apply(resources));
                }

                return result;
            } catch (Exception e) {
                logger.error("Error handling resources change notification", e);
                CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
        };
    }

    /**
     * Retrieves the list of all prompts provided by the server.
     *
     * @return A CompletableFuture that completes with the list of prompts result.
     * @see ListPromptsResult
     * @see #getPrompt(GetPromptRequest)
     */
    public CompletableFuture<ListPromptsResult> listPrompts() {
        return listPrompts(null);
    }

    /**
     * Retrieves a paginated list of prompts provided by the server.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return A CompletableFuture that completes with the list of prompts result.
     * @see ListPromptsResult
     * @see #getPrompt(GetPromptRequest)
     */
    public CompletableFuture<ListPromptsResult> listPrompts(String cursor) {
        return withInitializationCheck("list prompts", initResult ->
                mcpSession.sendRequest(
                        METHOD_PROMPT_LIST,
                        new PaginatedRequest(cursor),
                        LIST_PROMPTS_RESULT_TYPE_REF
                )
        );
    }

    /**
     * Retrieves a specific prompt by its ID. This provides the complete prompt template
     * including all parameters and instructions for generating AI content.
     *
     * @param getPromptRequest The request containing the ID of the prompt to retrieve.
     * @return A CompletableFuture that completes with the prompt result.
     * @see GetPromptRequest
     * @see GetPromptResult
     * @see #listPrompts()
     */
    public CompletableFuture<GetPromptResult> getPrompt(GetPromptRequest getPromptRequest) {
        Assert.notNull(getPromptRequest, "Get prompt request must not be null");

        return withInitializationCheck("get prompt", initResult ->
                mcpSession.sendRequest(
                        METHOD_PROMPT_GET,
                        getPromptRequest,
                        GET_PROMPT_RESULT_TYPE_REF
                )
        );
    }

    /**
     * Handler for prompts/list_changed notifications.
     *
     * @param promptsChangeConsumers The consumers to notify when prompts change
     * @return The notification handler
     */
    private NotificationHandler asyncPromptsChangeNotificationHandler(
            List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers) {
        return params -> {
            if (params == null) {
                return CompletableFuture.completedFuture(null);
            }

            try {
                @SuppressWarnings("unchecked")
                List<Prompt> prompts = (List<Prompt>) params;

                CompletableFuture<Void> result = CompletableFuture.completedFuture(null);

                for (Function<List<Prompt>, CompletableFuture<Void>> consumer : promptsChangeConsumers) {
                    result = result.thenCompose(v -> consumer.apply(prompts));
                }

                return result;
            } catch (Exception e) {
                logger.error("Error handling prompts change notification", e);
                CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
        };
    }

    /**
     * Handler for logging notifications.
     *
     * @param loggingConsumers The consumers to notify when logging messages are received
     * @return The notification handler
     */
    private NotificationHandler asyncLoggingNotificationHandler(
            List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers) {
        return params -> {
            if (params == null) {
                return CompletableFuture.completedFuture(null);
            }

            try {
                LoggingMessageNotification notification = transport.unmarshalFrom(
                        params,
                        new TypeReference<LoggingMessageNotification>() {
                        }
                );

                CompletableFuture<Void> result = CompletableFuture.completedFuture(null);

                for (Function<LoggingMessageNotification, CompletableFuture<Void>> consumer : loggingConsumers) {
                    result = result.thenCompose(v -> consumer.apply(notification));
                }

                return result;
            } catch (Exception e) {
                logger.error("Error handling logging notification", e);
                CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
        };
    }

    /**
     * Sets the minimum logging level for messages received from the server. The client
     * will only receive log messages at or above the specified severity level.
     *
     * @param loggingLevel The minimum logging level to receive.
     * @return A CompletableFuture that completes when the logging level is set.
     * @see LoggingLevel
     */
    public CompletableFuture<Void> setLoggingLevel(LoggingLevel loggingLevel) {
        Assert.notNull(loggingLevel, "Logging level must not be null");

        Map<String, Object> params = new HashMap<>();
        params.put("level", loggingLevel);

        return withInitializationCheck("set logging level", initResult ->
                mcpSession.sendRequest(
                        METHOD_LOGGING_SET_LEVEL,
                        params,
                        VOID_TYPE_REFERENCE
                )
        );
    }

    /**
     * This method is package-private and used for test only. Should not be called by user
     * code.
     *
     * @param protocolVersions the Client supported protocol versions.
     */
    void setProtocolVersions(List<String> protocolVersions) {
        this.protocolVersions = protocolVersions;
    }
}

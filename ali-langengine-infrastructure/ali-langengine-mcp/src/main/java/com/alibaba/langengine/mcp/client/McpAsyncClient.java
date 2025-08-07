/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.mcp.client;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.mcp.spec.*;
import com.alibaba.langengine.mcp.spec.schema.*;
import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptRequest;
import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptResult;
import com.alibaba.langengine.mcp.spec.schema.prompts.ListPromptsResult;
import com.alibaba.langengine.mcp.spec.schema.prompts.Prompt;
import com.alibaba.langengine.mcp.spec.schema.resources.*;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolResult;
import com.alibaba.langengine.mcp.spec.schema.tools.ListToolsResult;
import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
import com.alibaba.langengine.mcp.util.Assert;
import com.alibaba.langengine.mcp.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Model Context Protocol (MCP) client implementation that provides asynchronous
 * communication with MCP servers.
 *
 * @author Dariusz Jędrzejczyk
 * @author Christian Tzolov
 */
public class McpAsyncClient {

	private final static Logger logger = LoggerFactory.getLogger(McpAsyncClient.class);

	private static TypeReference<Void> VOID_TYPE_REFERENCE = new TypeReference<Void>() {
	};

	/**
	 * The MCP session implementation that manages bidirectional JSON-RPC communication
	 * between clients and servers.
	 */
	private final DefaultMcpSession mcpSession;

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
	private Function<CreateMessageRequest, CreateMessageResult> samplingHandler;

	/**
	 * Client transport implementation.
	 */
	private final McpTransport transport;

	/**
	 * Create a new McpAsyncClient with the given transport and session request-response
	 * timeout.
	 * @param transport the transport to use.
	 * @param requestTimeout the session request-response timeout.
	 * @param clientInfo the client implementation information.
	 * @param clientCapabilities the client capabilities.
	 * @param roots the roots.
	 * @param toolsChangeConsumers the tools change consumers.
	 * @param resourcesChangeConsumers the resources change consumers.
	 * @param promptsChangeConsumers the prompts change consumers.
	 * @param loggingConsumers the logging consumers.
	 * @param samplingHandler the sampling handler.
	 */
	public McpAsyncClient(ClientMcpTransport transport, Duration requestTimeout, Implementation clientInfo,
			ClientCapabilities clientCapabilities, Map<String, Root> roots,
			List<Consumer<List<Tool>>> toolsChangeConsumers,
			List<Consumer<List<Resource>>> resourcesChangeConsumers,
			List<Consumer<List<Prompt>>> promptsChangeConsumers,
			List<Consumer<LoggingMessageNotification>> loggingConsumers,
			Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {

		Assert.notNull(transport, "Transport must not be null");
		Assert.notNull(requestTimeout, "Request timeout must not be null");
		Assert.notNull(clientInfo, "Client info must not be null");

		this.clientInfo = clientInfo;

		this.clientCapabilities = (clientCapabilities != null) ? clientCapabilities
				: new ClientCapabilities(null, !Utils.isEmpty(roots) ? new RootCapabilities(false) : null,
						samplingHandler != null ? new Sampling() : null);

		this.transport = transport;

		this.roots = roots != null ? new ConcurrentHashMap<>(roots) : new ConcurrentHashMap<>();

		// Request Handlers
		Map<String, DefaultMcpSession.RequestHandler> requestHanlers = new HashMap<>();

		// Roots List Request Handler
		if (this.roots != null && this.clientCapabilities.getRoots() != null) {
			requestHanlers.put(MethodDefined.RootsList.getValue(), rootsListRequestHandler());
		}

		// Sampling Handler
		if (this.clientCapabilities.getSampling() != null) {
			if (samplingHandler == null) {
				throw new McpError("Sampling handler must not be null when client capabilities include sampling");
			}
			this.samplingHandler = samplingHandler;
			requestHanlers.put(MethodDefined.SamplingCreateMessage.getValue(), samplingCreateMessageHandler());
		}

		// Notification Handlers
		Map<String, DefaultMcpSession.NotificationHandler> notificationHandlers = new HashMap<>();

		// Tools Change Notification
		List<Consumer<List<Tool>>> toolsChangeConsumersFinal = new ArrayList<>();
		toolsChangeConsumersFinal.add((notification) -> logger.info("Tools changed: {}", notification));
		if (!Utils.isEmpty(toolsChangeConsumers)) {
			toolsChangeConsumersFinal.addAll(toolsChangeConsumers);
		}
		notificationHandlers.put(MethodDefined.NotificationsToolsListChanged.getValue(),
				toolsChangeNotificationHandler(toolsChangeConsumersFinal));

		// Resources Change Notification
		List<Consumer<List<Resource>>> resourcesChangeConsumersFinal = new ArrayList<>();
		resourcesChangeConsumersFinal.add((notification) -> logger.info("Resources changed: {}", notification));
		if (!Utils.isEmpty(resourcesChangeConsumers)) {
			resourcesChangeConsumersFinal.addAll(resourcesChangeConsumers);
		}
		notificationHandlers.put(MethodDefined.NotificationsResourcesListChanged.getValue(),
				resourcesChangeNotificationHandler(resourcesChangeConsumersFinal));

		// Prompts Change Notification
		List<Consumer<List<Prompt>>> promptsChangeConsumersFinal = new ArrayList<>();
		promptsChangeConsumersFinal.add((notification) -> logger.info("Prompts changed: {}", notification));
		if (!Utils.isEmpty(promptsChangeConsumers)) {
			promptsChangeConsumersFinal.addAll(promptsChangeConsumers);
		}
		notificationHandlers.put(MethodDefined.NotificationsPromptsListChanged.getValue(),
				promptsChangeNotificationHandler(promptsChangeConsumersFinal));

		// Utility Logging Notification
		List<Consumer<LoggingMessageNotification>> loggingConsumersFinal = new ArrayList<>();
		loggingConsumersFinal.add((notification) -> logger.info("Logging: {}", notification));
		if (!Utils.isEmpty(loggingConsumers)) {
			loggingConsumersFinal.addAll(loggingConsumers);
		}
		notificationHandlers.put(MethodDefined.NotificationsMessage.getValue(),
				loggingNotificationHandler(loggingConsumersFinal));

		this.mcpSession = new DefaultMcpSession(requestTimeout, transport, requestHanlers, notificationHandlers);

	}

	// --------------------------
	// Lifecycle
	// --------------------------
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
	 * <ul>
	 * <li>The protocol version the client supports</li>
	 * <li>The client's capabilities</li>
	 * <li>Client implementation information</li>
	 * </ul>
	 *
	 * The server MUST respond with its own capabilities and information:
	 * After successful initialization, the client MUST send an initialized notification
	 * to indicate it is ready to begin normal operations.
	 *
	 * <br/>
	 *
	 * <a href=
	 * "https://github.com/modelcontextprotocol/specification/blob/main/docs/specification/basic/lifecycle.md#initialization">Initialization
	 * Spec</a>
	 * @return the initialize result.
	 */
	public Mono<InitializeResult> initialize() {
		InitializeRequest initializeRequest = new InitializeRequest(
                Schema.LATEST_PROTOCOL_VERSION,
                this.clientCapabilities,
                this.clientInfo); // @formatter:on

		Mono<InitializeResult> result = this.mcpSession.sendRequest(MethodDefined.Initialize.getValue(),
				initializeRequest, new TypeReference<InitializeResult>() {
				});

		return result.flatMap(initializeResult -> {

			this.serverCapabilities = initializeResult.getCapabilities();
			this.serverInfo = initializeResult.getServerInfo();

			logger.info("Server response with Protocol: {}, Capabilities: {}, Info: {} and Instructions {}",
					initializeResult.getProtocolVersion(), initializeResult.getCapabilities(), initializeResult.getServerInfo(),
					initializeResult.getInstructions());

			if (!Schema.LATEST_PROTOCOL_VERSION.equals(initializeResult.getProtocolVersion())) {
				return Mono.error(new McpError(
						"Unsupported protocol version from the server: " + initializeResult.getProtocolVersion()));
			}
			else {
				return this.mcpSession.sendNotification(MethodDefined.NotificationsInitialized.getValue(), null)
					.thenReturn(initializeResult);
			}
		});
	}

	/**
	 * Get the server capabilities that define the supported features and functionality.
	 * @return The server capabilities
	 */
	public ServerCapabilities getServerCapabilities() {
		return this.serverCapabilities;
	}

	/**
	 * Get the server implementation information.
	 * @return The server implementation details
	 */
	public Implementation getServerInfo() {
		return this.serverInfo;
	}

	/**
	 * Get the client capabilities that define the supported features and functionality.
	 * @return The client capabilities
	 */
	public ClientCapabilities getClientCapabilities() {
		return this.clientCapabilities;
	}

	/**
	 * Get the client implementation information.
	 * @return The client implementation details
	 */
	public Implementation getClientInfo() {
		return this.clientInfo;
	}

	/**
	 * Closes the client connection immediately.
	 */
	public void close() {
		this.mcpSession.close();
	}

	/**
	 * Gracefully closes the client connection.
	 * @return A Mono that completes when the connection is closed
	 */
	public Mono<Void> closeGracefully() {
		return this.mcpSession.closeGracefully();
	}

	// --------------------------
	// Basic Utilities
	// --------------------------

	/**
	 * Sends a ping request to the server.
	 * @return A Mono that completes with the server's ping response
	 */
	public Mono<Object> ping() {
		return this.mcpSession.sendRequest(MethodDefined.Ping.getValue(), null, new TypeReference<Object>() {
		});
	}

	// --------------------------
	// Roots
	// --------------------------
	/**
	 * Adds a new root to the client's root list.
	 * @param root The root to add
	 * @return A Mono that completes when the root is added and notifications are sent
	 */
	public Mono<Void> addRoot(Root root) {

		if (root == null) {
			return Mono.error(new McpError("Root must not be null"));
		}

		if (this.clientCapabilities.getRoots() == null) {
			return Mono.error(new McpError("Client must be configured with roots capabilities"));
		}

		if (this.roots.containsKey(root.getUri())) {
			return Mono.error(new McpError("Root with uri '" + root.getUri() + "' already exists"));
		}

		this.roots.put(root.getUri(), root);

		logger.info("Added root: {}", root);

		if (this.clientCapabilities.getRoots().getListChanged()) {
			return this.rootsListChangedNotification();
		}
		return Mono.empty();
	}

	/**
	 * Removes a root from the client's root list.
	 * @param rootUri The URI of the root to remove
	 * @return A Mono that completes when the root is removed and notifications are sent
	 */
	public Mono<Void> removeRoot(String rootUri) {

		if (rootUri == null) {
			return Mono.error(new McpError("Root uri must not be null"));
		}

		if (this.clientCapabilities.getRoots() == null) {
			return Mono.error(new McpError("Client must be configured with roots capabilities"));
		}

		Root removed = this.roots.remove(rootUri);

		if (removed != null) {
			logger.info("Removed Root: {}", rootUri);
			if (this.clientCapabilities.getRoots().getListChanged()) {
				return this.rootsListChangedNotification();
			}
			return Mono.empty();
		}
		return Mono.error(new McpError("Root with uri '" + rootUri + "' not found"));
	}

	/**
	 * Manually sends a roots/list_changed notification. The addRoot and removeRoot
	 * methods automatically send the roots/list_changed notification.
	 * @return A Mono that completes when the notification is sent
	 */
	public Mono<Void> rootsListChangedNotification() {
		return this.mcpSession.sendNotification(MethodDefined.NotificationsRootsListChanged.getValue());
	}

	private DefaultMcpSession.RequestHandler rootsListRequestHandler() {
		return params -> {
			PaginatedRequest request = transport.unmarshalFrom(params,
					new TypeReference<PaginatedRequest>() {
					});

			List<Root> roots = this.roots.values().stream().collect(Collectors.toList());

			return Mono.just(new ListRootsResult(roots));
		};
	}

	// --------------------------
	// Sampling
	// --------------------------
	private DefaultMcpSession.RequestHandler samplingCreateMessageHandler() {
		return params -> {
			CreateMessageRequest request = transport.unmarshalFrom(params,
					new TypeReference<CreateMessageRequest>() {
					});

			CreateMessageResult response = this.samplingHandler.apply(request);

			return Mono.just(response);
		};
	}

	// --------------------------
	// Tools
	// --------------------------
	private static TypeReference<CallToolResult> CALL_TOOL_RESULT_TYPE_REF = new TypeReference<CallToolResult>() {
	};

	private static TypeReference<ListToolsResult> LIST_TOOLS_RESULT_TYPE_REF = new TypeReference<ListToolsResult>() {
	};

	/**
	 * Calls a tool provided by the server. Tools enable servers to expose executable
	 * functionality that can interact with external systems, perform computations, and
	 * take actions in the real world.
	 * @param callToolRequest The request containing: - name: The name of the tool to call
	 * (must match a tool name from tools/list) - arguments: Arguments that conform to the
	 * tool's input schema
	 * @return A Mono that emits the tool execution result containing: - content: List of
	 * content items (text, images, or embedded resources) representing the tool's output
	 * - isError: Boolean indicating if the execution failed (true) or succeeded
	 * (false/absent)
	 */
	public Mono<CallToolResult> callTool(CallToolRequest callToolRequest) {
		return this.mcpSession.sendRequest(MethodDefined.ToolsCall.getValue(), callToolRequest, CALL_TOOL_RESULT_TYPE_REF);
	}

	/**
	 * Retrieves the list of all tools provided by the server.
	 * @return A Mono that emits the list of tools result containing: - tools: List of
	 * available tools, each with a name, description, and input schema - nextCursor:
	 * Optional cursor for pagination if more tools are available
	 */
	public Mono<ListToolsResult> listTools() {
		return this.listTools(null);
	}

	/**
	 * Retrieves a paginated list of tools provided by the server.
	 * @param cursor Optional pagination cursor from a previous list request
	 * @return A Mono that emits the list of tools result containing: - tools: List of
	 * available tools, each with a name, description, and input schema - nextCursor:
	 * Optional cursor for pagination if more tools are available
	 */
	public Mono<ListToolsResult> listTools(String cursor) {
		return this.mcpSession.sendRequest(MethodDefined.ToolsList.getValue(), new PaginatedRequest(cursor),
				LIST_TOOLS_RESULT_TYPE_REF);
	}

	/**
	 * Creates a notification handler for tools/list_changed notifications from the
	 * server. When the server's available tools change, it sends a notification to inform
	 * connected clients. This handler automatically fetches the updated tool list and
	 * distributes it to all registered consumers.
	 * @param toolsChangeConsumers List of consumers that will be notified when the tools
	 * list changes. Each consumer receives the complete updated list of tools.
	 * @return A NotificationHandler that processes tools/list_changed notifications by:
	 * 1. Fetching the current list of tools from the server 2. Distributing the updated
	 * list to all registered consumers 3. Handling any errors that occur during this
	 * process
	 */
	private DefaultMcpSession.NotificationHandler toolsChangeNotificationHandler(
			List<Consumer<List<Tool>>> toolsChangeConsumers) {

		return params -> listTools().flatMap(listToolsResult -> Mono.fromRunnable(() -> {
			for (Consumer<List<Tool>> toolsChangeConsumer : toolsChangeConsumers) {
				toolsChangeConsumer.accept(listToolsResult.getTools());
			}
		}).subscribeOn(Schedulers.boundedElastic())).onErrorResume(error -> {
			logger.error("Error handling tools list change notification", error);
			return Mono.empty();
		}).then(); // Convert to Mono<Void>
	};

	// --------------------------
	// Resources
	// --------------------------

	private static TypeReference<ListResourcesResult> LIST_RESOURCES_RESULT_TYPE_REF = new TypeReference<ListResourcesResult>() {
	};

	private static TypeReference<ReadResourceResult> READ_RESOURCE_RESULT_TYPE_REF = new TypeReference<ReadResourceResult>() {
	};

	private static TypeReference<ListResourceTemplatesResult> LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF = new TypeReference<ListResourceTemplatesResult>() {
	};

	/**
	 * Send a resources/list request.
	 * @return A Mono that completes with the list of resources result
	 */
	public Mono<ListResourcesResult> listResources() {
		return this.listResources(null);
	}

	/**
	 * Send a resources/list request.
	 * @param cursor the cursor for pagination
	 * @return A Mono that completes with the list of resources result
	 */
	public Mono<ListResourcesResult> listResources(String cursor) {
		return this.mcpSession.sendRequest(MethodDefined.ResourcesList.getValue(), new PaginatedRequest(cursor),
				LIST_RESOURCES_RESULT_TYPE_REF);
	}

	/**
	 * Send a resources/read request.
	 * @param resource the resource to read
	 * @return A Mono that completes with the resource content
	 */
	public Mono<ReadResourceResult> readResource(Resource resource) {
		return this.readResource(new ReadResourceRequest(resource.getUri()));
	}

	/**
	 * Send a resources/read request.
	 * @param readResourceRequest the read resource request
	 * @return A Mono that completes with the resource content
	 */
	public Mono<ReadResourceResult> readResource(ReadResourceRequest readResourceRequest) {
		return this.mcpSession.sendRequest(MethodDefined.ResourcesRead.getValue(), readResourceRequest,
				READ_RESOURCE_RESULT_TYPE_REF);
	}

	/**
	 * Resource templates allow servers to expose parameterized resources using URI
	 * templates. Arguments may be auto-completed through the completion API.
	 *
	 * Request a list of resource templates the server has.
	 * @return A Mono that completes with the list of resource templates result
	 */
	public Mono<ListResourceTemplatesResult> listResourceTemplates() {
		return this.listResourceTemplates(null);
	}

	/**
	 * Resource templates allow servers to expose parameterized resources using URI
	 * templates. Arguments may be auto-completed through the completion API.
	 *
	 * Request a list of resource templates the server has.
	 * @param cursor the cursor for pagination
	 * @return A Mono that completes with the list of resource templates result
	 */
	public Mono<ListResourceTemplatesResult> listResourceTemplates(String cursor) {
		return this.mcpSession.sendRequest(MethodDefined.ResourcesTemplatesList.getValue(),
				new PaginatedRequest(cursor), LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF);
	}

	/**
	 * List Changed Notification. When the list of available resources changes, servers
	 * that declared the listChanged capability SHOULD send a notification.
	 * @return A Mono that completes when the notification is sent
	 */
	public Mono<Void> sendResourcesListChanged() {
		return this.mcpSession.sendNotification(MethodDefined.NotificationsResourcesListChanged.getValue());
	}

	/**
	 * Subscriptions. The protocol supports optional subscriptions to resource changes.
	 * Clients can subscribe to specific resources and receive notifications when they
	 * change.
	 *
	 * Send a resources/subscribe request.
	 * @param subscribeRequest the subscribe request contains the uri of the resource to
	 * subscribe to
	 * @return A Mono that completes when the subscription is complete
	 */
	public Mono<Void> subscribeResource(SubscribeRequest subscribeRequest) {
		return this.mcpSession.sendRequest(MethodDefined.ResourcesSubscribe.getValue(), subscribeRequest, VOID_TYPE_REFERENCE);
	}

	/**
	 * Send a resources/unsubscribe request.
	 * @param unsubscribeRequest the unsubscribe request contains the uri of the resource
	 * to unsubscribe from
	 * @return A Mono that completes when the unsubscription is complete
	 */
	public Mono<Void> unsubscribeResource(UnsubscribeRequest unsubscribeRequest) {
		return this.mcpSession.sendRequest(MethodDefined.ResourcesUnsubscribe.getValue(), unsubscribeRequest,
				VOID_TYPE_REFERENCE);
	}

	private DefaultMcpSession.NotificationHandler resourcesChangeNotificationHandler(
			List<Consumer<List<Resource>>> resourcesChangeConsumers) {

		return params -> listResources().flatMap(listResourcesResult -> Mono.fromRunnable(() -> {
			for (Consumer<List<Resource>> resourceChangeConsumer : resourcesChangeConsumers) {
				resourceChangeConsumer.accept(listResourcesResult.getResources());
			}
		}).subscribeOn(Schedulers.boundedElastic())).onErrorResume(error -> {
			logger.error("Error handling resources list change notification", error);
			return Mono.empty();
		}).then();
	};

	// --------------------------
	// Prompts
	// --------------------------
	private static TypeReference<ListPromptsResult> LIST_PROMPTS_RESULT_TYPE_REF = new TypeReference<ListPromptsResult>() {
	};

	private static TypeReference<GetPromptResult> GET_PROMPT_RESULT_TYPE_REF = new TypeReference<GetPromptResult>() {
	};

	/**
	 * List all available prompts.
	 * @return A Mono that completes with the list of prompts result
	 */
	public Mono<ListPromptsResult> listPrompts() {
		return this.listPrompts(null);
	}

	/**
	 * List all available prompts.
	 * @param cursor the cursor for pagination
	 * @return A Mono that completes with the list of prompts result
	 */
	public Mono<ListPromptsResult> listPrompts(String cursor) {
		return this.mcpSession.sendRequest(MethodDefined.PromptsList.getValue(), new PaginatedRequest(cursor),
				LIST_PROMPTS_RESULT_TYPE_REF);
	}

	/**
	 * Get a prompt by its id.
	 * @param getPromptRequest the get prompt request
	 * @return A Mono that completes with the get prompt result
	 */
	public Mono<GetPromptResult> getPrompt(GetPromptRequest getPromptRequest) {
		return this.mcpSession.sendRequest(MethodDefined.PromptsGet.getValue(), getPromptRequest, GET_PROMPT_RESULT_TYPE_REF);
	}

	/**
	 * (Server) An optional notification from the server to the client, informing it that
	 * the list of prompts it offers has changed. This may be issued by servers without
	 * any previous subscription from the client.
	 * @return A Mono that completes when the notification is sent
	 */
	public Mono<Void> promptListChangedNotification() {
		return this.mcpSession.sendNotification(MethodDefined.NotificationsPromptsListChanged.getValue());
	}

	private DefaultMcpSession.NotificationHandler promptsChangeNotificationHandler(
			List<Consumer<List<Prompt>>> promptsChangeConsumers) {

		return params -> {// @formatter:off
			return listPrompts().flatMap(listPromptsResult -> Mono.fromRunnable(() -> {
				for (Consumer<List<Prompt>> promptChangeConsumer : promptsChangeConsumers) {
					promptChangeConsumer.accept(listPromptsResult.getPrompts());
				}
			}).subscribeOn(Schedulers.boundedElastic())).onErrorResume(error -> {
				logger.error("Error handling prompts list change notification", error);
				return Mono.empty();
			}).then(); // Convert to Mono<Void>
		}; // @formatter:on
	};

	// --------------------------
	// Logging
	// --------------------------
	/**
	 * Create a notification handler for logging notifications from the server. This
	 * handler automatically distributes logging messages to all registered consumers.
	 * @param loggingConsumers List of consumers that will be notified when a logging
	 * message is received. Each consumer receives the logging message notification.
	 * @return A NotificationHandler that processes log notifications by distributing the
	 * message to all registered consumers
	 */
	private DefaultMcpSession.NotificationHandler loggingNotificationHandler(
			List<Consumer<LoggingMessageNotification>> loggingConsumers) {

		return params -> {

			LoggingMessageNotification loggingMessageNotification = transport.unmarshalFrom(params,
					new TypeReference<LoggingMessageNotification>() {
					});

			return Mono.fromRunnable(() -> {
				for (Consumer<LoggingMessageNotification> loggingConsumer : loggingConsumers) {
					loggingConsumer.accept(loggingMessageNotification);
				}
			}).subscribeOn(Schedulers.boundedElastic()).then();

		};
	}

	/**
	 * Client can set the minimum logging level it wants to receive from the server.
	 * @param loggingLevel the min logging level
	 */
	public Mono<Void> setLoggingLevel(LoggingLevel loggingLevel) {
		Assert.notNull(loggingLevel, "Logging level must not be null");

		String levelName = this.transport.unmarshalFrom(loggingLevel, new TypeReference<String>() {
		});

		Map<String, Object> params = Collections.singletonMap("level", levelName);

		return this.mcpSession.sendNotification(MethodDefined.LoggingSetLevel.getValue(), params);
	}

}

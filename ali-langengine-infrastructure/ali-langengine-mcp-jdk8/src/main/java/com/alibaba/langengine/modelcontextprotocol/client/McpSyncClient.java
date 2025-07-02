/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.client;

import com.alibaba.langengine.modelcontextprotocol.spec.*;
import com.alibaba.langengine.modelcontextprotocol.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A synchronous client implementation for the Model Context Protocol (MCP) that wraps an
 * {@link McpAsyncClient} to provide blocking operations.
 *
 * <p>
 * This client implements the MCP specification by delegating to an asynchronous client
 * and blocking on the results. Key features include:
 * <ul>
 * <li>Synchronous, blocking API for simpler integration in non-reactive applications
 * <li>Tool discovery and invocation for server-provided functionality
 * <li>Resource access and management with URI-based addressing
 * <li>Prompt template handling for standardized AI interactions
 * <li>Real-time notifications for tools, resources, and prompts changes
 * <li>Structured logging with configurable severity levels
 * </ul>
 *
 * <p>
 * The client follows the same lifecycle as its async counterpart:
 * <ol>
 * <li>Initialization - Establishes connection and negotiates capabilities
 * <li>Normal Operation - Handles requests and notifications
 * <li>Graceful Shutdown - Ensures clean connection termination
 * </ol>
 *
 * <p>
 * This implementation implements {@link AutoCloseable} for resource cleanup and provides
 * both immediate and graceful shutdown options. All operations block until completion or
 * timeout, making it suitable for traditional synchronous programming models.
 * <p>
 * JDK 1.8 compatible version.
 *
 * @author Dariusz Jędrzejczyk
 * @author Christian Tzolov
 * @see McpClient
 * @see McpAsyncClient
 */
public class McpSyncClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(McpSyncClient.class);

    // TODO: Consider providing a client config to set this properly
    // this is currently a concern only because AutoCloseable is used - perhaps it
    // is not a requirement?
    private static final long DEFAULT_CLOSE_TIMEOUT_MS = 10_000L;

    private final McpAsyncClient delegate;

    /**
     * Create a new McpSyncClient with the given delegate.
     * @param delegate the asynchronous kernel on top of which this synchronous client
     * provides a blocking API.
     */
    McpSyncClient(McpAsyncClient delegate) {
        Assert.notNull(delegate, "The delegate can not be null");
        this.delegate = delegate;
    }
    
    /**
     * Create a new McpSyncClient with the given parameters.
     * @param transport the transport to use
     * @param requestTimeout the request timeout
     * @param initializationTimeout the initialization timeout
     * @param capabilities the client capabilities
     * @param clientInfo the client implementation information
     * @param roots the client roots
     * @param toolsChangeConsumers the tools change consumers
     * @param resourcesChangeConsumers the resources change consumers
     * @param promptsChangeConsumers the prompts change consumers
     * @param loggingConsumers the logging consumers
     * @param samplingHandler the sampling handler
     */
    McpSyncClient(McpClientTransport transport,
                  Duration requestTimeout,
                  Duration initializationTimeout,
                  ClientCapabilities capabilities,
                  Implementation clientInfo,
                  List<Root> roots,
                  List<Consumer<List<Tool>>> toolsChangeConsumers,
                  List<Consumer<List<Resource>>> resourcesChangeConsumers,
                  List<Consumer<List<Prompt>>> promptsChangeConsumers,
                  List<Consumer<LoggingMessageNotification>> loggingConsumers,
                  Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {
        
        McpClientFeatures.Sync syncFeatures = new McpClientFeatures.Sync(
                capabilities, 
                clientInfo,
                null, // We'll add roots directly to the async client
                toolsChangeConsumers,
                resourcesChangeConsumers,
                promptsChangeConsumers,
                loggingConsumers,
                samplingHandler);

        McpClientFeatures.Async asyncFeatures = McpClientFeatures.Async.fromSync(syncFeatures);

        this.delegate = new McpAsyncClient(
                transport, 
                requestTimeout, 
                initializationTimeout, 
                capabilities, 
                clientInfo, 
                roots, 
                asyncFeatures.toolsChangeConsumers(),
                asyncFeatures.resourcesChangeConsumers(),
                asyncFeatures.promptsChangeConsumers(),
                asyncFeatures.loggingConsumers(),
                asyncFeatures.samplingHandler());
    }

    /**
     * Get the server capabilities that define the supported features and functionality.
     * @return The server capabilities
     */
    public ServerCapabilities getServerCapabilities() {
        return this.delegate.getServerCapabilities();
    }

    /**
     * Get the server implementation information.
     * @return The server implementation details
     */
    public Implementation getServerInfo() {
        return this.delegate.getServerInfo();
    }

    /**
     * Get the client capabilities that define the supported features and functionality.
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return this.delegate.getClientCapabilities();
    }

    /**
     * Get the client implementation information.
     * @return The client implementation details
     */
    public Implementation getClientInfo() {
        return this.delegate.getClientInfo();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    /**
     * Closes the client connection gracefully with a timeout.
     * 
     * @return true if the client closed successfully, false if it timed out
     */
    public boolean closeGracefully() {
        try {
            this.delegate.closeGracefully().get(DEFAULT_CLOSE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            logger.warn("Client didn't close within timeout of {} ms.", DEFAULT_CLOSE_TIMEOUT_MS, e);
            return false;
        }
        return true;
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
     * <ul>
     * <li>The protocol version the client supports</li>
     * <li>The client's capabilities</li>
     * <li>Client implementation information</li>
     * </ul>
     *
     * The server MUST respond with its own capabilities and information:
     * {@link ServerCapabilities}. <br/>
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
    public InitializeResult initialize() {
        try {
            return this.delegate.initialize().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize client", e);
        }
    }

    /**
     * Send a roots/list_changed notification.
     */
    public void rootsListChangedNotification() {
        try {
            this.delegate.rootsListChangedNotification().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send roots list changed notification", e);
        }
    }

    /**
     * Add a root dynamically.
     * 
     * @param root the root to add
     */
    public void addRoot(Root root) {
        try {
            this.delegate.addRoot(root).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add root", e);
        }
    }

    /**
     * Remove a root dynamically.
     * 
     * @param rootUri the URI of the root to remove
     */
    public void removeRoot(String rootUri) {
        try {
            this.delegate.removeRoot(rootUri).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove root", e);
        }
    }

    /**
     * Send a synchronous ping request.
     * 
     * @return the ping response
     */
    public Object ping() {
        try {
            return this.delegate.ping().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to ping server", e);
        }
    }

    // --------------------------
    // Tools
    // --------------------------
    /**
     * Calls a tool provided by the server. Tools enable servers to expose executable
     * functionality that can interact with external systems, perform computations, and
     * take actions in the real world.
     * @param callToolRequest The request containing: - name: The name of the tool to call
     * (must match a tool name from tools/list) - arguments: Arguments that conform to the
     * tool's input schema
     * @return The tool execution result containing: - content: List of content items
     * (text, images, or embedded resources) representing the tool's output - isError:
     * Boolean indicating if the execution failed (true) or succeeded (false/absent)
     */
    public CallToolResult callTool(CallToolRequest callToolRequest) {
        try {
            return this.delegate.callTool(callToolRequest).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call tool", e);
        }
    }

    /**
     * Retrieves the list of all tools provided by the server.
     * @return The list of tools result containing: - tools: List of available tools, each
     * with a name, description, and input schema - nextCursor: Optional cursor for
     * pagination if more tools are available
     */
    public ListToolsResult listTools() {
        try {
            return this.delegate.listTools().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list tools", e);
        }
    }

    /**
     * Retrieves a paginated list of tools provided by the server.
     * @param cursor Optional pagination cursor from a previous list request
     * @return The list of tools result containing: - tools: List of available tools, each
     * with a name, description, and input schema - nextCursor: Optional cursor for
     * pagination if more tools are available
     */
    public ListToolsResult listTools(String cursor) {
        try {
            return this.delegate.listTools(cursor).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list tools", e);
        }
    }

    // --------------------------
    // Resources
    // --------------------------

    /**
     * Send a resources/list request.
     * @param cursor the cursor
     * @return the list of resources result.
     */
    public ListResourcesResult listResources(String cursor) {
        try {
            return this.delegate.listResources(cursor).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list resources", e);
        }
    }

    /**
     * Send a resources/list request.
     * @return the list of resources result.
     */
    public ListResourcesResult listResources() {
        try {
            return this.delegate.listResources().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list resources", e);
        }
    }

    /**
     * Send a resources/read request.
     * @param resource the resource to read
     * @return the resource content.
     */
    public ReadResourceResult readResource(Resource resource) {
        try {
            return this.delegate.readResource(resource).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource", e);
        }
    }

    /**
     * Send a resources/read request.
     * @param readResourceRequest the read resource request.
     * @return the resource content.
     */
    public ReadResourceResult readResource(ReadResourceRequest readResourceRequest) {
        try {
            return this.delegate.readResource(readResourceRequest).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource", e);
        }
    }

    /**
     * Resource templates allow servers to expose parameterized resources using URI
     * templates. Arguments may be auto-completed through the completion API.
     *
     * Request a list of resource templates the server has.
     * @param cursor the cursor
     * @return the list of resource templates result.
     */
    public ListResourceTemplatesResult listResourceTemplates(String cursor) {
        try {
            return this.delegate.listResourceTemplates(cursor).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list resource templates", e);
        }
    }

    /**
     * Request a list of resource templates the server has.
     * @return the list of resource templates result.
     */
    public ListResourceTemplatesResult listResourceTemplates() {
        try {
            return this.delegate.listResourceTemplates().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list resource templates", e);
        }
    }

    /**
     * Subscriptions. The protocol supports optional subscriptions to resource changes.
     * Clients can subscribe to specific resources and receive notifications when they
     * change.
     *
     * Send a resources/subscribe request.
     * @param subscribeRequest the subscribe request contains the uri of the resource to
     * subscribe to.
     */
    public void subscribeResource(SubscribeRequest subscribeRequest) {
        try {
            this.delegate.subscribeResource(subscribeRequest).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to subscribe to resource", e);
        }
    }

    /**
     * Send a resources/unsubscribe request.
     * @param unsubscribeRequest the unsubscribe request contains the uri of the resource
     * to unsubscribe from.
     */
    public void unsubscribeResource(UnsubscribeRequest unsubscribeRequest) {
        try {
            this.delegate.unsubscribeResource(unsubscribeRequest).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to unsubscribe from resource", e);
        }
    }

    // --------------------------
    // Prompts
    // --------------------------
    /**
     * Retrieves the list of all prompts provided by the server.
     * 
     * @param cursor Optional pagination cursor from a previous list request
     * @return The list of prompts result
     */
    public ListPromptsResult listPrompts(String cursor) {
        try {
            return this.delegate.listPrompts(cursor).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list prompts", e);
        }
    }

    /**
     * Retrieves the list of all prompts provided by the server.
     * 
     * @return The list of prompts result
     */
    public ListPromptsResult listPrompts() {
        try {
            return this.delegate.listPrompts().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list prompts", e);
        }
    }

    /**
     * Retrieves a specific prompt by its ID.
     * 
     * @param getPromptRequest The request containing the ID of the prompt to retrieve
     * @return The prompt result
     */
    public GetPromptResult getPrompt(GetPromptRequest getPromptRequest) {
        try {
            return this.delegate.getPrompt(getPromptRequest).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get prompt", e);
        }
    }

    /**
     * Client can set the minimum logging level it wants to receive from the server.
     * @param loggingLevel the min logging level
     */
    public void setLoggingLevel(LoggingLevel loggingLevel) {
        try {
            this.delegate.setLoggingLevel(loggingLevel).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set logging level", e);
        }
    }
}

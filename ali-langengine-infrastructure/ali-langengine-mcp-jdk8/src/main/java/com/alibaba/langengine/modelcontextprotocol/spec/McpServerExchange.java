/*
 * Copyright 2024-2024 the original author or authors.
 */
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an exchange with a Model Context Protocol (MCP) client. The
 * exchange provides methods to interact with the client and query its capabilities.
 *
 * JDK 1.8 compatible version.
 *
 * @author Dariusz Jędrzejczyk
 */
public class McpServerExchange {

    private final McpServerSession session;

    private final ClientCapabilities clientCapabilities;

    private final Implementation clientInfo;

    private static final TypeReference<CreateMessageResult> CREATE_MESSAGE_RESULT_TYPE_REF =
        new TypeReference<CreateMessageResult>() {};

    private static final TypeReference<ListRootsResult> LIST_ROOTS_RESULT_TYPE_REF =
        new TypeReference<ListRootsResult>() {};

    /**
     * Create a new exchange with the client.
     * @param session The server session representing a 1-1 interaction.
     * @param clientCapabilities The client capabilities that define the supported
     * features and functionality.
     * @param clientInfo The client implementation information.
     */
    public McpServerExchange(McpServerSession session, ClientCapabilities clientCapabilities,
            Implementation clientInfo) {
        this.session = session;
        this.clientCapabilities = clientCapabilities;
        this.clientInfo = clientInfo;
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
     * Create a new message using the sampling capabilities of the client. The Model
     * Context Protocol (MCP) provides a standardized way for servers to request LLM
     * sampling ("completions" or "generations") from language models via clients. This
     * flow allows clients to maintain control over model access, selection, and
     * permissions while enabling servers to leverage AI capabilities—with no server API
     * keys necessary. Servers can request text or image-based interactions and optionally
     * include context from MCP servers in their prompts.
     * @param createMessageRequest The request to create a new message
     * @return A CompletableFuture that completes when the message has been created
     * @see CreateMessageRequest
     * @see CreateMessageResult
     * @see <a href=
     * "https://spec.modelcontextprotocol.io/specification/client/sampling/">Sampling
     * Specification</a>
     */
    public CompletableFuture<CreateMessageResult> createMessage(CreateMessageRequest createMessageRequest) {
        if (this.clientCapabilities == null) {
            CompletableFuture<CreateMessageResult> future = new CompletableFuture<>();
            future.completeExceptionally(new McpError("Client must be initialized. Call the initialize method first!"));
            return future;
        }
        if (this.clientCapabilities.sampling() == null) {
            CompletableFuture<CreateMessageResult> future = new CompletableFuture<>();
            future.completeExceptionally(new McpError("Client must be configured with sampling capabilities"));
            return future;
        }
        return this.session.sendRequest(McpSchema.METHOD_SAMPLING_CREATE_MESSAGE, createMessageRequest,
                CREATE_MESSAGE_RESULT_TYPE_REF);
    }

    /**
     * Retrieves the list of all roots provided by the client.
     * @return A CompletableFuture that emits the list of roots result.
     */
    public CompletableFuture<ListRootsResult> listRoots() {
        return this.listRoots(null);
    }

    /**
     * Retrieves a paginated list of roots provided by the client.
     * @param cursor Optional pagination cursor from a previous list request
     * @return A CompletableFuture that emits the list of roots result
     */
    public CompletableFuture<ListRootsResult> listRoots(String cursor) {
        return this.session.sendRequest(McpSchema.METHOD_ROOTS_LIST, new PaginatedRequest(cursor),
                LIST_ROOTS_RESULT_TYPE_REF);
    }
}

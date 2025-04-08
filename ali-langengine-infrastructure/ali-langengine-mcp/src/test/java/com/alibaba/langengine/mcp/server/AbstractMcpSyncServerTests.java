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

package com.alibaba.langengine.mcp.server;

import com.alibaba.langengine.mcp.spec.*;
import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceResult;
import com.alibaba.langengine.mcp.spec.schema.prompts.*;
import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolResult;
import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test suite for the {@link McpSyncServer} that can be used with different
 *
 * @author Christian Tzolov
 */
public abstract class AbstractMcpSyncServerTests {

	private static final String TEST_TOOL_NAME = "test-tool";

	private static final String TEST_RESOURCE_URI = "test://resource";

	private static final String TEST_PROMPT_NAME = "test-prompt";

	abstract protected ServerMcpTransport createMcpTransport();

	protected void onStart() {
	}

	protected void onClose() {
	}

	@BeforeEach
	void setUp() {
		// onStart();
	}

	@AfterEach
	void tearDown() {
		onClose();
	}

	// ---------------------------------------
	// Server Lifecycle Tests
	// ---------------------------------------

	@Test
	void testConstructorWithInvalidArguments() {
		assertThatThrownBy(() -> McpServer.using(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Transport must not be null");

		assertThatThrownBy(() -> McpServer.using(createMcpTransport()).serverInfo((Implementation) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Server info must not be null");
	}

	@Test
	void testGracefulShutdown() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testImmediateClose() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatCode(() -> mcpSyncServer.close()).doesNotThrowAnyException();
	}

	@Test
	void testGetAsyncServer() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThat(mcpSyncServer.getAsyncServer()).isNotNull();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	// ---------------------------------------
	// Tools Tests
	// ---------------------------------------

	String emptyJsonSchema = "{\n" +
			"\t\t\t\t\"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
			"\t\t\t\t\"type\": \"object\",\n" +
			"\t\t\t\t\"properties\": {}\n" +
			"\t\t\t}";

	@Test
	void testAddTool() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().tools(true).build())
			.sync();

		Tool newTool = new Tool("new-tool", "New test tool", emptyJsonSchema);
		assertThatCode(() -> mcpSyncServer
			.addTool(new ToolRegistration(newTool, args -> new CallToolResult(new ArrayList<>(), false))))
			.doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testAddDuplicateTool() {
		Tool duplicateTool = new Tool(TEST_TOOL_NAME, "Duplicate tool", emptyJsonSchema);

		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().tools(true).build())
			.tool(duplicateTool, args -> new CallToolResult(new ArrayList<>(), false))
			.sync();

		assertThatThrownBy(() -> mcpSyncServer
			.addTool(new ToolRegistration(duplicateTool, args -> new CallToolResult(new ArrayList<>(), false))))
			.isInstanceOf(McpError.class)
			.hasMessage("Tool with name '" + TEST_TOOL_NAME + "' already exists");

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testRemoveTool() {
		Tool tool = new Tool(TEST_TOOL_NAME, "Test tool", emptyJsonSchema);

		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().tools(true).build())
			.tool(tool, args -> new CallToolResult(new ArrayList<>(), false))
			.sync();

		assertThatCode(() -> mcpSyncServer.removeTool(TEST_TOOL_NAME)).doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testRemoveNonexistentTool() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().tools(true).build())
			.sync();

		assertThatThrownBy(() -> mcpSyncServer.removeTool("nonexistent-tool")).isInstanceOf(McpError.class)
			.hasMessage("Tool with name 'nonexistent-tool' not found");

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testNotifyToolsListChanged() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatCode(() -> mcpSyncServer.notifyToolsListChanged()).doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	// ---------------------------------------
	// Resources Tests
	// ---------------------------------------

	@Test
	void testNotifyResourcesListChanged() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatCode(() -> mcpSyncServer.notifyResourcesListChanged()).doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testAddResource() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().resources(true, false).build())
			.sync();

		Resource resource = new Resource(TEST_RESOURCE_URI, "Test Resource", "text/plain", "Test resource description",
				null);
		ResourceRegistration registration = new ResourceRegistration(resource,
				req -> new ReadResourceResult(new ArrayList<>()));

		assertThatCode(() -> mcpSyncServer.addResource(registration)).doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testAddResourceWithNullRegistration() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().resources(true, false).build())
			.sync();

		assertThatThrownBy(() -> mcpSyncServer.addResource(null)).isInstanceOf(McpError.class)
			.hasMessage("Resource must not be null");

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testAddResourceWithoutCapability() {
		McpSyncServer serverWithoutResources = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		Resource resource = new Resource(TEST_RESOURCE_URI, "Test Resource", "text/plain", "Test resource description",
				null);
		ResourceRegistration registration = new ResourceRegistration(resource,
				req -> new ReadResourceResult(new ArrayList<>()));

		assertThatThrownBy(() -> serverWithoutResources.addResource(registration)).isInstanceOf(McpError.class)
			.hasMessage("Server must be configured with resource capabilities");
	}

	@Test
	void testRemoveResourceWithoutCapability() {
		McpSyncServer serverWithoutResources = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatThrownBy(() -> serverWithoutResources.removeResource(TEST_RESOURCE_URI)).isInstanceOf(McpError.class)
			.hasMessage("Server must be configured with resource capabilities");
	}

	// ---------------------------------------
	// Prompts Tests
	// ---------------------------------------

	@Test
	void testNotifyPromptsListChanged() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatCode(() -> mcpSyncServer.notifyPromptsListChanged()).doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testAddPromptWithNullRegistration() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().prompts(false).build())
			.sync();

		assertThatThrownBy(() -> mcpSyncServer.addPrompt(null)).isInstanceOf(McpError.class)
			.hasMessage("Prompt registration must not be null");
	}

	@Test
	void testAddPromptWithoutCapability() {
		McpSyncServer serverWithoutPrompts = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		Prompt prompt = new Prompt(TEST_PROMPT_NAME, "Test Prompt", new ArrayList<>());
		PromptRegistration registration = new PromptRegistration(prompt, req -> new GetPromptResult(
				"Test prompt description",
                Collections.singletonList(new PromptMessage(Role.assistant, new TextContent("Test content")))));

		assertThatThrownBy(() -> serverWithoutPrompts.addPrompt(registration)).isInstanceOf(McpError.class)
			.hasMessage("Server must be configured with prompt capabilities");
	}

	@Test
	void testRemovePromptWithoutCapability() {
		McpSyncServer serverWithoutPrompts = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThatThrownBy(() -> serverWithoutPrompts.removePrompt(TEST_PROMPT_NAME)).isInstanceOf(McpError.class)
			.hasMessage("Server must be configured with prompt capabilities");
	}

	@Test
	void testRemovePrompt() {
		Prompt prompt = new Prompt(TEST_PROMPT_NAME, "Test Prompt", new ArrayList<>());
		PromptRegistration registration = new PromptRegistration(prompt, req -> new GetPromptResult(
				"Test prompt description",
                Collections.singletonList(new PromptMessage(Role.assistant, new TextContent("Test content")))));

		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().prompts(true).build())
			.prompts(registration)
			.sync();

		assertThatCode(() -> mcpSyncServer.removePrompt(TEST_PROMPT_NAME)).doesNotThrowAnyException();

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	@Test
	void testRemoveNonexistentPrompt() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().prompts(true).build())
			.sync();

		assertThatThrownBy(() -> mcpSyncServer.removePrompt("nonexistent-prompt")).isInstanceOf(McpError.class)
			.hasMessage("Prompt with name 'nonexistent-prompt' not found");

		assertThatCode(() -> mcpSyncServer.closeGracefully()).doesNotThrowAnyException();
	}

	// ---------------------------------------
	// Roots Tests
	// ---------------------------------------

	@Test
	void testRootsChangeConsumers() {
		// Test with single consumer
		Root[] rootsReceived = new Root[1];
		boolean[] consumerCalled = new boolean[1];

		McpSyncServer singleConsumerServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.rootsChangeConsumers(Collections.singletonList(roots -> {
                consumerCalled[0] = true;
                if (!roots.isEmpty()) {
                    rootsReceived[0] = roots.get(0);
                }
            }))
			.sync();

		assertThat(singleConsumerServer).isNotNull();
		assertThatCode(() -> singleConsumerServer.closeGracefully()).doesNotThrowAnyException();
		onClose();

		// Test with multiple consumers
		boolean[] consumer1Called = new boolean[1];
		boolean[] consumer2Called = new boolean[1];
		List[] rootsContent = new List[1];

		McpSyncServer multipleConsumersServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.rootsChangeConsumers(Arrays.asList(roots -> {
				consumer1Called[0] = true;
				rootsContent[0] = roots;
			}, roots -> consumer2Called[0] = true))
			.sync();

		assertThat(multipleConsumersServer).isNotNull();
		assertThatCode(() -> multipleConsumersServer.closeGracefully()).doesNotThrowAnyException();
		onClose();

		// Test error handling
		McpSyncServer errorHandlingServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.rootsChangeConsumers(Collections.singletonList(roots -> {
                throw new RuntimeException("Test error");
            }))
			.sync();

		assertThat(errorHandlingServer).isNotNull();
		assertThatCode(() -> errorHandlingServer.closeGracefully()).doesNotThrowAnyException();
		onClose();

		// Test without consumers
		McpSyncServer noConsumersServer = McpServer.using(createMcpTransport()).serverInfo("test-server", "1.0.0").sync();

		assertThat(noConsumersServer).isNotNull();
		assertThatCode(() -> noConsumersServer.closeGracefully()).doesNotThrowAnyException();
	}

	// ---------------------------------------
	// Logging Tests
	// ---------------------------------------

	@Test
	void testLoggingLevels() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().logging().build())
			.sync();

		// Test all logging levels
		for (LoggingLevel level : LoggingLevel.values()) {
			LoggingMessageNotification notification = LoggingMessageNotification.builder()
				.level(level)
				.logger("test-logger")
				.data("Test message with level " + level)
				.build();

			assertThatCode(() -> mcpSyncServer.loggingNotification(notification)).doesNotThrowAnyException();
		}
	}

	@Test
	void testLoggingWithoutCapability() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().build()) // No logging capability
			.sync();

		LoggingMessageNotification notification = LoggingMessageNotification.builder()
			.level(LoggingLevel.INFO)
			.logger("test-logger")
			.data("Test log message")
			.build();

		assertThatCode(() -> mcpSyncServer.loggingNotification(notification)).doesNotThrowAnyException();
	}

	@Test
	void testLoggingWithNullNotification() {
		McpSyncServer mcpSyncServer = McpServer.using(createMcpTransport())
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().logging().build())
			.sync();

		assertThatThrownBy(() -> mcpSyncServer.loggingNotification(null)).isInstanceOf(McpError.class);
	}

}

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

import com.alibaba.langengine.mcp.spec.ClientCapabilities;
import com.alibaba.langengine.mcp.spec.ClientMcpTransport;
import com.alibaba.langengine.mcp.spec.LoggingLevel;
import com.alibaba.langengine.mcp.spec.Root;
import com.alibaba.langengine.mcp.spec.schema.*;
import com.alibaba.langengine.mcp.spec.schema.prompts.TextContent;
import com.alibaba.langengine.mcp.spec.schema.resources.ListResourceTemplatesResult;
import com.alibaba.langengine.mcp.spec.schema.resources.ListResourcesResult;
import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceResult;
import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolResult;
import com.alibaba.langengine.mcp.spec.schema.tools.ListToolsResult;
import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MCP Client Session functionality.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public abstract class AbstractMcpSyncClientTests {

	private McpSyncClient mcpSyncClient;

	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private static final String TEST_MESSAGE = "Hello MCP Spring AI!";

	protected ClientMcpTransport mcpTransport;

	abstract protected ClientMcpTransport createMcpTransport();

	abstract protected void onStart();

	abstract protected void onClose();

	@BeforeEach
	void setUp() {
		onStart();
		this.mcpTransport = createMcpTransport();

		assertThatCode(() -> {
			mcpSyncClient = McpClient.using(mcpTransport)
				.requestTimeout(TIMEOUT)
				.capabilities(ClientCapabilities.builder().roots(true).build())
				.sync();
			mcpSyncClient.initialize();
		}).doesNotThrowAnyException();
	}

	@AfterEach
	void tearDown() {
		if (mcpSyncClient != null) {
			assertThatCode(() -> mcpSyncClient.close()).doesNotThrowAnyException();
		}
		onClose();
	}

	@Test
	void testConstructorWithInvalidArguments() {
		assertThatThrownBy(() -> McpClient.using(null).sync()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Transport must not be null");

		assertThatThrownBy(() -> McpClient.using(mcpTransport).requestTimeout(null).sync())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Request timeout must not be null");
	}

	@Test
	void testListTools() {
		ListToolsResult tools = mcpSyncClient.listTools(null);

		assertThat(tools).isNotNull().satisfies(result -> {
			assertThat(result.getTools()).isNotNull().isNotEmpty();

			Tool firstTool = result.getTools().get(0);
			assertThat(firstTool.getName()).isNotNull();
			assertThat(firstTool.getDescription()).isNotNull();
		});
	}

	@Test
	void testCallTools() {
		Map<String, Object> map = new HashMap<>();
		map.put("a", 3);
		map.put("b", 4);
		CallToolResult toolResult = mcpSyncClient.callTool(new CallToolRequest("add", map));

		assertThat(toolResult).isNotNull().satisfies(result -> {

//			TextContent content = (TextContent) result.getContent().get(0);
			TextContent content = (TextContent) result.getContent();


			assertThat(content).isNotNull();
			assertThat(content.getText()).isNotNull();
			assertThat(content.getText()).contains("7");
		});
	}

	@Test
	void testPing() {
		assertThatCode(() -> mcpSyncClient.ping()).doesNotThrowAnyException();
	}

	@Test
	void testCallTool() {
		CallToolRequest callToolRequest = new CallToolRequest("echo", Collections.singletonMap("message", TEST_MESSAGE));

		CallToolResult callToolResult = mcpSyncClient.callTool(callToolRequest);

		assertThat(callToolResult).isNotNull().satisfies(result -> {
			assertThat(result.getContent()).isNotNull();
			assertThat(result.getIsError()).isNull();
		});
	}

	@Test
	void testCallToolWithInvalidTool() {
		CallToolRequest invalidRequest = new CallToolRequest("nonexistent_tool", Collections.singletonMap("message", TEST_MESSAGE));

		assertThatThrownBy(() -> mcpSyncClient.callTool(invalidRequest)).isInstanceOf(Exception.class);
	}

	@Test
	void testRootsListChanged() {
		assertThatCode(() -> mcpSyncClient.rootsListChangedNotification()).doesNotThrowAnyException();
	}

	@Test
	void testListResources() {
		ListResourcesResult resources = mcpSyncClient.listResources(null);

		assertThat(resources).isNotNull().satisfies(result -> {
			assertThat(result.getResources()).isNotNull();

			if (!result.getResources().isEmpty()) {
				Resource firstResource = result.getResources().get(0);
				assertThat(firstResource.getUri()).isNotNull();
				assertThat(firstResource.getName()).isNotNull();
			}
		});
	}

	@Test
	void testClientSessionState() {
		assertThat(mcpSyncClient).isNotNull();
	}

	@Test
	void testInitializeWithRootsListProviders() {
		ClientMcpTransport transport = createMcpTransport();

		McpSyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.roots(new Root("file:///test/path", "test-root"))
			.sync();

		assertThatCode(() -> {
			client.initialize();
			client.close();
		}).doesNotThrowAnyException();
	}

	@Test
	void testAddRoot() {
		Root newRoot = new Root("file:///new/test/path", "new-test-root");
		assertThatCode(() -> mcpSyncClient.addRoot(newRoot)).doesNotThrowAnyException();
	}

	@Test
	void testAddRootWithNullValue() {
		assertThatThrownBy(() -> mcpSyncClient.addRoot(null)).hasMessageContaining("Root must not be null");
	}

	@Test
	void testRemoveRoot() {
		Root root = new Root("file:///test/path/to/remove", "root-to-remove");
		assertThatCode(() -> {
			mcpSyncClient.addRoot(root);
			mcpSyncClient.removeRoot(root.getUri());
		}).doesNotThrowAnyException();
	}

	@Test
	void testRemoveNonExistentRoot() {
		assertThatThrownBy(() -> mcpSyncClient.removeRoot("nonexistent-uri"))
			.hasMessageContaining("Root with uri 'nonexistent-uri' not found");
	}

	@Test
	void testReadResource() {
		ListResourcesResult resources = mcpSyncClient.listResources(null);

		if (!resources.getResources().isEmpty()) {
			Resource firstResource = resources.getResources().get(0);
			ReadResourceResult result = mcpSyncClient.readResource(firstResource);

			assertThat(result).isNotNull();
			assertThat(result.getContents()).isNotNull();
		}
	}

	@Test
	void testListResourceTemplates() {
		ListResourceTemplatesResult result = mcpSyncClient.listResourceTemplates(null);

		assertThat(result).isNotNull();
		assertThat(result.getResourceTemplates()).isNotNull();
	}

	// @Test
	void testResourceSubscription() {
		ListResourcesResult resources = mcpSyncClient.listResources(null);

		if (!resources.getResources().isEmpty()) {
			Resource firstResource = resources.getResources().get(0);

			// Test subscribe
			assertThatCode(() -> mcpSyncClient.subscribeResource(new SubscribeRequest(firstResource.getUri())))
				.doesNotThrowAnyException();

			// Test unsubscribe
			assertThatCode(() -> mcpSyncClient.unsubscribeResource(new UnsubscribeRequest(firstResource.getUri())))
				.doesNotThrowAnyException();
		}
	}

	@Test
	void testNotificationHandlers() {
		AtomicBoolean toolsNotificationReceived = new AtomicBoolean(false);
		AtomicBoolean resourcesNotificationReceived = new AtomicBoolean(false);
		AtomicBoolean promptsNotificationReceived = new AtomicBoolean(false);

		ClientMcpTransport transport = createMcpTransport();
		McpSyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.toolsChangeConsumer(tools -> toolsNotificationReceived.set(true))
			.resourcesChangeConsumer(resources -> resourcesNotificationReceived.set(true))
			.promptsChangeConsumer(prompts -> promptsNotificationReceived.set(true))
			.sync();

		assertThatCode(() -> {
			client.initialize();
			// Trigger notifications
			client.sendResourcesListChanged();
			client.promptListChangedNotification();
			client.close();
		}).doesNotThrowAnyException();
	}

	// ---------------------------------------
	// Logging Tests
	// ---------------------------------------

	@Test
	void testLoggingLevels() {
		// Test all logging levels
		for (LoggingLevel level : LoggingLevel.values()) {
			assertThatCode(() -> mcpSyncClient.setLoggingLevel(level)).doesNotThrowAnyException();
		}
	}

	@Test
	void testLoggingConsumer() {
		AtomicBoolean logReceived = new AtomicBoolean(false);
		ClientMcpTransport transport = createMcpTransport();

		McpSyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.loggingConsumer(notification -> logReceived.set(true))
			.sync();

		assertThatCode(() -> {
			client.initialize();
			client.close();
		}).doesNotThrowAnyException();
	}

	@Test
	void testLoggingWithNullNotification() {
		assertThatThrownBy(() -> mcpSyncClient.setLoggingLevel(null))
			.hasMessageContaining("Logging level must not be null");
	}

}

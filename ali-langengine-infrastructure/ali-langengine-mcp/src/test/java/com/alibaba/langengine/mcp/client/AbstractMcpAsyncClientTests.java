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
import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptRequest;
import com.alibaba.langengine.mcp.spec.schema.prompts.Prompt;
import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for the {@link McpAsyncClient} that can be used with different
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public abstract class AbstractMcpAsyncClientTests {

	private McpAsyncClient mcpAsyncClient;

	protected ClientMcpTransport mcpTransport;

	private static final Duration TIMEOUT = Duration.ofSeconds(20);

	private static final String ECHO_TEST_MESSAGE = "Hello MCP Spring AI!";

	abstract protected ClientMcpTransport createMcpTransport();

	protected void onStart() {
	}

	protected void onClose() {
	}

	@BeforeEach
	void setUp() {
		onStart();
		this.mcpTransport = createMcpTransport();

		assertThatCode(() -> {
			mcpAsyncClient = McpClient.using(mcpTransport)
				.requestTimeout(TIMEOUT)
				.capabilities(ClientCapabilities.builder().roots(true).build())
				.async();
			mcpAsyncClient.initialize().block(Duration.ofSeconds(10));
		}).doesNotThrowAnyException();
	}

	@AfterEach
	void tearDown() {
		if (mcpAsyncClient != null) {
			assertThatCode(() -> mcpAsyncClient.closeGracefully().block(Duration.ofSeconds(10)))
				.doesNotThrowAnyException();
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
		StepVerifier.create(mcpAsyncClient.listTools(null)).consumeNextWith(result -> {
			assertThat(result.getTools()).isNotNull().isNotEmpty();

			Tool firstTool = result.getTools().get(0);
			assertThat(firstTool.getName()).isNotNull();
			assertThat(firstTool.getDescription()).isNotNull();
		}).verifyComplete();
	}

	@Test
	void testPing() {
		assertThatCode(() -> mcpAsyncClient.ping().block()).doesNotThrowAnyException();
	}

	@Test
	void testCallTool() {
		CallToolRequest callToolRequest = new CallToolRequest("echo", Collections.singletonMap("message", ECHO_TEST_MESSAGE));

		StepVerifier.create(mcpAsyncClient.callTool(callToolRequest)).consumeNextWith(callToolResult -> {
			assertThat(callToolResult).isNotNull().satisfies(result -> {
				assertThat(result.getContent()).isNotNull();
				assertThat(result.getIsError()).isNull();
			});
		}).verifyComplete();
	}

	@Test
	void testCallToolWithInvalidTool() {
		CallToolRequest invalidRequest = new CallToolRequest("nonexistent_tool", Collections.singletonMap("message", ECHO_TEST_MESSAGE));

		assertThatThrownBy(() -> mcpAsyncClient.callTool(invalidRequest).block()).isInstanceOf(Exception.class);
	}

	@Test
	void testListResources() {
		StepVerifier.create(mcpAsyncClient.listResources(null)).consumeNextWith(resources -> {
			assertThat(resources).isNotNull().satisfies(result -> {
				assertThat(result.getResources()).isNotNull();

				if (!result.getResources().isEmpty()) {
					Resource firstResource = result.getResources().get(0);
					assertThat(firstResource.getUri()).isNotNull();
					assertThat(firstResource.getName()).isNotNull();
				}
			});
		}).verifyComplete();
	}

	@Test
	void testMcpAsyncClientState() {
		assertThat(mcpAsyncClient).isNotNull();
	}

	@Test
	void testListPrompts() {
		StepVerifier.create(mcpAsyncClient.listPrompts(null)).consumeNextWith(prompts -> {
			assertThat(prompts).isNotNull().satisfies(result -> {
				assertThat(result.getPrompts()).isNotNull();

				if (!result.getPrompts().isEmpty()) {
					Prompt firstPrompt = result.getPrompts().get(0);
					assertThat(firstPrompt.getName()).isNotNull();
					assertThat(firstPrompt.getDescription()).isNotNull();
				}
			});
		}).verifyComplete();
	}

	@Test
	void testGetPrompt() {
		StepVerifier.create(mcpAsyncClient.getPrompt(new GetPromptRequest("simple_prompt", Collections.emptyMap())))
			.consumeNextWith(prompt -> {
				assertThat(prompt).isNotNull().satisfies(result -> {
					assertThat(result.getMessages()).isNotEmpty();
					assertThat(result.getMessages()).hasSize(1);
				});
			})
			.verifyComplete();
	}

	@Test
	void testRootsListChanged() {
		assertThatCode(() -> mcpAsyncClient.rootsListChangedNotification().block()).doesNotThrowAnyException();
	}

	@Test
	void testInitializeWithRootsListProviders() {
		ClientMcpTransport transport = createMcpTransport();

		McpAsyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.roots(new Root("file:///test/path", "test-root"))
			.async();

		assertThatCode(() -> client.initialize().block(Duration.ofSeconds(10))).doesNotThrowAnyException();

		assertThatCode(() -> client.closeGracefully().block(Duration.ofSeconds(10))).doesNotThrowAnyException();
	}

	@Test
	void testAddRoot() {
		Root newRoot = new Root("file:///new/test/path", "new-test-root");
		assertThatCode(() -> mcpAsyncClient.addRoot(newRoot).block()).doesNotThrowAnyException();
	}

	@Test
	void testAddRootWithNullValue() {
		assertThatThrownBy(() -> mcpAsyncClient.addRoot(null).block()).hasMessageContaining("Root must not be null");
	}

	@Test
	void testRemoveRoot() {
		Root root = new Root("file:///test/path/to/remove", "root-to-remove");
		assertThatCode(() -> {
			mcpAsyncClient.addRoot(root).block();
			mcpAsyncClient.removeRoot(root.getUri()).block();
		}).doesNotThrowAnyException();
	}

	@Test
	void testRemoveNonExistentRoot() {
		assertThatThrownBy(() -> mcpAsyncClient.removeRoot("nonexistent-uri").block())
			.hasMessageContaining("Root with uri 'nonexistent-uri' not found");
	}

	@Test
	@Disabled
	void testReadResource() {
		StepVerifier.create(mcpAsyncClient.listResources()).consumeNextWith(resources -> {
			if (!resources.getResources().isEmpty()) {
				Resource firstResource = resources.getResources().get(0);
				StepVerifier.create(mcpAsyncClient.readResource(firstResource)).consumeNextWith(result -> {
					assertThat(result).isNotNull();
					assertThat(result.getContents()).isNotNull();
				}).verifyComplete();
			}
		}).verifyComplete();
	}

	@Test
	void testListResourceTemplates() {
		StepVerifier.create(mcpAsyncClient.listResourceTemplates()).consumeNextWith(result -> {
			assertThat(result).isNotNull();
			assertThat(result.getResourceTemplates()).isNotNull();
		}).verifyComplete();
	}

	// @Test
	void testResourceSubscription() {
		StepVerifier.create(mcpAsyncClient.listResources()).consumeNextWith(resources -> {
			if (!resources.getResources().isEmpty()) {
				Resource firstResource = resources.getResources().get(0);

				// Test subscribe
				StepVerifier.create(mcpAsyncClient.subscribeResource(new SubscribeRequest(firstResource.getUri())))
					.verifyComplete();

				// Test unsubscribe
				StepVerifier.create(mcpAsyncClient.unsubscribeResource(new UnsubscribeRequest(firstResource.getUri())))
					.verifyComplete();
			}
		}).verifyComplete();
	}

	@Test
	void testNotificationHandlers() {
		AtomicBoolean toolsNotificationReceived = new AtomicBoolean(false);
		AtomicBoolean resourcesNotificationReceived = new AtomicBoolean(false);
		AtomicBoolean promptsNotificationReceived = new AtomicBoolean(false);

		ClientMcpTransport transport = createMcpTransport();
		McpAsyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.toolsChangeConsumer(tools -> toolsNotificationReceived.set(true))
			.resourcesChangeConsumer(resources -> resourcesNotificationReceived.set(true))
			.promptsChangeConsumer(prompts -> promptsNotificationReceived.set(true))
			.async();

		assertThatCode(() -> {
			client.initialize().block();
			// Trigger notifications
			client.sendResourcesListChanged().block();
			client.promptListChangedNotification().block();
			client.closeGracefully().block();
		}).doesNotThrowAnyException();
	}

	@Test
	void testInitializeWithSamplingCapability() {
		ClientMcpTransport transport = createMcpTransport();

		ClientCapabilities capabilities = ClientCapabilities.builder().sampling().build();

		McpAsyncClient client = McpClient.using(transport).requestTimeout(TIMEOUT).capabilities(capabilities).sampling(request -> {
			return CreateMessageResult.builder().message("test").model("test-model").build();
		}).async();

		assertThatCode(() -> {
			client.initialize().block(Duration.ofSeconds(10));
			client.closeGracefully().block(Duration.ofSeconds(10));
		}).doesNotThrowAnyException();
	}

	@Test
	void testInitializeWithAllCapabilities() {
		ClientMcpTransport transport = createMcpTransport();

		ClientCapabilities capabilities = ClientCapabilities.builder()
			.experimental(Collections.singletonMap("feature", "test"))
			.roots(true)
			.sampling()
			.build();

		Function<CreateMessageRequest, CreateMessageResult> samplingHandler = request -> {
			return CreateMessageResult.builder().message("test").model("test-model").build();
		};
		McpAsyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.capabilities(capabilities)
			.sampling(samplingHandler)
			.async();

		assertThatCode(() -> {
			InitializeResult result = client.initialize().block(Duration.ofSeconds(10));
			assertThat(result).isNotNull();
			assertThat(result.getCapabilities()).isNotNull();
			client.closeGracefully().block(Duration.ofSeconds(10));
		}).doesNotThrowAnyException();
	}

	// ---------------------------------------
	// Logging Tests
	// ---------------------------------------

	@Test
	void testLoggingLevels() {
		// Test all logging levels
		for (LoggingLevel level : LoggingLevel.values()) {
			StepVerifier.create(mcpAsyncClient.setLoggingLevel(level)).verifyComplete();
		}
	}

	@Test
	void testLoggingConsumer() {
		AtomicBoolean logReceived = new AtomicBoolean(false);
		ClientMcpTransport transport = createMcpTransport();

		McpAsyncClient client = McpClient.using(transport)
			.requestTimeout(TIMEOUT)
			.loggingConsumer(notification -> logReceived.set(true))
			.async();

		assertThatCode(() -> {
			client.initialize().block(Duration.ofSeconds(10));
			client.closeGracefully().block(Duration.ofSeconds(10));
		}).doesNotThrowAnyException();
	}

	@Test
	void testLoggingWithNullNotification() {
		assertThatThrownBy(() -> mcpAsyncClient.setLoggingLevel(null).block())
			.hasMessageContaining("Logging level must not be null");
	}

}

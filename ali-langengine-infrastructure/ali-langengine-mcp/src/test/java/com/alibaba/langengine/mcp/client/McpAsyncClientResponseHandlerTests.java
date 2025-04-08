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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.mcp.MockMcpTransport;
import com.alibaba.langengine.mcp.spec.*;
import com.alibaba.langengine.mcp.spec.schema.*;
import com.alibaba.langengine.mcp.spec.schema.prompts.*;
import com.alibaba.langengine.mcp.spec.schema.resources.ListResourcesResult;
import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
import com.alibaba.langengine.mcp.spec.schema.tools.ListToolsResult;
import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class McpAsyncClientResponseHandlerTests {

	@Test
	void testToolsChangeNotificationHandling() {
		MockMcpTransport transport = new MockMcpTransport();

		// Create a list to store received tools for verification
		List<Tool> receivedTools = new ArrayList<>();

		// Create a consumer that will be called when tools change
		Consumer<List<Tool>> toolsChangeConsumer = tools -> {
			receivedTools.addAll(tools);
		};

		// Create client with tools change consumer
		McpAsyncClient asyncMcpClient = McpClient.using(transport).toolsChangeConsumer(toolsChangeConsumer).async();

		// Create a mock tools list that the server will return
		Map<String, Object> inputSchema = new HashMap<>();
		inputSchema.put("type", "object");
		inputSchema.put("properties", new HashMap<>());
		inputSchema.put("required", new ArrayList<>());
		Tool mockTool = new Tool("test-tool", "Test Tool Description",
				JSON.toJSONString(inputSchema));
		ListToolsResult mockToolsResult = new ListToolsResult(Collections.singletonList(mockTool), null);

		// Simulate server sending tools/list_changed notification
		JSONRPCNotification notification = new JSONRPCNotification(Schema.JSONRPC_VERSION,
				MethodDefined.NotificationsToolsListChanged.getValue(), null);
		transport.simulateIncomingMessage(notification);

		// Simulate server response to tools/list request
		JSONRPCRequest toolsListRequest = transport.getLastSentMessageAsRequest();
		assertThat(toolsListRequest.getMethod()).isEqualTo(MethodDefined.ToolsList.getValue());

		JSONRPCResponse toolsListResponse = new JSONRPCResponse(Schema.JSONRPC_VERSION,
				toolsListRequest.getId(), mockToolsResult, null);
		transport.simulateIncomingMessage(toolsListResponse);

		// Verify the consumer received the expected tools
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			assertThat(receivedTools).hasSize(1);
			assertThat(receivedTools.get(0).getName()).isEqualTo("test-tool");
			assertThat(receivedTools.get(0).getDescription()).isEqualTo("Test Tool Description");
		});

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testRootsListRequestHandling() {
		MockMcpTransport transport = new MockMcpTransport();

		McpAsyncClient asyncMcpClient = McpClient.using(transport)
			.roots(new Root("file:///test/path", "test-root"))
			.async();

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(Schema.JSONRPC_VERSION,
				MethodDefined.RootsList.getValue(), "test-id", null);
		transport.simulateIncomingMessage(request);

		// Verify response
		JSONRPCMessage sentMessage = transport.getLastSentMessage();
		assertThat(sentMessage).isInstanceOf(JSONRPCResponse.class);

		JSONRPCResponse response = (JSONRPCResponse) sentMessage;
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getResult())
			.isEqualTo(new ListRootsResult(Collections.singletonList(new Root("file:///test/path", "test-root"))));
		assertThat(response.getError()).isNull();

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testResourcesChangeNotificationHandling() {
		MockMcpTransport transport = new MockMcpTransport();

		// Create a list to store received resources for verification
		List<Resource> receivedResources = new ArrayList<>();

		// Create a consumer that will be called when resources change
		Consumer<List<Resource>> resourcesChangeConsumer = resources -> {
			receivedResources.addAll(resources);
		};

		// Create client with resources change consumer
		McpAsyncClient asyncMcpClient = McpClient.using(transport)
			.resourcesChangeConsumer(resourcesChangeConsumer)
			.async();

		// Create a mock resources list that the server will return
		Resource mockResource = new Resource("test://resource", "Test Resource", "A test resource",
				"text/plain", null);
		ListResourcesResult mockResourcesResult = new ListResourcesResult(Collections.singletonList(mockResource),
				null);

		// Simulate server sending resources/list_changed notification
		JSONRPCNotification notification = new JSONRPCNotification(Schema.JSONRPC_VERSION,
				MethodDefined.NotificationsResourcesListChanged.getValue(), null);
		transport.simulateIncomingMessage(notification);

		// Simulate server response to resources/list request
		JSONRPCRequest resourcesListRequest = transport.getLastSentMessageAsRequest();
		assertThat(resourcesListRequest.getMethod()).isEqualTo(MethodDefined.ResourcesList.getValue());

		JSONRPCResponse resourcesListResponse = new JSONRPCResponse(Schema.JSONRPC_VERSION,
				resourcesListRequest.getId(), mockResourcesResult, null);
		transport.simulateIncomingMessage(resourcesListResponse);

		// Verify the consumer received the expected resources
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			assertThat(receivedResources).hasSize(1);
			assertThat(receivedResources.get(0).getUri()).isEqualTo("test://resource");
			assertThat(receivedResources.get(0).getName()).isEqualTo("Test Resource");
			assertThat(receivedResources.get(0).getDescription()).isEqualTo("A test resource");
		});

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testPromptsChangeNotificationHandling() {
		MockMcpTransport transport = new MockMcpTransport();

		// Create a list to store received prompts for verification
		List<Prompt> receivedPrompts = new ArrayList<>();

		// Create a consumer that will be called when prompts change
		Consumer<List<Prompt>> promptsChangeConsumer = prompts -> {
			receivedPrompts.addAll(prompts);
		};

		// Create client with prompts change consumer
		McpAsyncClient asyncMcpClient = McpClient.using(transport).promptsChangeConsumer(promptsChangeConsumer).async();

		// Create a mock prompts list that the server will return
		Prompt mockPrompt = new Prompt("test-prompt", "Test Prompt Description",
                Collections.singletonList(new PromptArgument("arg1", "Test argument", true)));
		ListPromptsResult mockPromptsResult = new ListPromptsResult(Collections.singletonList(mockPrompt), null);

		// Simulate server sending prompts/list_changed notification
		JSONRPCNotification notification = new JSONRPCNotification(Schema.JSONRPC_VERSION,
				MethodDefined.NotificationsPromptsListChanged.getValue(), null);
		transport.simulateIncomingMessage(notification);

		// Simulate server response to prompts/list request
		JSONRPCRequest promptsListRequest = transport.getLastSentMessageAsRequest();
		assertThat(promptsListRequest.getMethod()).isEqualTo(MethodDefined.PromptsList.getValue());

		JSONRPCResponse promptsListResponse = new JSONRPCResponse(Schema.JSONRPC_VERSION,
				promptsListRequest.getId(), mockPromptsResult, null);
		transport.simulateIncomingMessage(promptsListResponse);

		// Verify the consumer received the expected prompts
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			assertThat(receivedPrompts).hasSize(1);
			assertThat(receivedPrompts.get(0).getName()).isEqualTo("test-prompt");
			assertThat(receivedPrompts.get(0).getDescription()).isEqualTo("Test Prompt Description");
			assertThat(receivedPrompts.get(0).getArguments()).hasSize(1);
			assertThat(receivedPrompts.get(0).getArguments().get(0).getName()).isEqualTo("arg1");
		});

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testSamplingCreateMessageRequestHandling() {
		MockMcpTransport transport = new MockMcpTransport();

		// Create a test sampling handler that echoes back the input
		Function<CreateMessageRequest, CreateMessageResult> samplingHandler = request -> {
			Content content = request.getMessages().get(0).getContent();
			return new CreateMessageResult(Role.assistant, content, "test-model",
					CreateMessageResult.StopReason.END_TURN);
		};

		// Create client with sampling capability and handler
		McpAsyncClient asyncMcpClient = McpClient.using(transport)
			.capabilities(ClientCapabilities.builder().sampling().build())
			.sampling(samplingHandler)
			.async();

		// Create a mock create message request
		CreateMessageRequest messageRequest = new CreateMessageRequest(
				Arrays.asList(new SamplingMessage(Role.user, new TextContent("Test message"))),
				null, // modelPreferences
				"Test system prompt", CreateMessageRequest.ContextInclusionStrategy.NONE, 0.7, // temperature
				100, // maxTokens
				null, // stopSequences
				null // metadata
		);

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(Schema.JSONRPC_VERSION,
				MethodDefined.SamplingCreateMessage.getValue(), "test-id", messageRequest);
		transport.simulateIncomingMessage(request);

		// Verify response
		JSONRPCMessage sentMessage = transport.getLastSentMessage();
		assertThat(sentMessage).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse response = (JSONRPCResponse) sentMessage;
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getError()).isNull();

		CreateMessageResult result = transport.unmarshalFrom(response.getResult(),
				new TypeReference<CreateMessageResult>() {
				});
		assertThat(result).isNotNull();
		assertThat(result.getRole()).isEqualTo(Role.assistant);
		assertThat(result.getContent()).isNotNull();
		assertThat(result.getModel()).isEqualTo("test-model");
		assertThat(result.getStopReason()).isEqualTo(CreateMessageResult.StopReason.END_TURN);

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testSamplingCreateMessageRequestHandlingWithoutCapability() {
		MockMcpTransport transport = new MockMcpTransport();

		// Create client without sampling capability
		McpAsyncClient asyncMcpClient = McpClient.using(transport)
			.capabilities(ClientCapabilities.builder().build()) // No sampling capability
			.async();

		// Create a mock create message request
		CreateMessageRequest messageRequest = new CreateMessageRequest(
                Collections.singletonList(new SamplingMessage(Role.user, new TextContent("Test message"))),
				null, null, null, null, 0, null, null);

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(Schema.JSONRPC_VERSION,
				MethodDefined.SamplingCreateMessage.getValue(), "test-id", messageRequest);
		transport.simulateIncomingMessage(request);

		// Verify error response
		JSONRPCMessage sentMessage = transport.getLastSentMessage();
		assertThat(sentMessage).isInstanceOf(JSONRPCResponse.class);

		JSONRPCResponse response = (JSONRPCResponse) sentMessage;
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getResult()).isNull();
		assertThat(response.getError()).isNotNull();
		assertThat(response.getError().getMessage()).contains("Method not found: sampling/createMessage");

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testSamplingCreateMessageRequestHandlingWithNullHandler() {
		MockMcpTransport transport = new MockMcpTransport();

		// Create client with sampling capability but null handler
		assertThatThrownBy(
				() -> McpClient.using(transport).capabilities(ClientCapabilities.builder().sampling().build()).async())
			.isInstanceOf(McpError.class)
			.hasMessage("Sampling handler must not be null when client capabilities include sampling");
	}

}

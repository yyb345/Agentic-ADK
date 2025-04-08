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

package com.alibaba.langengine.mcp.spec;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.mcp.MockMcpTransport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test suite for {@link DefaultMcpSession} that verifies its JSON-RPC message handling,
 * request-response correlation, and notification processing.
 *
 * @author Christian Tzolov
 */
class DefaultMcpSessionTests {

	private final static Logger logger = LoggerFactory.getLogger(DefaultMcpSessionTests.class);

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private static final String TEST_METHOD = "test.method";

	private static final String TEST_NOTIFICATION = "test.notification";

	private static final String ECHO_METHOD = "echo";

	private DefaultMcpSession session;

	private MockMcpTransport transport;

	@BeforeEach
	void setUp() {
		transport = new MockMcpTransport();
		session = new DefaultMcpSession(TIMEOUT, transport, Collections.emptyMap(),
				Collections.singletonMap(TEST_NOTIFICATION, params -> Mono.fromRunnable(() -> logger.info("Status update: " + params))));
	}

	@AfterEach
	void tearDown() {
		if (session != null) {
			session.close();
		}
	}

	@Test
	void testConstructorWithInvalidArguments() {
		assertThatThrownBy(() -> new DefaultMcpSession(null, transport, Collections.emptyMap(), Collections.emptyMap()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("requstTimeout can not be null");

		assertThatThrownBy(() -> new DefaultMcpSession(TIMEOUT, null, Collections.emptyMap(), Collections.emptyMap()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("transport can not be null");
	}

	TypeReference<String> responseType = new TypeReference<String>() {
	};

	@Test
	void testSendRequest() {
		String testParam = "test parameter";
		String responseData = "test response";

		// Create a Mono that will emit the response after the request is sent
		Mono<String> responseMono = session.sendRequest(TEST_METHOD, testParam, responseType);
		// Verify response handling
		StepVerifier.create(responseMono).then(() -> {
			JSONRPCRequest request = transport.getLastSentMessageAsRequest();
			transport.simulateIncomingMessage(
					new JSONRPCResponse(Schema.JSONRPC_VERSION, request.getId(), responseData, null));
		}).consumeNextWith(response -> {
			// Verify the request was sent
			JSONRPCMessage sentMessage = transport.getLastSentMessageAsRequest();
			assertThat(sentMessage).isInstanceOf(JSONRPCRequest.class);
			JSONRPCRequest request = (JSONRPCRequest) sentMessage;
			assertThat(request.getMethod()).isEqualTo(TEST_METHOD);
			assertThat(request.getParams()).isEqualTo(testParam);
			assertThat(response).isEqualTo(responseData);
		}).verifyComplete();
	}

	@Test
	void testSendRequestWithError() {
		Mono<String> responseMono = session.sendRequest(TEST_METHOD, "test", responseType);

		// Verify error handling
		StepVerifier.create(responseMono).then(() -> {
			JSONRPCRequest request = transport.getLastSentMessageAsRequest();
			// Simulate error response
			JSONRPCError error = new JSONRPCError(
					Schema.ErrorCodes.METHOD_NOT_FOUND, "Method not found", null);
			transport.simulateIncomingMessage(
					new JSONRPCResponse(Schema.JSONRPC_VERSION, request.getId(), null, error));
		}).expectError(McpError.class).verify();
	}

	@Test
	void testRequestTimeout() {
		Mono<String> responseMono = session.sendRequest(TEST_METHOD, "test", responseType);

		// Verify timeout
		StepVerifier.create(responseMono)
			.expectError(java.util.concurrent.TimeoutException.class)
			.verify(TIMEOUT.plusSeconds(1));
	}

	@Test
	void testSendNotification() {
		Map<String, Object> params = Collections.singletonMap("key", "value");
		Mono<Void> notificationMono = session.sendNotification(TEST_NOTIFICATION, params);

		// Verify notification was sent
		StepVerifier.create(notificationMono).consumeSubscriptionWith(response -> {
			JSONRPCMessage sentMessage = transport.getLastSentMessage();
			assertThat(sentMessage).isInstanceOf(JSONRPCNotification.class);
			JSONRPCNotification notification = (JSONRPCNotification) sentMessage;
			assertThat(notification.getMethod()).isEqualTo(TEST_NOTIFICATION);
			assertThat(notification.getParams()).isEqualTo(params);
		}).verifyComplete();
	}

	@Test
	void testRequestHandling() {
		String echoMessage = "Hello MCP!";
		Map<String, DefaultMcpSession.RequestHandler> requestHandlers = Collections.singletonMap(ECHO_METHOD,
				params -> Mono.just(params));
		transport = new MockMcpTransport();
		session = new DefaultMcpSession(TIMEOUT, transport, requestHandlers, Collections.emptyMap());

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(Schema.JSONRPC_VERSION, ECHO_METHOD,
				"test-id", echoMessage);
		transport.simulateIncomingMessage(request);

		// Verify response
		JSONRPCMessage sentMessage = transport.getLastSentMessage();
		assertThat(sentMessage).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse response = (JSONRPCResponse) sentMessage;
		assertThat(response.getResult()).isEqualTo(echoMessage);
		assertThat(response.getError()).isNull();
	}

	@Test
	void testNotificationHandling() {
		Sinks.One<Object> receivedParams = Sinks.one();

		transport = new MockMcpTransport();
		session = new DefaultMcpSession(TIMEOUT, transport, Collections.emptyMap(),
				Collections.singletonMap(TEST_NOTIFICATION, params -> Mono.fromRunnable(() -> receivedParams.tryEmitValue(params))));

		// Simulate incoming notification from the server
		Map<String, Object> notificationParams = Collections.singletonMap("status", "ready");

		JSONRPCNotification notification = new JSONRPCNotification(Schema.JSONRPC_VERSION,
				TEST_NOTIFICATION, notificationParams);

		transport.simulateIncomingMessage(notification);

		// Verify handler was called
		assertThat(receivedParams.asMono().block(Duration.ofSeconds(1))).isEqualTo(notificationParams);
	}

	@Test
	void testUnknownMethodHandling() {
		// Simulate incoming request for unknown method
		JSONRPCRequest request = new JSONRPCRequest(Schema.JSONRPC_VERSION, "unknown.method",
				"test-id", null);
		transport.simulateIncomingMessage(request);

		// Verify error response
		JSONRPCMessage sentMessage = transport.getLastSentMessage();
		assertThat(sentMessage).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse response = (JSONRPCResponse) sentMessage;
		assertThat(response.getError()).isNotNull();
		assertThat(response.getError().getCode()).isEqualTo(Schema.ErrorCodes.METHOD_NOT_FOUND);
	}

	@Test
	void testGracefulShutdown() {
		StepVerifier.create(session.closeGracefully()).verifyComplete();
	}

}

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

package com.alibaba.langengine.mcp.server.transport;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.mcp.spec.JSONRPCRequest;
import com.alibaba.langengine.mcp.spec.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StdioServerTransport}.
 *
 * @author Christian Tzolov
 */
class StdioServerTransportTests {

	private final InputStream originalIn = System.in;

	private final PrintStream originalOut = System.out;

	private final PrintStream originalErr = System.err;

	private ByteArrayOutputStream testOut;

	private ByteArrayOutputStream testErr;

	private PrintStream testOutPrintStream;

	private StdioServerTransport transport;

	@BeforeEach
	void setUp() {
		testOut = new ByteArrayOutputStream();
		testErr = new ByteArrayOutputStream();
		testOutPrintStream = new PrintStream(testOut, true);
		System.setOut(testOutPrintStream);
		System.setErr(new PrintStream(testErr));
	}

	@AfterEach
	void tearDown() {
		if (transport != null) {
			transport.closeGracefully().block();
		}
		if (testOutPrintStream != null) {
			testOutPrintStream.close();
		}
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	void shouldHandleIncomingMessages() throws Exception {
		// Prepare test input
		String jsonMessage = "{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"params\":{},\"id\":1}";

		// Create transport with test streams
		transport = new StdioServerTransport();

		// Parse expected message
		JSONRPCRequest expected = JSON.parseObject(jsonMessage, JSONRPCRequest.class);

		// Connect transport with message handler and verify message
		StepVerifier.create(transport.connect(message -> message.doOnNext(msg -> {
			JSONRPCRequest received = (JSONRPCRequest) msg;
			assertThat(received.getId()).isEqualTo(expected.getId());
			assertThat(received.getMethod()).isEqualTo(expected.getMethod());
		}))).verifyComplete();
	}

	@Test
	@Disabled
	void shouldHandleOutgoingMessages() throws Exception {
		// Create transport with test streams
		transport = new StdioServerTransport();
		// transport = new StdioServerTransport(objectMapper, new BlockingInputStream(),
		// testOutPrintStream);

		// Create test messages
		JSONRPCRequest initMessage = new JSONRPCRequest(Schema.JSONRPC_VERSION, "init", "init-id",
				Collections.singletonMap("init", "true"));
		JSONRPCRequest testMessage = new JSONRPCRequest(Schema.JSONRPC_VERSION, "test", "test-id",
				Collections.singletonMap("key", "value"));

		// Connect transport, send messages, and verify output in a reactive chain
		StepVerifier.create(transport.connect(message -> message)
			.then(transport.sendMessage(initMessage))
			// .then(Mono.fromRunnable(() -> testOut.reset())) // Clear buffer after init
			// message
			.then(transport.sendMessage(testMessage))
			.then(Mono.fromCallable(() -> {
				String output = testOut.toString(String.valueOf(StandardCharsets.UTF_8));
				assertThat(output).contains("\"jsonrpc\":\"2.0\"");
				assertThat(output).contains("\"method\":\"test\"");
				assertThat(output).contains("\"id\":\"test-id\"");
				return null;
			}))).verifyComplete();
	}

	@Test
	void shouldWaitForProcessorsBeforeSendingMessage() {
		// Create transport with test streams
		transport = new StdioServerTransport();

		// Create test message
		JSONRPCRequest testMessage = new JSONRPCRequest(Schema.JSONRPC_VERSION, "test", "test-id",
				Collections.singletonMap("key", "value"));

		// Try to send message before connecting (before processors are ready)
		StepVerifier.create(transport.sendMessage(testMessage)).verifyTimeout(java.time.Duration.ofMillis(100));

		// Connect transport and verify message can be sent
		StepVerifier.create(transport.connect(message -> message).then(transport.sendMessage(testMessage)))
			.verifyComplete();
	}

	@Test
	void shouldCloseGracefully() {
		// Create transport with test streams
		transport = new StdioServerTransport();

		// Create test message
		JSONRPCRequest initMessage = new JSONRPCRequest(Schema.JSONRPC_VERSION, "init", "init-id",
				Collections.singletonMap("init", "true"));

		// Connect transport, send message, and close gracefully in a reactive chain
		StepVerifier
			.create(transport.connect(message -> message)
				.then(transport.sendMessage(initMessage))
				.then(transport.closeGracefully()))
			.verifyComplete();

		// Verify error log is empty
		assertThat(testErr.toString()).doesNotContain("Error");
	}

}

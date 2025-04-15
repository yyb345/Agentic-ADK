/*
 * Copyright 2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Copyright 2024 - 2024 the original author or authors.
*/
package com.alibaba.langengine.modelcontextprotocol.client.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * A Server-Sent Events (SSE) client implementation for JDK 1.8 compatibility.
 * This client establishes a connection to an SSE endpoint and processes the incoming 
 * event stream, parsing SSE-formatted messages into structured events.
 *
 * <p>
 * The client supports standard SSE event fields including:
 * <ul>
 * <li>event - The event type (defaults to "message" if not specified)</li>
 * <li>id - The event ID</li>
 * <li>data - The event payload data</li>
 * </ul>
 *
 * <p>
 * Events are delivered to a provided {@link SseEventHandler} which can process events and
 * handle any errors that occur during the connection.
 *
 * @author Christian Tzolov
 * @author aihe.ah
 * @see SseEventHandler
 * @see SseEvent
 */
public class FlowSseClient {

	private final ExecutorService executorService;

	/**
	 * Pattern to extract the data content from SSE data field lines. Matches lines
	 * starting with "data:" and captures the remaining content.
	 */
	private static final Pattern EVENT_DATA_PATTERN = Pattern.compile("^data:(.+)$", Pattern.MULTILINE);

	/**
	 * Pattern to extract the event ID from SSE id field lines. Matches lines starting
	 * with "id:" and captures the ID value.
	 */
	private static final Pattern EVENT_ID_PATTERN = Pattern.compile("^id:(.+)$", Pattern.MULTILINE);

	/**
	 * Pattern to extract the event type from SSE event field lines. Matches lines
	 * starting with "event:" and captures the event type.
	 */
	private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("^event:(.+)$", Pattern.MULTILINE);

	/**
	 * Class representing a Server-Sent Event with its standard fields.
	 */
	public static class SseEvent {
		private final String id;
		private final String type;
		private final String data;

		/**
		 * Creates a new SSE event with the specified fields.
		 * 
		 * @param id the event ID (may be null)
		 * @param type the event type (defaults to "message" if not specified in the stream)
		 * @param data the event payload data
		 */
		public SseEvent(String id, String type, String data) {
			this.id = id;
			this.type = type;
			this.data = data;
		}

		/**
		 * @return the event ID (may be null)
		 */
		public String id() {
			return id;
		}

		/**
		 * @return the event type
		 */
		public String type() {
			return type;
		}

		/**
		 * @return the event payload data
		 */
		public String data() {
			return data;
		}


		public String getData() {
			return data;
		}

		public String getId() {
			return id;
		}

		public String getType() {
			return type;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SseEvent sseEvent = (SseEvent) o;
			return Objects.equals(id, sseEvent.id) &&
				Objects.equals(type, sseEvent.type) &&
				Objects.equals(data, sseEvent.data);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, type, data);
		}

		@Override
		public String toString() {
			return "SseEvent{" +
				"id='" + id + '\'' +
				", type='" + type + '\'' +
				", data='" + data + '\'' +
				'}';
		}
	}

	/**
	 * Interface for handling SSE events and errors. Implementations can process received
	 * events and handle any errors that occur during the SSE connection.
	 */
	public interface SseEventHandler {

		/**
		 * Called when an SSE event is received.
		 * @param event the received SSE event containing id, type, and data
		 */
		void onEvent(SseEvent event);

		/**
		 * Called when an error occurs during the SSE connection.
		 * @param error the error that occurred
		 */
		void onError(Throwable error);

	}

	/**
	 * Creates a new FlowSseClient with a default thread pool.
	 */
	public FlowSseClient() {
		this(Executors.newCachedThreadPool());
	}

	/**
	 * Creates a new FlowSseClient with the specified executor service.
	 * @param executorService the executor service to use for background processing
	 */
	public FlowSseClient(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Subscribes to an SSE endpoint and processes the event stream.
	 *
	 * <p>
	 * This method establishes a connection to the specified URL and begins processing the
	 * SSE stream. Events are parsed and delivered to the provided event handler. The
	 * connection remains active until either an error occurs or the server closes the
	 * connection.
	 * @param url the SSE endpoint URL to connect to
	 * @param eventHandler the handler that will receive SSE events and error
	 * notifications
	 * @return a CompletableFuture that completes when the connection is closed
	 * @throws RuntimeException if the connection fails with a non-200 status code
	 */
	public CompletableFuture<Void> subscribe(final String url, final SseEventHandler eventHandler) {
		return subscribe(url, null, eventHandler);
	}

	/**
	 * Subscribes to an SSE endpoint with custom headers and processes the event stream.
	 *
	 * <p>
	 * This method establishes a connection to the specified URL with custom headers and begins 
	 * processing the SSE stream. Events are parsed and delivered to the provided event handler. 
	 * The connection remains active until either an error occurs or the server closes the
	 * connection.
	 * @param url the SSE endpoint URL to connect to
	 * @param headers custom HTTP headers for authentication and authorization (may be null)
	 * @param eventHandler the handler that will receive SSE events and error notifications
	 * @return a CompletableFuture that completes when the connection is closed
	 * @throws RuntimeException if the connection fails with a non-200 status code
	 */
	public CompletableFuture<Void> subscribe(final String url, final Map<String, String> headers, final SseEventHandler eventHandler) {
		final CompletableFuture<Void> future = new CompletableFuture<>();
		final AtomicBoolean isRunning = new AtomicBoolean(true);

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				BufferedReader reader = null;

				try {
					// Establish connection
					URL sseUrl = new URL(url);
					connection = (HttpURLConnection) sseUrl.openConnection();
					connection.setRequestMethod("GET");
					connection.setRequestProperty("Accept", "text/event-stream");
					connection.setRequestProperty("Cache-Control", "no-cache");
					connection.setDoInput(true);
					
					// Apply custom headers if provided
					if (headers != null && !headers.isEmpty()) {
						for (Map.Entry<String, String> entry : headers.entrySet()) {
							connection.setRequestProperty(entry.getKey(), entry.getValue());
						}
					}
					
					connection.connect();

					// Check response code
					int status = connection.getResponseCode();
					if (status != 200 && status != 201 && status != 202 && status != 206) {
						throw new RuntimeException("Failed to connect to SSE stream. Unexpected status code: " + status);
					}

					// Process the stream
					InputStream inputStream = connection.getInputStream();
					reader = new BufferedReader(new InputStreamReader(inputStream));

					StringBuilder eventBuilder = new StringBuilder();
					AtomicReference<String> currentEventId = new AtomicReference<String>();
					AtomicReference<String> currentEventType = new AtomicReference<String>("message");

					String line;
					while (isRunning.get() && (line = reader.readLine()) != null) {
						if (line.isEmpty()) {
							// Empty line means end of event
							if (eventBuilder.length() > 0) {
								String eventData = eventBuilder.toString();
								SseEvent event = new SseEvent(currentEventId.get(), currentEventType.get(), eventData.trim());
								eventHandler.onEvent(event);
								eventBuilder.setLength(0);
							}
						} else {
							if (line.startsWith("data:")) {
								java.util.regex.Matcher matcher = EVENT_DATA_PATTERN.matcher(line);
								if (matcher.find()) {
									eventBuilder.append(matcher.group(1).trim()).append("\n");
								}
							} else if (line.startsWith("id:")) {
								java.util.regex.Matcher matcher = EVENT_ID_PATTERN.matcher(line);
								if (matcher.find()) {
									currentEventId.set(matcher.group(1).trim());
								}
							} else if (line.startsWith("event:")) {
								java.util.regex.Matcher matcher = EVENT_TYPE_PATTERN.matcher(line);
								if (matcher.find()) {
									currentEventType.set(matcher.group(1).trim());
								}
							}
						}
					}

					// Handle any remaining event data
					if (eventBuilder.length() > 0) {
						String eventData = eventBuilder.toString();
						SseEvent event = new SseEvent(currentEventId.get(), currentEventType.get(), eventData.trim());
						eventHandler.onEvent(event);
					}

					future.complete(null);
				} catch (Exception e) {
					eventHandler.onError(e);
					future.completeExceptionally(e);
				} finally {
					isRunning.set(false);
					
					// Clean up resources
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							// Ignore
						}
					}
					
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		});

		return future;
	}

	/**
	 * Closes the SSE client and releases any resources.
	 */
	public void close() {
		if (!executorService.isShutdown()) {
			executorService.shutdown();
		}
	}
}

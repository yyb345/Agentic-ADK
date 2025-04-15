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
package com.alibaba.langengine.modelcontextprotocol.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.modelcontextprotocol.client.transport.ServerParameters;
import com.alibaba.langengine.modelcontextprotocol.spec.Tool;

/**
 * Client implementation for the weather server.
 */
public class WeatherClient extends BaseMcpClient {

	@Override
	protected ServerParameters createServerParameters() {
		return ServerParameters.builder("node")
			.arg("/Users/aihe/Desktop/mcp-frontend/user-test.js")
			.build();
	}

	@Override
	protected void runTest(List<Tool> tools) {
		System.out.println("Testing weather server...");

		// Try to find the forecast tool
		Tool forecastTool = findToolByNamePrefix(tools, "get-forecast");

		if (forecastTool != null) {
			// Get weather forecast
			System.out.println("\nGetting weather forecast:");
			Map<String, Object> params = new HashMap<>();
			// Use latitude and longitude as required by the tool schema
			params.put("latitude", 40.7128); // New York latitude
			params.put("longitude", -74.0060); // New York longitude
			callToolAndPrintResult(forecastTool, params);
		}

		// Try to find the alerts tool
		Tool alertsTool = findToolByNamePrefix(tools, "get-alerts");
		if (alertsTool != null) {
			// Get weather alerts
			System.out.println("\nGetting weather alerts:");
			Map<String, Object> params = new HashMap<>();
			params.put("state", "CA");
			callToolAndPrintResult(alertsTool, params);
		}
	}

	public static void main(String[] args) {
		WeatherClient client = new WeatherClient();
		client.initialize("Weather Client");
		client.run();
	}

}

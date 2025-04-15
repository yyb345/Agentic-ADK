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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.langengine.modelcontextprotocol.client.McpClient;
import com.alibaba.langengine.modelcontextprotocol.client.McpSyncClient;
import com.alibaba.langengine.modelcontextprotocol.client.transport.ServerParameters;
import com.alibaba.langengine.modelcontextprotocol.client.transport.StdioClientTransport;
import com.alibaba.langengine.modelcontextprotocol.spec.CallToolRequest;
import com.alibaba.langengine.modelcontextprotocol.spec.CallToolResult;
import com.alibaba.langengine.modelcontextprotocol.spec.ClientCapabilities;
import com.alibaba.langengine.modelcontextprotocol.spec.Implementation;
import com.alibaba.langengine.modelcontextprotocol.spec.ListPromptsResult;
import com.alibaba.langengine.modelcontextprotocol.spec.ListResourcesResult;
import com.alibaba.langengine.modelcontextprotocol.spec.ListToolsResult;
import com.alibaba.langengine.modelcontextprotocol.spec.Prompt;
import com.alibaba.langengine.modelcontextprotocol.spec.PromptArgument;
import com.alibaba.langengine.modelcontextprotocol.spec.Resource;
import com.alibaba.langengine.modelcontextprotocol.spec.TextContent;
import com.alibaba.langengine.modelcontextprotocol.spec.Tool;

/**
 * Base class for MCP client implementations.
 */
public abstract class BaseMcpClient {

    protected McpSyncClient client;

    /**
     * Initialize the MCP client.
     *
     * @param clientName The name of the client
     */
    public void initialize(String clientName) {
        // 检查是否启用详细日志
        boolean enableDetailedLogs = Boolean.parseBoolean(System.getProperty("logback.debug", "false"));
        if (enableDetailedLogs) {
            System.out.println("Detailed logging enabled. Check mcp-client-debug.log for complete logs.");
        } else {
            // 如果没有启用详细日志，则禁用所有反应式日志
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");
        }

        // 创建客户端能力
        ClientCapabilities capabilities = ClientCapabilities.builder().roots(true).build();

        // 创建服务器参数
        ServerParameters serverParams = createServerParameters();

        // 创建传输层
        StdioClientTransport transport = new StdioClientTransport(serverParams);

        // 创建客户端，增加超时
        client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(60)) // 更长的超时
                .capabilities(capabilities)
                .clientInfo(new Implementation(clientName, "1.0.0"))
                .build();
    }

    /**
     * Run the client test.
     */
    public void run() {
        try {
            System.out.println("Starting server and initializing client...");

            // 等待服务器启动的时间
            TimeUnit.SECONDS.sleep(2);

            // 初始化客户端
            List<Tool> tools;
            try {
                System.out.println("Initializing client connection (with 10-second timeout)...");
                client.initialize();
                System.out.println("Client initialized successfully!");

                // 获取工具列表
                ListToolsResult toolsResult = client.listTools();
                tools = toolsResult.tools();
                System.out.println("Server returned " + tools.size() + " available tools");

                if (tools.isEmpty()) {
                    System.out.println("No tools available from server.");
                    return;
                }
            } catch (Exception initEx) {
                // Check if it's a timeout exception
                if (initEx.getCause() instanceof java.util.concurrent.TimeoutException ||
                    initEx instanceof java.util.concurrent.TimeoutException) {
                    System.out.println("Connection timed out after 10 seconds. The server might be taking too long to respond.");
                } else {
                    System.out.println("Failed to initialize client: " + initEx.getMessage());
                    initEx.printStackTrace();
                }
                return;
            }

            // 打印所有可用工具
            System.out.println("\nAvailable tools:");
            tools.forEach(tool -> {
                System.out.println("- " + tool.name() + ": " + tool.description());
                // Print tool input schema if available
                if (tool.inputSchema() != null) {
                    System.out.println("  Input Schema:");
                    System.out.println("  Type: " + tool.inputSchema().type());
                    System.out.println("  Properties: " + tool.inputSchema().properties());
                    System.out.println("  Required: " + tool.inputSchema().required());
                }
            });
            System.out.println();

            // Try to list prompts, but don't fail if the server doesn't support this method
            try {
                ListPromptsResult listPromptsResult = client.listPrompts();
                List<Prompt> prompts = listPromptsResult.prompts();
                if (!prompts.isEmpty()) {
                    System.out.println("\nAvailable prompts:");
                    for (Prompt prompt : prompts) {
                        List<PromptArgument> arguments = prompt.arguments();
                        for (PromptArgument argument : arguments) {
                            System.out.println("Prompt: " + prompt.name());
                            System.out.println("Argument: " + argument.name());
                            System.out.println("Description: " + argument.description());
                            System.out.println();
                        }
                    }
                }
            } catch (Exception promptEx) {
                System.out.println("\nServer does not support prompts: " + promptEx.getMessage());
            }

            // Try to list resources, but don't fail if the server doesn't support this method
            try {
                ListResourcesResult listResourcesResult = client.listResources();
                List<Resource> resources = listResourcesResult.resources();
                if (!resources.isEmpty()) {
                    System.out.println("\nAvailable resources:");
                    for (Resource resource : resources) {
                        System.out.println("Resource: " + resource.name());
                        System.out.println("Description: " + resource.description());
                        System.out.println();
                    }
                }
            } catch (Exception resourceEx) {
                System.out.println("\nServer does not support resources: " + resourceEx.getMessage());
            }

            // 执行特定服务器的测试
            try {
                runTest(tools);
            } catch (Exception testEx) {
                System.err.println("Error running test: " + testEx.getMessage());
                testEx.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭客户端
            if (client != null) {
                try {
                    System.out.println("Closing client...");
                    client.close();
                    System.out.println("Client closed.");
                } catch (Exception closeEx) {
                    System.err.println("Error closing client: " + closeEx.getMessage());
                }
            }

            // 关闭共享的线程池
            try {
                com.alibaba.langengine.modelcontextprotocol.client.transport.StdioClientTransport.shutdownSharedScheduler();
            } catch (Exception e) {
                System.err.println("Error shutting down shared scheduler: " + e.getMessage());
            }
        }
    }

    /**
     * Create server parameters for the specific server type.
     *
     * @return ServerParameters for the specific server
     */
    protected abstract ServerParameters createServerParameters();

    /**
     * Run tests specific to the server type.
     *
     * @param tools List of available tools from the server
     */
    protected abstract void runTest(List<Tool> tools);

    /**
     * 根据名称前缀查找工具
     */
    protected Tool findToolByNamePrefix(List<Tool> tools, String prefix) {
        return tools.stream().filter(tool -> tool.name().startsWith(prefix)).findFirst().orElse(null);
    }

    /**
     * 调用工具并打印结果
     */
    protected void callToolAndPrintResult(Tool tool, Map<String, Object> params) {
        try {
            System.out.println("Calling tool: " + tool.name());
            CallToolResult result = client.callTool(new CallToolRequest(tool.name(), params));

            // 输出结果
            result.content().forEach(content -> {
                if (content instanceof TextContent) {
                    System.out.println(((TextContent) content).text());
                } else {
                    System.out.println("Non-text content received: " + content.getClass().getSimpleName());
                }
            });
        } catch (Exception e) {
            System.err.println("Error calling tool '" + tool.name() + "': " + e.getMessage());
        }
    }

}

package com.alibaba.langengine.modelcontextprotocol.examples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alibaba.langengine.modelcontextprotocol.client.McpClient;
import com.alibaba.langengine.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import com.alibaba.langengine.modelcontextprotocol.client.transport.ServerParameters;
import com.alibaba.langengine.modelcontextprotocol.spec.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client implementation for the SSE HTTP Time server. This client connects to a server
 * using Server-Sent Events (SSE) over HTTP instead of the standard input/output transport
 * used by other clients.
 */
public class SseHttpTimeClient extends BaseMcpClient {

    private static final String BASE_URL = "http://localhost:3001";

    private static final String NPX_COMMAND = "npx";

    private static final String MCP_PACKAGE = "@composio/mcp@latest";

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected ServerParameters createServerParameters() {
        return null;
    }

    /**
     * Use the standard initialize method from BaseMcpClient
     */
    @Override
    public void initialize(String clientName) {
        // 禁用所有反应式日志
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO"); // 改为 INFO
        // 以查看更多调试信息

        // 创建客户端能力
        ClientCapabilities capabilities = ClientCapabilities.builder().roots(true).build();

        // 创建传输层 - 使用完整的 SSE 端点 URL
        HttpClientSseClientTransport transport = new HttpClientSseClientTransport(BASE_URL);

        // 创建客户端，增加超时
        client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(30)) // 减少超时时间以更快地发现问题
                .capabilities(capabilities)
                .clientInfo(new Implementation(clientName, "1.0.0"))
                .build();

        System.out.println("Connecting to SSE server at: " + BASE_URL);
    }

    @Override
    protected void runTest(List<Tool> tools) {
        System.out.println("Testing SSE HTTP Time server...");


        Object ping = client.ping();
        System.out.println(ping);

        // 打印所有可用工具的详细信息
        System.out.println("\nDetailed tool information:");
        tools.forEach(tool -> {
            System.out.println("\nTool: " + tool.name());
            System.out.println("Description: " + tool.description());
            try {
                System.out.println("Schema: " + tool.inputSchema());
            } catch (Exception e) {
                System.out.println("Schema: [Error retrieving schema]");
            }
        });

        // 根据 JS 服务器中的工具尝试使用加法工具
        Tool addTool = findToolByNamePrefix(tools, "add");
        if (addTool != null) {
            System.out.println("\nTesting addition tool:");
            Map<String, Object> params = new HashMap<>();
            params.put("a", 5);
            params.put("b", 3);
            callToolAndPrintResult(addTool, params);
        }

        // 根据 JS 服务器中的工具尝试使用乘法工具
        Tool multiplyTool = findToolByNamePrefix(tools, "multiply");
        if (multiplyTool != null) {
            System.out.println("\nTesting multiplication tool:");
            Map<String, Object> params = new HashMap<>();
            params.put("a", 6);
            params.put("b", 7);
            callToolAndPrintResult(multiplyTool, params);
        }

        // 如果没有找到特定工具，尝试使用第一个可用工具
        if (addTool == null && multiplyTool == null && !tools.isEmpty()) {
            Tool firstTool = tools.get(0);
            System.out.println("\nNo specific tool found. Using first available tool: " + firstTool.name());

            // 创建一个空参数集
            Map<String, Object> params = new HashMap<>();
            callToolAndPrintResult(firstTool, params);
        }

        // 注意：Java SDK 可能不支持资源相关功能，所以我们只使用工具调用
        System.out.println("\nNote: Resource functionality may not be supported in the Java SDK");
        System.out.println("Only tool calls are being tested in this client.");
        ListResourcesResult listResourcesResult = client.listResources();
        List<Resource> resources = listResourcesResult.resources();
        if (!resources.isEmpty()) {
            System.out.println("\nAvailable resources:");
            resources.forEach(resource -> {
                System.out.println("- " + resource.name() + ": " + resource.description());
            });
            System.out.println();
        }

        ListResourceTemplatesResult listResourceTemplatesResult = client.listResourceTemplates();
        List<ResourceTemplate> resourceTemplates = listResourceTemplatesResult.resourceTemplates();
        if (!resourceTemplates.isEmpty()) {
            System.out.println("Available resource templates:");
            resourceTemplates.forEach(resourceTemplate -> {
                System.out.println("- " + resourceTemplate.name() + ": " + resourceTemplate.description());
                System.out.println(resourceTemplate.uriTemplate());
            });
            System.out.println();
        }

        ReadResourceResult readResourceResult = client
                .readResource(new ReadResourceRequest("greeting://World"));
        List<ResourceContents> contents = readResourceResult.contents();
        if (!contents.isEmpty()) {
            System.out.println("Contents of resource 'greeting://World':");
            contents.forEach(content -> {
                String uri = content.uri();
                String mimeType = content.mimeType();
                System.out.println(content.getClass().getSimpleName());
                try {
                    System.out.println(objectMapper.writeValueAsString(content));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        ReadResourceResult result = client.readResource(new ReadResourceRequest("greeting://xxx?avdsa=asdsa"));
        List<ResourceContents> contents2 = result.contents();
        if (!contents2.isEmpty()) {
            System.out.println("Contents of resource 'greeting://xxx':");
            contents2.forEach(content -> {
                String uri = content.uri();
                String mimeType = content.mimeType();
                System.out.println(content.getClass().getSimpleName());
                try {
                    System.out.println(objectMapper.writeValueAsString(content));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    /**
     * 覆盖父类的 run 方法，避免调用 JavaScript SSE 服务器不支持的方法
     */
    @Override
    public void run() {
        try {
            System.out.println("Starting server and initializing client...");

            // 初始化客户端
            try {
                System.out.println("Initializing client connection...");
                client.initialize();
                System.out.println("Client initialized successfully!");

                // 获取工具列表
                ListToolsResult toolsResult = client.listTools();
                List<Tool> tools = toolsResult.tools();
                System.out.println("Server returned " + tools.size() + " available tools");

                if (tools.isEmpty()) {
                    System.out.println("No tools available from server.");
                    return;
                }

                // 打印所有可用工具
                System.out.println("\nAvailable tools:");
                tools.forEach(tool -> {
                    System.out.println("- " + tool.name() + ": " + tool.description());
                });
                System.out.println();

                // 注意：跳过 listPrompts 和 listResources 调用，因为 JavaScript SSE 服务器不支持这些方法

                // 执行特定服务器的测试
                runTest(tools);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭客户端
            System.out.println("Closing client...");
            client.close();
            System.out.println("Client closed.");
        }
    }

    public static void main(String[] args) {
        SseHttpTimeClient client = new SseHttpTimeClient();
        client.initialize("SSE HTTP Time Client");
        client.run();
    }

}

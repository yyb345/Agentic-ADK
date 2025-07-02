package com.alibaba.langengine.modelcontextprotocol.examples;

import com.alibaba.langengine.modelcontextprotocol.client.McpClient;
import com.alibaba.langengine.modelcontextprotocol.client.McpSyncClient;
import com.alibaba.langengine.modelcontextprotocol.client.transport.ServerParameters;
import com.alibaba.langengine.modelcontextprotocol.client.transport.StdioClientTransport;
import com.alibaba.langengine.modelcontextprotocol.spec.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        // 禁用所有反应式日志
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");

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

                ListPromptsResult listPromptsResult = client.listPrompts();
                List<Prompt> prompts = listPromptsResult.prompts();
                for (Prompt prompt : prompts) {
                    List<PromptArgument> arguments = prompt.arguments();
                    for (PromptArgument argument : arguments) {
                        System.out.println("Prompt: " + prompt.name());
                        System.out.println("Argument: " + argument.name());
                        System.out.println("Description: " + argument.description());
                        System.out.println();
                    }
                }

                ListResourcesResult listResourcesResult = client.listResources();
                List<Resource> resources = listResourcesResult.resources();
                for (Resource resource : resources) {
                    System.out.println("Resource: " + resource.name());
                    System.out.println("Description: " + resource.description());
                    System.out.println();
                }

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

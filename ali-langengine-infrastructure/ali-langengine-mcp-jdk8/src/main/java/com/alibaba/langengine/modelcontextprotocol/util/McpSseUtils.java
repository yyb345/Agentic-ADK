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
package com.alibaba.langengine.modelcontextprotocol.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.modelcontextprotocol.client.McpClient;
import com.alibaba.langengine.modelcontextprotocol.client.McpSyncClient;
import com.alibaba.langengine.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import com.alibaba.langengine.modelcontextprotocol.spec.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * MCPSSE工具类
 * 提供MCP (Model Context Protocol) SSE客户端的工具方法，包括初始化、列出工具、调用工具等功能
 *
 * @author aihe.ah
 * @date 2025/4/3
 */
@Slf4j
public class McpSseUtils {

    // ObjectMapper removed as it's not used in this utility class

    /**
     * 默认请求超时时间（秒）
     */
    private static final int DEFAULT_REQUEST_TIMEOUT = 30;

    /**
     * 默认ping超时时间（秒）
     */
    private static final int DEFAULT_PING_TIMEOUT_SECONDS = 3;

    /**
     * 客户端缓存，key为服务地址，value为客户端实例
     */
    private static final ConcurrentHashMap<String, McpSyncClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 默认SSE端点路径
     */
    private static final String DEFAULT_SSE_ENDPOINT = "";

    /**
     * 全局线程池，用于异步操作
     */
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "McpSseUtils-ThreadPool");
        thread.setDaemon(true);
        return thread;
    });

    private static class AgentMagicCoreSwitch {
        public static boolean ALWAYS_CREATE_NEW_MCP_CLIENT = true;
        public static List<String> notProcessMcpUrls = new ArrayList<>();
    }

    /**
     * 处理URL和端点
     * 如果需要处理，则提取baseUrl的域名部分，并将路径部分与端点合并
     * 保留原始URL中的查询参数
     *
     * @param baseUrl    原始服务地址
     * @param endpoint   原始端点路径
     * @param processUrl 是否处理URL
     * @return 处理后的[baseUrl, endpoint]数组
     */
    private static String[] processUrlAndEndpoint(String baseUrl, String endpoint, boolean processUrl) {
        if (!processUrl || baseUrl == null) {
            return new String[]{baseUrl, endpoint};
        }

        try {

            if (AgentMagicCoreSwitch.notProcessMcpUrls.contains(baseUrl)) {
                return new String[]{baseUrl, endpoint};
            }

            // 解析URL
            URI uri = new URI(baseUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();
            String query = uri.getQuery(); // 获取查询参数

            // 构建不带路径的baseUrl
            StringBuilder cleanBaseUrl = new StringBuilder();
            cleanBaseUrl.append(scheme).append("://").append(host);
            if (port != -1) {
                cleanBaseUrl.append(":").append(port);
            }

            // 合并路径和端点
            String combinedEndpoint = endpoint;
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                // 确保path以/开头
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }

                // 确保endpoint以/开头
                if (StringUtils.isNotEmpty(endpoint) && !endpoint.startsWith("/")) {
                    combinedEndpoint = "/" + endpoint;
                }

                // 合并路径
                combinedEndpoint = path + combinedEndpoint;
            }

            // 检查并处理重复的/sse
            if (combinedEndpoint != null && combinedEndpoint.contains("/sse/sse")) {
                combinedEndpoint = combinedEndpoint.replace("/sse/sse", "/sse");
            }

            // 添加查询参数到endpoint
            if (query != null && !query.isEmpty() && combinedEndpoint != null) {
                combinedEndpoint = combinedEndpoint + (combinedEndpoint.contains("?") ? "&" : "?") + query;
            }

            log.info("处理后的URL: baseUrl={}, endpoint={}", cleanBaseUrl, combinedEndpoint);
            return new String[]{cleanBaseUrl.toString(), combinedEndpoint};
        } catch (Exception e) {
            log.warn("处理URL时发生异常，将使用原始URL: {}, 异常: {}", baseUrl, e.getMessage());
            return new String[]{baseUrl, endpoint};
        }
    }

    /**
     * 初始化MCP客户端
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @param sseEndpoint           SSE端点路径
     * @return 初始化后的客户端
     */
    public static McpSyncClient initializeClient(String baseUrl, String clientName, String version,
                                                 Integer requestTimeoutSeconds, Map<String, Object> authParams,
                                                 String sseEndpoint) {
        log.info("初始化MCP客户端，服务地址: {}, 客户端名称: {}, 授权参数: {}, SSE端点: {}",
                baseUrl, clientName, authParams, sseEndpoint);

        // 创建客户端能力
        ClientCapabilities capabilities = ClientCapabilities.builder().roots(true).build();

        // 把authParams转换成Map<String,String>
        Map<String, String> authParamsStringMap = authParams != null ? authParams.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())) : new HashMap<>();

        // 使用传入的endpoint或默认值
        String endpoint = StringUtils.isNotEmpty(sseEndpoint) ? sseEndpoint : DEFAULT_SSE_ENDPOINT;

        String[] urlAndEndpoint = processUrlAndEndpoint(baseUrl, endpoint, true);
        baseUrl = urlAndEndpoint[0];
        endpoint = urlAndEndpoint[1];

        // 创建传输层
        HttpClientSseClientTransport transport = new HttpClientSseClientTransport
                .Builder(baseUrl)
                .sseEndpoint(endpoint)
                .headers(authParamsStringMap)
                .build();

        // 创建客户端，设置超时时间
        int timeout = requestTimeoutSeconds != null ? requestTimeoutSeconds : DEFAULT_REQUEST_TIMEOUT;
        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(timeout))
                .capabilities(capabilities)
                .clientInfo(new Implementation(clientName, version != null ? version : "1.0.0"))
                .build();

        return client;
    }

    /**
     * 初始化MCP客户端（使用默认SSE端点）
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @return 初始化后的客户端
     */
    public static McpSyncClient initializeClient(String baseUrl, String clientName, String version,
                                                 Integer requestTimeoutSeconds, Map<String, Object> authParams) {
        return initializeClient(baseUrl, clientName, version, requestTimeoutSeconds, authParams, null);
    }

    /**
     * 获取MCP客户端（带缓存）
     * 如果缓存中存在且连接正常，则返回缓存中的客户端；否则创建新客户端
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @param sseEndpoint           SSE端点路径
     * @return MCP客户端
     */
    public static McpSyncClient getClient(String baseUrl, String clientName, String version,
                                          Integer requestTimeoutSeconds, Map<String, Object> authParams,
                                          String sseEndpoint) {
        // 如果开关打开，则每次都创建新的客户端
        if (AgentMagicCoreSwitch.ALWAYS_CREATE_NEW_MCP_CLIENT) {
            log.info("开关已开启，创建新的MCP客户端，服务地址: {}, SSE端点: {}", baseUrl, sseEndpoint);
            McpSyncClient newClient = initializeClient(baseUrl, clientName, version,
                    requestTimeoutSeconds, authParams, sseEndpoint);
            try {
                newClient.initialize();
                return newClient;
            } catch (Exception e) {
                log.error("MCP客户端初始化失败，服务地址: {}, 异常: {}", baseUrl, e.getMessage());
                throw new RuntimeException("MCP客户端初始化失败: " + e.getMessage(), e);
            }
        }

        // 开关关闭，使用缓存逻辑
        String cacheKey = generateCacheKey(baseUrl, authParams, sseEndpoint);

        // 检查缓存中是否存在客户端
        McpSyncClient cachedClient = clientCache.get(cacheKey);

        // 如果缓存中存在客户端，异步检查连接是否正常
        if (cachedClient != null) {
            try {
                // 通过异步ping检查连接状态，设置超时时间
                Object pingResult = pingWithTimeout(cachedClient, DEFAULT_PING_TIMEOUT_SECONDS);
                log.info("MCP客户端ping结果: {}", pingResult);
                if (pingResult != null) {
                    log.info("使用缓存的MCP客户端，服务地址: {}, SSE端点: {}", baseUrl, sseEndpoint);
                    return cachedClient;
                } else {
                    log.warn("缓存的MCP客户端ping无响应或超时，将创建新客户端，服务地址: {}", baseUrl);
                }
            } catch (Exception e) {
                log.warn("缓存的MCP客户端连接异常，将创建新客户端，服务地址: {}, 异常: {}", baseUrl, e.getMessage());
            }
        }

        // 创建新客户端
        McpSyncClient newClient = initializeClient(baseUrl, clientName, version, requestTimeoutSeconds, authParams, sseEndpoint);

        try {
            // 初始化客户端连接
            newClient.initialize();
            log.info("MCP客户端初始化成功，服务地址: {}, SSE端点: {}", baseUrl, sseEndpoint);

            // 更新缓存
            clientCache.put(cacheKey, newClient);

            return newClient;
        } catch (Exception e) {
            log.error("MCP客户端初始化失败，服务地址: {}, 异常: {}", baseUrl, e.getMessage());
            throw new RuntimeException("MCP客户端初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取MCP客户端（带缓存），使用默认SSE端点
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @return MCP客户端
     */
    public static McpSyncClient getClient(String baseUrl, String clientName, String version,
                                          Integer requestTimeoutSeconds, Map<String, Object> authParams) {
        return getClient(baseUrl, clientName, version, requestTimeoutSeconds, authParams, null);
    }

    /**
     * 生成缓存键
     *
     * @param baseUrl     服务地址
     * @param authParams  授权参数
     * @param sseEndpoint SSE端点路径
     * @return 缓存键
     */
    private static String generateCacheKey(String baseUrl, Map<String, Object> authParams, String sseEndpoint) {
        StringBuilder cacheKey = new StringBuilder(baseUrl);

        // 加入端点信息
        if (StringUtils.isNotEmpty(sseEndpoint)) {
            cacheKey.append("_").append(sseEndpoint);
        } else {
            cacheKey.append("_").append(DEFAULT_SSE_ENDPOINT);
        }

        // 加入授权参数信息
        if (authParams != null && !authParams.isEmpty()) {
            cacheKey.append("_").append(authParams.toString());
        }

        return cacheKey.toString();
    }

    /**
     * 列出可用工具
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @param sseEndpoint           SSE端点路径
     * @return 工具列表
     */
    public static List<Tool> listTools(String baseUrl, String clientName, String version,
                                       Integer requestTimeoutSeconds, Map<String, Object> authParams,
                                       String sseEndpoint) {
        McpSyncClient client = getClient(baseUrl, clientName, version, requestTimeoutSeconds, authParams, sseEndpoint);

        try {
            ListToolsResult toolsResult = client.listTools();
            List<Tool> tools = toolsResult.tools();
            log.info("获取到{}个可用工具，服务地址: {}, SSE端点: {}", tools.size(), baseUrl, sseEndpoint);

            return tools;
        } catch (Exception e) {
            log.error("获取工具列表失败，服务地址: {}, SSE端点: {}, 异常: {}", baseUrl, sseEndpoint, e.getMessage());
            throw new RuntimeException("获取工具列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 列出可用工具（使用默认SSE端点）
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @return 工具列表
     */
    public static List<Tool> listTools(String baseUrl, String clientName, String version,
                                       Integer requestTimeoutSeconds, Map<String, Object> authParams) {
        return listTools(baseUrl, clientName, version, requestTimeoutSeconds, authParams, null);
    }


    public static List<Tool> listTools(String baseUrl, String clientName, String version,
                                       Integer requestTimeoutSeconds) {
        return listTools(baseUrl, clientName, version, requestTimeoutSeconds, new HashMap<>(), null);
    }

    /**
     * 根据名称前缀查找工具
     *
     * @param tools  工具列表
     * @param prefix 名称前缀
     * @return 匹配的工具，如果没有找到则返回null
     */
    public static Tool findToolByNamePrefix(List<Tool> tools, String prefix) {
        return tools.stream()
                .filter(tool -> tool.name().startsWith(prefix))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据名称精确查找工具
     *
     * @param tools 工具列表
     * @param name  工具名称
     * @return 匹配的工具，如果没有找到则返回null
     */
    public static Tool findToolByName(List<Tool> tools, String name) {
        return tools.stream()
                .filter(tool -> tool.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 调用工具
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param toolName              工具名称
     * @param params                工具参数
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @param sseEndpoint           SSE端点路径
     * @return 工具调用结果
     */
    public static CallToolResult callTool(String baseUrl, String clientName, String version,
                                          String toolName, Map<String, Object> params,
                                          Integer requestTimeoutSeconds, Map<String, Object> authParams,
                                          String sseEndpoint) {
        McpSyncClient client = getClient(baseUrl, clientName, version, requestTimeoutSeconds, authParams, sseEndpoint);

        try {
            log.info("调用工具: {}, 参数: {}, 服务地址: {}, SSE端点: {}", toolName, params, baseUrl, sseEndpoint);
            CallToolResult result = client.callTool(new CallToolRequest(toolName, params));
            return result;
        } catch (Exception e) {
            log.error("调用工具失败，工具: {}, 服务地址: {}, SSE端点: {}, 异常: {}",
                    toolName, baseUrl, sseEndpoint, e.getMessage());
            throw new RuntimeException("调用工具失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用工具（使用默认SSE端点）
     *
     * @param baseUrl               服务地址
     * @param clientName            客户端名称
     * @param version               客户端版本
     * @param toolName              工具名称
     * @param params                工具参数
     * @param requestTimeoutSeconds 请求超时时间（秒）
     * @param authParams            授权参数
     * @return 工具调用结果
     */
    public static CallToolResult callTool(String baseUrl, String clientName, String version,
                                          String toolName, Map<String, Object> params,
                                          Integer requestTimeoutSeconds, Map<String, Object> authParams) {
        return callTool(baseUrl, clientName, version, toolName, params, requestTimeoutSeconds, authParams, null);
    }

    public static CallToolResult callTool(String baseUrl, String clientName, String version,
                                          String toolName, Map<String, Object> params,
                                          Integer requestTimeoutSeconds) {
        return callTool(baseUrl, clientName, version, toolName, params, requestTimeoutSeconds, new HashMap<>(), null);
    }

    /**
     * 获取工具调用结果的文本内容
     *
     * @param result 工具调用结果
     * @return 文本内容列表
     */
    public static List<String> getTextContents(CallToolResult result) {
        return result.content().stream()
                .filter(content -> content instanceof TextContent)
                .map(content -> ((TextContent) content).text())
                .collect(Collectors.toList());
    }

    /**
     * 关闭客户端
     *
     * @param baseUrl 服务地址
     */
    public static void closeClient(String baseUrl) {
        McpSyncClient client = clientCache.remove(baseUrl);
        if (client != null) {
            try {
                client.close();
                log.info("MCP客户端已关闭，服务地址: {}", baseUrl);
            } catch (Exception e) {
                log.warn("关闭MCP客户端异常，服务地址: {}, 异常: {}", baseUrl, e.getMessage());
            }
        }
    }

    /**
     * 关闭所有客户端
     */
    public static void closeAllClients() {
        clientCache.forEach((baseUrl, client) -> {
            try {
                client.close();
                log.info("MCP客户端已关闭，服务地址: {}", baseUrl);
            } catch (Exception e) {
                log.warn("关闭MCP客户端异常，服务地址: {}, 异常: {}", baseUrl, e.getMessage());
            }
        });
        clientCache.clear();
    }

    /**
     * 使用超时机制执行ping操作
     *
     * @param client         MCP客户端
     * @param timeoutSeconds 超时时间（秒）
     * @return ping结果，如果超时或异常则返回null
     */
    private static Object pingWithTimeout(McpSyncClient client, int timeoutSeconds) {
        try {
            // 创建异步任务
            Future<Object> future = THREAD_POOL.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return client.ping();
                }
            });

            // 等待结果，设置超时时间
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("MCP客户端ping操作超时（{}秒）", timeoutSeconds);
            return null;
        } catch (Exception e) {
            log.warn("MCP客户端ping操作异常: {}", e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        List<Tool> tools = McpSseUtils
                .listTools("https://mcp.aone.alibaba-inc.com/sessions/iIyh2HCDLwny7QN4/ali-staff/sse", "agentmagic", "1.0.0", 50, new HashMap<>(), "");

        System.out.println(JSON.toJSONString(tools));
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", "致问");
        CallToolResult callToolResult = McpSseUtils.callTool(
                "https://mcp.aone.alibaba-inc.com/sessions/iIyh2HCDLwny7QN4/ali-staff/sse", "agentmagic", "1.0.0", "get_empid_by_name", params, 30
        );
        System.out.println(callToolResult);
    }

    /**
     * 预处理输入参数，根据Schema定义转换参数类型
     *
     * @param inputParams 输入参数
     * @param schema      Schema定义
     * @return 处理后的参数
     */
    public static Map<String, Object> preProcessInputParams(Map<String, Object> inputParams, JsonSchema schema) {
        return McpParamUtils.preProcessInputParams(inputParams, schema);
    }

    /**
     * 关闭线程池
     * 应在应用程序关闭时调用此方法
     */
    public static void shutdownThreadPool() {
        try {
            THREAD_POOL.shutdown();
            if (!THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                THREAD_POOL.shutdownNow();
                if (!THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("线程池无法终止");
                }
            }
            log.info("McpSseUtils线程池已关闭");
        } catch (InterruptedException e) {
            THREAD_POOL.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("关闭线程池时被中断", e);
        }
    }


}

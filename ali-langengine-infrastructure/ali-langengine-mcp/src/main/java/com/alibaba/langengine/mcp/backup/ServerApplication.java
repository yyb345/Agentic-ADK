///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.mcp;
//
//import com.alibaba.langengine.mcp.server.Server;
//import com.alibaba.langengine.mcp.server.ServerOptions;
//import com.alibaba.langengine.mcp.server.StdioServerTransport;
//import com.alibaba.langengine.mcp.spec.Implementation;
//import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceResult;
//import com.alibaba.langengine.mcp.spec.ServerCapabilities;
//import com.alibaba.langengine.mcp.spec.schema.resources.TextResourceContents;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Arrays;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutionException;
//
//@Slf4j
//public class ServerApplication {
//
//    public static void main(String[] args) throws InterruptedException {
//        Server server = configureServer();
//        StdioServerTransport transport = new StdioServerTransport();
//
//        CompletableFuture<Void> future = server.connect(transport);
//
//        try {
//            future.get();
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//
//        log.info("Server running on stdio");
//
//        CountDownLatch done = new CountDownLatch(1);
//
//        new Thread(() -> {
//            try {
//                // 在这里执行异步任务
////                Thread.sleep(5000);
//                server.setOnCloseCallback(() -> {
//                    done.countDown();
//                });
////                done.countDown();
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//        }).start();
//
//        done.await();
//        log.info("Server closed");
//    }
//
//    public static void main_1(String[] args) {
//        String command = args.length > 0 ? args[0] : "--sse-server-ktor";
////        int port = (args.length > 1 && args[1] != null) ? parseIntOrDefault(args[1], 3001) : 3001;
//
////        if ("--stdio".equals(command)) {
////            runMcpServerUsingStdio();
////        } else if ("--sse-server-ktor".equals(command)) {
////            runSseMcpServerUsingKtorPlugin(port);
////        } else if ("--sse-server".equals(command)) {
////            runSseMcpServerWithPlainConfiguration(port);
////        } else {
////            System.err.println("Unknown command: " + command);
////        }
//
////        Implementation serverInfo = new Implementation();
////        serverInfo.setName("example-server");
////        serverInfo.setVersion("1.0.0");
////
////        ServerCapabilities capabilities = new ServerCapabilities();
////        ServerCapabilities.Resources resources = new ServerCapabilities.Resources(true, true);
////        capabilities.setResources(resources);
////        ServerOptions options = new ServerOptions(capabilities);
////
////        Server server = new Server(serverInfo, options);
////
////        String uri = "/Users/xiaoxuan.lp/works/example.txt";
////        String name = "Example Resource";
////        String description = "An example text file";
////        String mimeType = "text/plain";
////        server.addResource(uri, name, description, mimeType, readResourceRequest -> {
////            List<ResourceContents> contents = new ArrayList<>();
////            TextResourceContents textResourceContents = new TextResourceContents("This is the content of the example resource.", readResourceRequest.getUri(), "text/plain");
////            contents.add(textResourceContents);
////            ReadResourceResult readResourceResult = new ReadResourceResult(contents);
////            return CompletableFuture.supplyAsync(() -> readResourceResult);
////        });
////
////        // Start server with stdio transport
////        StdioServerTransport transport = new StdioServerTransport();
////        CompletableFuture<Void> completableFuture = server.connect(transport);
////        try {
////            completableFuture.get();
////        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
////        } catch (ExecutionException e) {
////            throw new RuntimeException(e);
////        }
//    }
//
//    private static Server configureServer() {
//        CompletableFuture<Void> def = new CompletableFuture<>();
//
//        ServerCapabilities serverCapabilities = new ServerCapabilities();
//        serverCapabilities.setPrompts(new ServerCapabilities.Prompts(true));
//        serverCapabilities.setResources(new ServerCapabilities.Resources(true, true));
//        serverCapabilities.setTools(new ServerCapabilities.Tools(true));
//        Server server = new Server(
//                new Implementation("mcp-kotlin test server", "0.1.0"),
//                new ServerOptions(serverCapabilities)
//        );
//        server.setOnCloseCallback(() -> def.complete(null));
//
////        server.addPrompt(
////                "Java Developer",
////                "Develop small java applications",
////                Arrays.asList(
////                        new PromptArgument(
////                                "Project Name",
////                                "Project name for the new project",
////                                true
////                        )
////                ),
////                request -> {
////                    CompletableFuture<GetPromptResult> resultFuture = new CompletableFuture<>();
////                    GetPromptResult result = new GetPromptResult(
////                            "Description for " + request.getName(),
////                            Arrays.asList(
////                                    new PromptMessage(
////                                            Role.user,
////                                            new TextContent("Develop a kotlin project named <name>" + request.getArguments().get("Project Name") + "</name>")
////                                    )
////                            )
////                    );
////                    resultFuture.complete(result);
////                    return resultFuture;
////                }
////        );
////
////        // Add a tool
////        server.addTool(
////                "Test com.alibaba.langengine.modelcontextprotocol.java.sdk.Tool",
////                "A test tool",
////                new Tool.Input(new JSONObject(), new ArrayList<>()),
////                request -> {
////                    CompletableFuture<CallToolResult> resultFuture = new CompletableFuture<>();
////                    CallToolResult result = new CallToolResult(
////                            Arrays.asList(new TextContent("Hello, world!")), false
////                    );
////                    resultFuture.complete(result);
////                    return resultFuture;
////                }
////        );
//
//        // Add a resource
////        server.addResource(
////                "https://search.com/",
////                "Web Search",
////                "Web search engine",
////                "text/html",
////                request -> {
////                    CompletableFuture<ReadResourceResult> resultFuture = new CompletableFuture<>();
////                    ReadResourceResult result = new ReadResourceResult(
////                            Arrays.asList(
////                                    new TextResourceContents("Placeholder content for " + request.getUri(), request.getUri(), "text/html")
////                            )
////                    );
////                    resultFuture.complete(result);
////                    return resultFuture;
////                }
////        );
//
//        server.addResource(
//                "file:///example.txt",
//                "Example Resource",
//                "An example text file",
//                "text/plain",
//                request -> {
//                    CompletableFuture<ReadResourceResult> resultFuture = new CompletableFuture<>();
//                    ReadResourceResult result = new ReadResourceResult(
//                            Arrays.asList(
//                                    new TextResourceContents("This is the content of the example resource.", request.getUri(),
//                                            "text/plain")
//                            )
//                    );
//                    resultFuture.complete(result);
//                    return resultFuture;
//                }
//        );
//
//        return server;
//    }
//}

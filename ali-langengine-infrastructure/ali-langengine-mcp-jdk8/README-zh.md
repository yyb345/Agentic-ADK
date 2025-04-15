# ali-langengine-mcp-jdk8

## 项目简介

`ali-langengine-mcp-jdk8` 是阿里巴巴 LangEngine 项目的一个独立模块，提供了 Model Context Protocol (MCP) 的 JDK 8 兼容实现。该模块允许在 JDK 8 环境中使用 MCP 协议与 AI 模型进行交互。

## 版本说明

本模块采用独立的版本号管理策略，不与主项目 `ali-langengine` 的版本号保持一致。这是因为：

1. 本模块是完全独立的组件，不依赖于主项目的其他部分
2. 独立的版本号允许本模块按照自己的节奏演进
3. 可以更好地遵循语义化版本控制原则
4. 为依赖本模块的项目提供更清晰的依赖关系
5. 便于针对 JDK 8 环境进行长期维护

当前版本：`1.0.1-SNAPSHOT`

## 功能特性

- 完全兼容 JDK 8 环境
- 实现 Model Context Protocol (MCP) 客户端
- 支持同步和异步 API
- 支持多种传输方式（标准输入/输出、SSE 等）
- 提供工具发现和调用功能
- 支持资源访问和管理
- 支持提示模板处理
- 支持实时更新
- 支持结构化日志记录

## 使用方法

### Maven 依赖

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-mcp-jdk8</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

### 基本用法

```java
// 创建同步客户端
McpSyncClient client = McpClient.createSync(
    StdioClientTransport.create(
        ServerParameters.builder("path/to/server")
            .arg("--some-arg")
            .build()
    )
);

// 初始化客户端
client.initialize();

// 获取可用工具列表
List<Tool> tools = client.listTools();

// 调用工具
CallToolResult result = client.callTool("tool-name", params);
```

## 调试指南

详细的调试信息请参考 [README-DEBUG.md](README-DEBUG.md)。

## 许可证

本项目采用 Apache License 2.0 许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 仓库配置

本模块使用 GitHub 包仓库进行发布：

```xml
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub AIDC-AI Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/AIDC-AI/ali-langengine</url>
    </repository>
    <snapshotRepository>
        <id>github</id>
        <name>GitHub AIDC-AI Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/AIDC-AI/ali-langengine</url>
    </snapshotRepository>
</distributionManagement>
```

## 贡献指南

欢迎提交 Pull Request 或 Issue 来帮助改进本项目。在提交代码前，请确保：

1. 代码符合项目的编码规范
2. 添加了适当的单元测试
3. 所有测试都能通过
4. 更新了相关文档

## 联系方式

如有问题或建议，请通过 GitHub Issue 与我们联系。

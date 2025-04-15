# 调试指南

本文档提供了如何启用详细日志以便调试MCP客户端的说明。

## 启用详细日志

我们提供了两种方式来启用详细日志：

### 方法1：使用提供的脚本

最简单的方法是使用提供的脚本：

```bash
./run-with-debug-logs.sh
```

这将运行WeatherClient示例，并启用详细日志。日志将输出到控制台和`mcp-client-debug.log`文件。

### 方法2：手动设置系统属性

如果您想在自己的应用程序中启用详细日志，可以设置以下系统属性：

```java
System.setProperty("logback.debug", "true");
```

或者在命令行中：

```bash
mvn exec:java -Dexec.mainClass="your.main.Class" -Dlogback.debug=true
```

## 日志级别

我们对不同类型的消息使用不同的日志级别：

- **TRACE**: 最详细的日志，包括所有消息的完整内容
- **DEBUG**: 详细的操作日志，包括连接、消息处理等
- **INFO**: 重要的操作信息
- **WARN**: 警告信息，可能需要注意但不影响核心功能
- **ERROR**: 错误信息，表示操作失败

## 错误处理

对于服务器端的错误消息，我们根据消息内容决定日志级别：

- 常见的非关键错误（如权限问题、文件不存在等）使用DEBUG级别
- 关键错误（包含"Error:"、"Exception:"或"Failed:"）使用WARN级别
- 其他错误信息使用INFO级别

## 注意事项

1. 详细日志可能会产生大量输出，可能会影响性能
2. 在生产环境中，建议禁用详细日志
3. 日志文件`mcp-client-debug.log`会在每次运行时被覆盖，如需保留，请手动备份

## 依赖说明

日志功能使用Logback作为SLF4J的实现。这个依赖被标记为`optional`和`runtime`范围，这意味着：

1. 当其他项目引入本项目时，不会自动包含Logback依赖
2. 如果您的项目已经有其他SLF4J实现，不会产生冲突

如果您需要在自己的项目中使用详细日志，需要手动添加Logback依赖：

```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.11</version>
    <scope>runtime</scope>
</dependency>
```

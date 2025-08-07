# 阿里智能体开发工具包核心 (Ali-Agent ADK Core)

## 项目介绍

阿里智能体开发工具包核心 (Ali-Agent ADK Core) 是一个基于 Java 的智能体框架，构建在阿里巴巴 Smart Engine 工作流引擎之上。该框架为创建具有与大语言模型 (LLM) 和外部工具交互能力的 AI 智能体提供了基础。

该框架利用 RxJava3 实现响应式编程模式，采用基于节点的流程系统来定义智能体行为，支持同步、异步和双向通信模式，为构建复杂的 AI 应用提供了灵活的基础。

## 项目架构概览

```
┌─────────────────────────────────────────────────────────────────────┐
│                          用户应用层                                 │
├─────────────────────────────────────────────────────────────────────┤
│                       Runner (执行入口)                             │
├─────────────────────────────────────────────────────────────────────┤
│                    Pipeline 管道处理层                              │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ Agent执行   │  │  ...           │  │  自定义处理管道          │  │
│  │   Pipe      │  │                │  │                         │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    Flow 流程引擎层                                  │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ FlowCanvas  │  │   FlowNode     │  │  DelegationExecutor     │  │
│  │ (流程容器)   │  │  (流程节点)     │  │  (委托执行器)            │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    AI 能力抽象层                                    │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │  LLM模型    │  │   工具集        │  │  条件判断                │  │
│  │ BasicLlm    │  │  BaseTool      │  │  BaseCondition          │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    基础设施层                                       │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ SmartEngine │  │   RxJava3      │  │  Spring Framework       │  │
│  │ 工作流引擎   │  │ 响应式编程框架  │  │  依赖注入框架            │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## 核心组件介绍

### 1. 流程引擎组件

- **FlowCanvas**: 流程定义的主要容器，用于构建和部署工作流
- **FlowNode**: 所有流程节点的基类，定义了节点的基本行为
- **节点类型**:
  - `LlmFlowNode`: 用于与大语言模型交互
  - `ToolFlowNode`: 用于执行外部工具
  - `ConditionalContainer`: 用于条件分支
  - `ParallelFlowNode`: 用于并行执行
  - `ReferenceFlowNode`: 用于引用其他流程

### 2. 执行组件

- **Runner**: 流程执行的主入口点
- **DelegationExecutor**: 处理委托任务的执行
- **SystemContext**: 包含执行上下文和配置信息
- **Request/Result**: 请求和响应的数据结构

### 3. AI 能力组件

- **BasicLlm 接口及实现** (如 `DashScopeLlm`): 定义和实现与大语言模型的交互
- **LlmRequest/LlmResponse**: 大语言模型交互的数据结构
- **BaseTool 接口及实现** (如 `DashScopeTools`): 定义和实现外部工具的调用

### 4. 管道系统

- **PipeInterface**: 管道组件的接口
- **AgentExecutePipe**: 主要的执行管道实现
- **PipelineUtil**: 管道执行的工具类

## 使用指南

### 快速开始

要开始使用 Ali-Agent ADK Core，请按照以下步骤操作：

1. 添加 Maven 依赖：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-core</artifactId>
    <version>${ali-agentic-adk.version}</version>
</dependency>
```

2. 创建 Spring Boot 应用并添加组件扫描：
```java
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic.core"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 基本用法示例

以下是一些基于测试用例的使用示例：

#### 1. 创建简单的 LLM 调用流程

```java
@Test
public void testLlmGraph() throws InterruptedException {
    FlowCanvas flowCanvas = new FlowCanvas();

    // 创建 LLM 请求
    LlmRequest llmRequest = new LlmRequest();
    llmRequest.setModel("dashscope");
    llmRequest.setModelName("qwen-plus");
    llmRequest.setMessages(List.of(new LlmRequest.Message("user", "你好，请介绍一下你自己。20字以内")));

    // 创建 LLM 节点
    LlmFlowNode llmNode = new LlmFlowNode(llmRequest);
    llmNode.setId("llmNode1");
    
    flowCanvas.setRoot(llmNode);

    // 执行流程
    Request request = new Request().setInvokeMode(InvokeMode.SYNC);
    Flowable<Result> flowable = new Runner().run(flowCanvas, request);

    // 处理结果
    List<Result> results = new ArrayList<>();
    flowable.blockingIterable().forEach(results::add);
}
```

#### 2. 创建工具调用流程

```java
@Test
public void testToolGraph() {
    FlowCanvas flowCanvas = new FlowCanvas();

    flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
            .setName("name").setValue("value")), new BaseTool() {
        @Override
        public String name() {
            return "testToolNode";
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            return Flowable.just(Map.of("text", args.get("name")));
        }
    }).next(new ToolFlowNode("dash_scope_tool",
            List.of(new ToolParam().setName("appId").setValue("your-app-id"), 
                   new ToolParam().setName("apiKey").setValue("your-api-key"), 
                   new ToolParam().setName("prompt").setValue("给我生成一份教案，教学内容是数学三年级上册的时分秒, 20字以内")))));

    Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
    flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
}
```

#### 3. 创建条件分支流程

```java
@Test
public void testConditionalGraph() {
    FlowCanvas flowCanvas = new FlowCanvas();

    flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
            .setName("name").setValue("value")), new BaseTool() {
        @Override
        public String name() {
            return "testToolNode";
        }
        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            return Flowable.just(Map.of("text", args.get("name")));
        }
    }).setId("myId").nextOnCondition(new ConditionalContainer() {
        @Override
        public Boolean eval(SystemContext systemContext) {
            return false; // 条件判断
        }
    }.setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
        @Override
        public String name() {
            return "useResult1";
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
            return Flowable.just(Map.of("newText1", myIdText));
        }
    }).setId("first tool"))).nextOnCondition(new ConditionalContainer() {
        @Override
        public Boolean eval(SystemContext systemContext) {
            return false; // 条件判断
        }
    }.setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
        @Override
        public String name() {
            return "useResult2";
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
            return Flowable.just(Map.of("newText2", myIdText));
        }
    }).setId("second tool"))).nextOnElse(new ToolFlowNode(List.of(), new BaseTool() {
        @Override
        public String name() {
            return "useResult3";
        }
        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
            return Flowable.just(Map.of("newText3", myIdText));
        }
    }).setId("third tool")));

    Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
    flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
}
```

## 配置说明

### 应用配置

在 `application.properties` 中配置必要的参数：

```properties
# Redis 配置（用于流程存储）
ali.agentic.adk.properties.redisHost=your-redis-host
ali.agentic.adk.properties.redisPort=6379
ali.agentic.adk.properties.redisPassword=your-redis-password
ali.agentic.adk.properties.redisKeyPrefix=your-key-prefix
ali.agentic.adk.properties.flowStorageStrategy=redis

# DashScope API Key
ali.agentic.adk.flownode.dashscope.apiKey=your-dashscope-api-key
```

### 服务注册

在 `META-INF/services/` 目录下创建服务注册文件：

1. `com.alibaba.agentic.core.models.BasicLlm`:
```
com.alibaba.agentic.core.models.DashScopeLlm
```

2. `com.alibaba.agentic.core.tools.BaseTool`:
```
com.alibaba.agentic.core.tools.DashScopeTools
```

## 执行模式

框架支持三种执行模式：

1. **SYNC (同步模式)**: 顺序执行，等待每个节点完成后再执行下一个
2. **ASYNC (异步模式)**: 异步执行，可以并行处理多个任务
3. **BIDI (双向模式)**: 支持双向通信，可以动态接收输入

## 扩展开发

### 自定义 LLM 模型

实现 `BasicLlm` 接口来集成新的 LLM 模型：

```java
public class CustomLlm implements BasicLlm {
    @Override
    public String model() {
        return "custom-model";
    }

    @Override
    public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
        // 实现调用逻辑
    }
}
```

### 自定义工具

实现 `BaseTool` 接口来创建新的工具：

```java
public class CustomTool implements BaseTool {
    @Override
    public String name() {
        return "custom-tool";
    }

    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        // 实现工具逻辑
        return Flowable.just(Map.of("result", "success"));
    }
}
```
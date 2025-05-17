# 大型语言模型JSON修复工具

## 概述

`ali-langengine-jsonrepair`包提供了强大的工具，用于处理和修复大型语言模型（LLMs）常常生成的格式不正确的JSON。在与LLMs交互时，由于各种问题如缺少逗号、括号不平衡或引号使用不正确等，JSON解析错误经常发生。本工具包提供了优雅处理这些问题的解决方案。

## 核心组件

### 1. JsonSafeParser

`JsonSafeParser`是一个工具类，提供了安全解析潜在格式不正确的JSON字符串的方法。它实现了多层次的解析策略，以确保成功解析：

1. 首先尝试使用FastJSON直接解析
2. 如果失败，尝试使用`JsonRepair`修复JSON后再解析
3. 处理类型不匹配的情况（例如，当JSON对象被错误地解析为数组时）
4. 回退到手动修复常见的JSON问题
5. 始终返回有效结果（如果所有尝试都失败，则返回空对象/数组）

**主要方法：**
- `parseObject(String jsonStr)`：安全地将JSON字符串解析为JSONObject
- `parseArray(String jsonStr)`：安全地将JSON字符串解析为JSONArray
- `getValidJsonString(String jsonStr)`：返回有效的JSON字符串，必要时进行修复

### 2. JsonRepair

`JsonRepair`是一个专门用于修复格式不正确的JSON字符串的工具。它可以处理各种常见的JSON错误，包括：

- 键周围缺少引号
- 元素之间缺少逗号
- 数组和对象中的尾随逗号
- 使用单引号而不是双引号
- 注释（包括//和/*风格）
- 未加引号的字符串字面量

**主要方法：**
- `repairJson(String jsonStr)`：修复格式不正确的JSON字符串并返回修复后的JSON字符串
- `loads(String jsonStr)`：修复并解析JSON字符串，返回解析后的对象
- `fromFile(String filename)`：从文件加载并修复JSON

## 使用示例

### JsonSafeParser基本用法

```java
import com.alibaba.langengine.jsonrepair.JsonSafeParser;
import com.alibaba.fastjson.JSONObject;

// 将可能格式不正确的JSON解析为JSONObject
String llmOutput = "{ \"name\": \"John\" \"age\": 30 }"; // 缺少逗号
JSONObject result = JsonSafeParser.parseObject(llmOutput);
System.out.println(result); // 正确解析的JSONObject

// 解析为数组
String arrayOutput = "[ {\"item\": \"one\"}, {\"item\": \"two\", ]"; // 尾随逗号
JSONArray array = JsonSafeParser.parseArray(arrayOutput);
System.out.println(array); // 正确解析的JSONArray

// 获取有效的JSON字符串
String fixedJson = JsonSafeParser.getValidJsonString(llmOutput);
System.out.println(fixedJson); // "{"name":"John","age":30}"
```

### 直接使用JsonRepair

```java
import com.alibaba.langengine.jsonrepair.JsonRepair;
import com.alibaba.fastjson.JSON;

// 修复格式不正确的JSON
String malformedJson = "{ 'name': 'John', 'age': 30 }"; // 使用单引号而不是双引号
String repairedJson = JsonRepair.repairJson(malformedJson);
System.out.println(repairedJson); // "{"name":"John","age":30}"

// 直接修复并解析
Object parsed = JsonRepair.loads(malformedJson);
System.out.println(parsed); // 解析后的对象
```

## 处理的常见错误情况

1. **缺少逗号**
   ```
   { "name": "John" "age": 30 }
   ```

2. **尾随逗号**
   ```
   { "items": [1, 2, 3, ] }
   ```

3. **JSON中的注释**
   ```
   { 
     "name": "John", // 这是一个注释
     /* 这是一个块注释 */
     "age": 30
   }
   ```

4. **单引号**
   ```
   { 'name': 'John', 'age': 30 }
   ```

5. **括号不平衡**
   ```
   { "name": "John", "items": [1, 2, 3 }
   ```

6. **类型不匹配**
   - 当JSON对象被错误地解析为数组时
   - 当JSON数组被错误地解析为对象时

## 适用场景

这个库在以下情况特别有用：

1. 处理应该是JSON格式的LLM输出
2. 为LLM响应实现JSON输出解析器
3. 需要确保与LLMs交互的应用程序中的JSON处理具有鲁棒性
4. 处理可能包含语法错误的用户生成的JSON

## 依赖项

- 阿里巴巴FastJSON
- Lombok（用于日志记录）

## 实现细节

实现遵循渐进式的JSON修复方法：

1. **直接解析**：首先尝试使用标准FastJSON方法解析
2. **修复并解析**：如果直接解析失败，使用JsonRepair修复常见问题
3. **类型纠正**：处理JSON结构类型与预期不匹配的情况
4. **手动修复**：应用针对常见错误模式的特定修复
5. **回退策略**：如果所有修复尝试都失败，返回空但有效的JSON结构

这种多层次的方法确保在处理来自LLMs的格式不正确的JSON时具有最大的弹性。

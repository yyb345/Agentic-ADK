/**
 * Copyright (C) 2024 AIDC-AI
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
package com.alibaba.langengine.demo.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.structured.StructuredChatGlmOutputParser;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ToolLoaders;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ConversationalAgentTest {

    @Test
    public void test_run() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        ConversationBufferMemory memory = new ConversationBufferMemory();
        memory.setMemoryKey("chat_history");

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "BingWebSearchAPI" }), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeConversationalAgent(baseTools, llm, memory, false);

        Map<String, Object> response;
        Map<String, Object> inputs = new HashMap<>();

        inputs.put("input", "你好，我是萧玄");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));

        inputs.put("input", "我的名字是什么");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));

        inputs.put("input", "我喜欢的城市是杭州");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));

        inputs.put("input", "请问我喜欢的城市 明天的天气怎么样？");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));
    }

    @Test
    public void test() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();

//        ConversationBufferMemory memory = new ConversationBufferMemory();
//        memory.setMemoryKey("chat_history");

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "ApiLogTool"}), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeStructredAgentWithParser(baseTools, llm,new StructuredChatGlmOutputParser());

        Map<String, Object> response;
        Map<String, Object> inputs = new HashMap<>();

        inputs.put("question", "我有一个请求,requestId=16lsxxx,请问具体的调用详情能告诉我么？");
//        inputs.put("question", "你是谁");
//        inputs.put("question", "我有一个API调用，请问具体的调用详情能告诉我么？");

        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));

//        inputs.put("input", "what's my name?");
//        inputs.put("input", "我有一个请求，requestId=16lsxxx，请问具体的调用详情能告诉我么？");
//        response = agentExecutor.run(inputs);
//        System.out.println(JSON.toJSONString(response.get("output")));
//
////        inputs.put("input", "what are some good dinners to make this week, if i like thai food?");
//        inputs.put("input", "如果我喜欢泰国菜，这周有哪些好吃的晚餐？");
//        response = agentExecutor.run(inputs);
//        System.out.println(JSON.toJSONString(response.get("output")));

//        inputs.put("input", "tell me the last letter in my name, and also tell me who won the world cup in 1978?");
//        response = agentExecutor.run(inputs);
//        System.out.println(JSON.toJSONString(response.get("output")));
    }

    @Test
    public void test2() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        ConversationBufferMemory memory = new ConversationBufferMemory();
        memory.setMemoryKey("chat_history");

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "BingWebSearchAPI" }), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeConversationalAgent(baseTools, llm, memory);

        Map<String, Object> response;
        Map<String, Object> inputs = new HashMap<>();

        inputs.put("input", "你好，我是萧玄");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));

        inputs.put("input", "我的名字是什么");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));

//        inputs.put("input", "我想买一件衬衫，能给一些建议吗？");
//        response = agentExecutor.run(inputs);
//        System.out.println(JSON.toJSONString(response.get("output")));

        inputs.put("input", "明天杭州的天气怎么样？");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));
    }
}

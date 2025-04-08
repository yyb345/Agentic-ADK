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
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.callback.StdOutCallbackHandler;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ToolLoaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * 能够使用多输入工具，结构化的参数输入
 *
 * @author xiaoxuan.lp
 */
public class StructuredChatAgentTest {

    @Test
    public void test_OpenAI() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "create_application", "post_message" }), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "我想生成一个性别为男性并且在180天访问过淘特的人群?");
        inputs.put("input", "我想要一个退货登记的应用程序，里面包括有用户信息，订单号，用户地址这些字段。");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_OpenAI_2() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "create_application", "llm-math-advance" /*, "post_message"*/ }), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "我想生成一个性别为男性并且在180天访问过淘特的人群?");
        inputs.put("input", "我想要一个退货登记的应用程序，里面包括有用户信息，订单号，用户地址这些字段，然后计算有多少个字段，以及字段数量的0.43次方是多少");
        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_OpenAI_serialize() throws JsonProcessingException {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "create_application", "post_message" }), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredAgent(baseTools, llm);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "我想要一个退货登记的应用程序，里面包括有用户信息，订单号，用户地址这些字段。");

        String agentJson = agentExecutor.serialize();
        AgentExecutor newAgentExecutor = JacksonUtils.MAPPER.readValue(agentJson, AgentExecutor.class);
        System.out.println(newAgentExecutor);

        Map<String, Object> response = agentExecutor.call(inputs);
        System.out.println("response1:" + JSON.toJSONString(response));

        response = newAgentExecutor.call(inputs);
        System.out.println("response2:" + JSON.toJSONString(response));
    }

    @Test
    public void test_OpenAI_callbackManager() {
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.addHandler(new StdOutCallbackHandler());

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = ToolLoaders.loadLools(Arrays.asList(new String[] { "create_application", "post_message" }), llm);
        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredAgent(baseTools, llm);
        agentExecutor.setCallbackManager(callbackManager);

        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "我想生成一个性别为男性并且在180天访问过淘特的人群?");
        inputs.put("input", "我想要一个退货登记的应用程序，里面包括有用户信息，订单号，用户地址这些字段。");
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setInputs(inputs);
        executionContext.setChain(agentExecutor);

        Map<String, Object> response = agentExecutor.run(inputs, executionContext, null, null);
        System.out.println(JSON.toJSONString(response));
    }
}

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
package com.alibaba.langengine.core.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.callback.StdOutCallbackHandler;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class LLMChainTest {

    @Test
    public void test_run() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.9d);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("What is a good name for a company that makes {product}?");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("product", "colorful socks");
        Map<String, Object> result = chain.run(inputs);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_run_serialize() throws JsonProcessingException {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.9d);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("What is a good name for a company that makes {product}?");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("product", "colorful socks");

        String chainJson = chain.serialize();
        LLMChain newChain = JacksonUtils.MAPPER.readValue(chainJson, LLMChain.class);

        Map<String, Object> result = chain.run(inputs);
        System.out.println("result1:" + JSON.toJSONString(result));

        result = newChain.run(inputs);
        System.out.println("result2:" + JSON.toJSONString(result));
    }

    @Test
    public void test_run_callbackManager() {
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.addHandler(new StdOutCallbackHandler());

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("What is a good name for a company that makes {product}?");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);
        chain.setCallbackManager(callbackManager);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("product", "colorful socks");
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setChain(chain);
        executionContext.setInputs(inputs);
        Map<String, Object> result = chain.run(inputs, executionContext, null, null);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_run2() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.9d);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("What is a good name for a company that makes {product}?");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("product", "colorful socks");
        Map<String, Object> result = chain.run(inputs);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_run3() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setStream(true);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("{question}");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);

        String response = chain.chat("你是谁？");
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_chatgpt_run() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.0d);

        PromptTemplate prompt = new PromptTemplate();
        prompt.setTemplate("已知信息：\n" +
                "{context}" +
                "\n" +
                "根据上述已知信息，简洁和专业的来回答用户的问题，请选择最匹配的一条信息。如果无法从中得到答案，请说 " +
                "“根据已知信息无法回答该问题” 或 “没有提供足够的相关信息”，不允许在答案中添加编造成分，答案请使用中文。 问题是：\n" +
                "{input}");

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);

        Map<String, Object> inputs = new HashMap<>();
        //知识库上下文
        inputs.put("context",
                "1、淘宝开放平台创建应用，可以参考：https://www.atatech.org/articles/112988 。\n" +
                "2、top接口传数据是有大小限制，API调用请求最大报文限制在10M以内，这个是nginx限制的要求。\n");
        //prompt
        inputs.put("input", "开放平台如何创建应用？");
        Map<String, Object> response = chain.run(inputs);
        System.out.println(response.get("text"));
    }
}

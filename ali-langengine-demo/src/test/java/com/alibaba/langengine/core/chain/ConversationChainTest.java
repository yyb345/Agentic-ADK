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
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.demo.vectorstore.BeanConfiguration;
import com.alibaba.langengine.hologres.vectorstore.HologresConfig;
import com.alibaba.langengine.hologres.vectorstore.HologresDB;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BeanConfiguration.class, HologresConfig.class})
public class ConversationChainTest {

    @Resource
    private HologresDB hologresDB;

    @Test
    public void test_OpenAI_ConversationBufferMemory() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.0d);

        ConversationChain conversation = new ConversationChain();
        conversation.setLlm(llm);
        conversation.setVerbose(true);
        conversation.setMemory(new ConversationBufferMemory());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Hi there!");
        Map<String, Object> response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "I'm doing well! Just having a conversation with an AI.");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "Tell me about yourself.");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_OpenAI_ConversationBufferMemory_serialize() throws JsonProcessingException {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.0d);

        ConversationChain conversation = new ConversationChain();
        conversation.setLlm(llm);
        conversation.setVerbose(true);
        conversation.setMemory(new ConversationBufferMemory());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Hi there!");
        Map<String, Object> response = conversation.predict(inputs);
        System.out.println("response1:" + JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "I'm doing well! Just having a conversation with an AI.");
        response = conversation.predict(inputs);
        System.out.println("response1:" + JSON.toJSONString(response));

        String chainJson = conversation.serialize();
        ConversationChain newConversation = JacksonUtils.MAPPER.readValue(chainJson, ConversationChain.class);

        inputs = new HashMap<>();
        inputs.put("input", "Tell me about yourself.");
        response = newConversation.predict(inputs);
        System.out.println("response2:" + JSON.toJSONString(response));
    }

    @Test
    public void test_OpenAI_ConversationBufferMemory_callbackManager() {
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.addHandler(new StdOutCallbackHandler());

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);

        ConversationChain conversation = new ConversationChain();
        conversation.setLlm(llm);
        conversation.setVerbose(true);
        conversation.setMemory(new ConversationBufferMemory());
        conversation.setCallbackManager(callbackManager);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Hi there!");
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setChain(conversation);
        executionContext.setInputs(inputs);
        Map<String, Object> response = conversation.predict(inputs, executionContext, null);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "I'm doing well! Just having a conversation with an AI.");
        executionContext.setInputs(inputs);
        response = conversation.predict(inputs, executionContext, null);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "Tell me about yourself.");
        executionContext.setInputs(inputs);
        response = conversation.predict(inputs, executionContext, null);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_ChatOpenAI_ConversationBufferMemory() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.0d);

        ConversationChain conversation = new ConversationChain();
        conversation.setLlm(llm);
        conversation.setVerbose(true);
        conversation.setMemory(new ConversationBufferMemory());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Hi there!");
        Map<String, Object> response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "I'm doing well! Just having a conversation with an AI.");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "Tell me about yourself.");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_ConversationBufferMemory() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.0d);

        ConversationChain conversation = new ConversationChain();
        conversation.setLlm(llm);
        conversation.setVerbose(true);
        conversation.setMemory(new ConversationBufferMemory());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "假设你是一个小朋友，接下来我将你对对联，只需要答下一句，不要有多余的描述和联想。当我问：云，你就回答：雨， 当我问：雪，你回答：风。");
        Map<String, Object> response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "云");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "花");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "火");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));
    }
}

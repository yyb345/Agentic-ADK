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
package com.alibaba.langengine.core.memory;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConversationBufferMemoryTest {

    @Test
    public void test_loadMemoryVariables() {
        // success
        ConversationBufferMemory memory = new ConversationBufferMemory();
        memory.getChatMemory().addUserMessage("hi!");
        memory.getChatMemory().addAIMessage("what's up?");
        System.out.println(JSON.toJSONString(memory.loadMemoryVariables(new HashMap<>())));

        memory = new ConversationBufferMemory();
        memory.setReturnMessages(true);
        memory.getChatMemory().addUserMessage("hi!");
        memory.getChatMemory().addAIMessage("what's up?");
        System.out.println(JSON.toJSONString(memory.loadMemoryVariables(new HashMap<>())));
    }

    @Test
    public void test_addLLMChain() {
        // success
        String template = "You are a chatbot having a conversation with a human. \n\n{chat_history} \nHuman: {human_input} \nAI:";

        PromptTemplate prompt = new PromptTemplate();
        prompt.setInputVariables(Arrays.asList(new String[] { "chat_history", "human_input" }));
        prompt.setTemplate(template);
        ConversationBufferMemory memory = new ConversationBufferMemory();
        memory.setMemoryKey("chat_history");

        FakeAI openAI = new FakeAI();
        LLMChain llm_chain = new LLMChain();
        llm_chain.setLlm(openAI);
        llm_chain.setPrompt(prompt);
        llm_chain.setMemory(memory);
        llm_chain.setVerbose(true);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("human_input", "Hi there my friend");
        System.out.println(JSON.toJSONString(llm_chain.predict(inputs)));

        inputs = new HashMap<>();
        inputs.put("human_input", "Not too bad - how are you?");
        System.out.println(JSON.toJSONString(llm_chain.predict(inputs)));
    }
}

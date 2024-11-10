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
import com.alibaba.langengine.core.agent.expert.ExpertChain;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ExpertChainTest {

    @Test
    public void test_run() {
        // success
        ChatOpenAI chatOpenAI = new ChatOpenAI();
        chatOpenAI.setTemperature(0d);
        chatOpenAI.setMaxTokens(1024);

        ExpertChain chain = new ExpertChain();
        chain.setLlm(chatOpenAI);
        chain.setExpertLlm(chatOpenAI);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", "Describe the structure of an atom.");
        Map<String, Object> result = chain.run(inputs);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_openai_run() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        ExpertChain chain = new ExpertChain(true);
        chain.setLlm(llm);
        chain.setExpertLlm(llm);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", "介绍一下淘宝");
        Map<String, Object> result = chain.run(inputs);
        System.out.println(result.get("text"));
    }
}

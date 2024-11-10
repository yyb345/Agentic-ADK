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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author aihe.ah
 * @time 2023/9/20
 * 功能说明：
 */
public class LLMRequestsChainTest {

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRequestChain() throws Exception {
        // success
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(new FakeAI());
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate("帮我总结如下的内容：\n"
            + "{requests_result} ");
        promptTemplate.setInputVariables(Lists.newArrayList("requests_result"));

        llmChain.setPrompt(promptTemplate);

        LLMRequestsChain llmRequestsChain = new LLMRequestsChain();
        llmRequestsChain.setLlmChain(llmChain);
        HashMap<String, Object> inputs = new HashMap<>();
        inputs.put(LLMRequestsChain.INPUT_KEY, "https://jsoup.org/");
        Map<String, Object> run = llmRequestsChain.run(inputs);
        System.out.println(JSON.toJSONString(run));
    }
}
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

import com.alibaba.langengine.dashscope.model.DashScopeLLM;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

public class DataGenerationChainTest {

    @Test
    public void test_run() {
        ChatOpenAI llm = new ChatOpenAI();
        DataGenerationChain chain = new DataGenerationChain();
        chain.setLlm(llm);

        Map<String, Object> inputs = new HashMap<>();
        //        inputs.put("fields", "[\"blue\", \"yellow\"]");
        //        inputs.put("preferences", "{\"style\": \"Make it in a style of a weather forecast.\"}");
        inputs.put("fields", "[\"红色\", \"蓝色\"]");
        inputs.put("preferences", "{\"style\": \"生成一个以上的颜色元素的化妆品宣传文案\"}");
        Map<String, Object> result = chain.run(inputs);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_run_withField() {
        String token = System.getenv("DASH_SCOPE_API");
        DashScopeLLM llm = new DashScopeLLM(token);
        DataGenerationChain chain = new DataGenerationChain();
        chain.setLlm(llm);

        //        inputs.put("fields", "[\"blue\", \"yellow\"]");
        //        inputs.put("preferences", "{\"style\": \"Make it in a style of a weather forecast.\"}");
        //inputs.put("fields", "[\"红色\", \"蓝色\"]");
        //inputs.put("preferences", "{\"style\": \"生成一个以上的颜色元素的化妆品宣传文案\"}");
        chain.setFields(Lists.newArrayList("红色", "蓝色"));
        HashMap<String, Object> preferences = new HashMap<>();
        preferences.put("style", "生成一个以上的颜色元素的化妆品宣传文案");
        chain.setPreferences(preferences);
        Map<String, Object> result = chain.generateSentence();
        System.out.println(JSON.toJSONString(result));
    }
}

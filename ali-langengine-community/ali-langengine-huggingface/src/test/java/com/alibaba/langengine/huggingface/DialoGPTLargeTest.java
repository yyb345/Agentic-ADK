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
package com.alibaba.langengine.huggingface;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class DialoGPTLargeTest {

    @Test
    public void test_predict() {
        // success
        DialoGPTLarge llm = new DialoGPTLarge();
        long start = System.currentTimeMillis();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("past_user_inputs", new String[] { "Which movie is the best ?" });
        inputs.put("generated_responses", new String[] { "It is Die Hard for sure." });
        inputs.put("text", "Can you explain why ?");
        System.out.println("response:" + llm.predict(JSON.toJSONString(inputs)));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

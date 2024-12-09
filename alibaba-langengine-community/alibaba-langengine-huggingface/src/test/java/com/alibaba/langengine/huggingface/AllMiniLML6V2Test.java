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

public class AllMiniLML6V2Test {

    @Test
    public void test_predict() {
        // success
        AllMiniLML6V2 llm = new AllMiniLML6V2();
        long start = System.currentTimeMillis();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("source_sentence", "That is a happy person");
        inputs.put("sentences", new String[] { "That is a happy dog", "That is a very happy person", "Today is a sunny day" });
        System.out.println("response:" + llm.predict(JSON.toJSONString(inputs)));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

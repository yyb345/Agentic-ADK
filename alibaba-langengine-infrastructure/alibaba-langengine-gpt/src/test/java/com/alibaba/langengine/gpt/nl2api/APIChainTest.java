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
package com.alibaba.langengine.gpt.nl2api;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

public class APIChainTest {

    @Test
    public void testApi() {
        // success
        ChatOpenAI llm = new ChatOpenAI();

        String apiDocs = "BASE URL: http://localhost:7001/countryInfo\n"
            + "\n"
            + "API Documentation:\n"
            + "\n"
            + "The API endpoint /capital/{name} Used to find capital of a country. All URL parameters are "
            + "listed below:\n"
            + "    - name: Name of country - Ex: america, france, china\n"
            + "    \n"
            + "The API endpoint /currency/{name} Used to find currency of a country. All URL parameters"
            + " are listed below:\n"
            + "    - name: Name of country - Ex: america, france, china\n"
            + "    \n"
            + "The API endpoint /population/{name} Used to find population of a country. All URL parameters"
            + " are listed below:\n"
            + "    - name: Name of country - Ex: america, france, china\n"
            + "    \n"
            + "Woo! This is my documentation";
        APIChain apiChain = APIChain.fromLlmAndApiDocs(llm, apiDocs, null);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put(APIChain.QUESTION_KEY, "法国的人口有多少?");
        // 若为false，则直接返回接口response
        inputs.put(APIChain.NEED_SUMMARY_KEY, true);
        System.out.println(JSON.toJSONString(apiChain.run(inputs)));
    }
}

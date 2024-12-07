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
package com.alibaba.langengine.core.chain.llmbash;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class LLMBashChainTest {

    @Test
    public void test_run() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);

        LLMBashChain llmBashChain = LLMBashChain.fromLlm(llm);
        llmBashChain.setWorkingDirectoryPath("/Users/xiaoxuan.lp/works/sources");

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", "请编写一个 bash 脚本，将“Hello World”打印到控制台。");
//        inputs.put("question", "列出当前目录，然后向上移动一个级别。");
        Map<String, Object> result = llmBashChain.run(inputs);
        System.out.println(JSON.toJSONString(result));
    }
}

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
package com.alibaba.langengine.core.chain.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMRouterChainTest {

    @Test
    public void test_run() {
        String physicsTemplate = "You are a very smart physics professor. " +
                "You are great at answering questions about physics in a concise and easy to understand manner. " +
                "When you don't know the answer to a question you admit that you don't know. Please answer in Chinese.\n" +
                "\n" +
                "Here is a question:\n" +
                "{input}";
        String mathTemplate = "You are a very good mathematician. You are great at answering math questions. " +
                "You are so good because you are able to break down hard problems into their component parts, " +
                "answer the component parts, and then put them together to answer the broader question.Please answer in Chinese.\n" +
                "\n" +
                "Here is a question:\n" +
                "{input}";
        String chineseTemplate = "You are a very good Chinese teacher. You are very good at answering Chinese questions.Please answer in Chinese.\n" +
                "\n" +
                "Here is a question:\n" +
                "{input}";
        List<Map<String, Object>> promptInfos = new ArrayList<>();
        Map<String, Object> pInfo;
        pInfo = new HashMap<>();
        pInfo.put("name", "physics");
        pInfo.put("description", "Good for answering questions about physics");
        pInfo.put("prompt_template", physicsTemplate);
        promptInfos.add(pInfo);
        pInfo = new HashMap<>();
        pInfo.put("name", "math");
        pInfo.put("description", "Good for answering questions about math");
        pInfo.put("prompt_template", mathTemplate);
        promptInfos.add(pInfo);
        pInfo = new HashMap<>();
        pInfo.put("name", "chinese");
        pInfo.put("description", "Good for answering questions about chinese");
        pInfo.put("prompt_template", chineseTemplate);
        promptInfos.add(pInfo);

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        MultiPromptChain multiPromptChain = MultiPromptChain.fromPrompts(llm, promptInfos);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "What is black body radiation?");
//        inputs.put("input", "What is the first prime number greater than 40 such that one plus the prime number is divisible by 3");
//        inputs.put("input", "What is 1 plus 1 equal to?");
//        inputs.put("input", "Which one hits the ground first, an iron ball or a tennis ball of the same size?");
//        inputs.put("input", "帮我把\"how are you\"这句话翻译成中文");
//        inputs.put("input", "介绍一下什么是惯性定律");
        inputs.put("input", "3的二次方是多少");
//        inputs.put("input", "Nice to meet you的中文是怎么说？");

        Map<String, Object> response = multiPromptChain.run(inputs);
        System.out.println("text:" + JSON.toJSONString(response.get("text")));
    }
}

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
package com.alibaba.langengine.demo.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.ConversationChain;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 一种带记忆功能的链,它可以记住用户与AI之前的所有的对话内容,
 * 把它们作为上下文传递给下一次对话,这样 AI 可以根据这个上下文更好的与用户进行对话
 *
 * @author xiaoxuan.lp
 */
public class ConversationChainTest {

    @Test
    public void test_run() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

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
        response = conversation.run(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "花");
        response = conversation.run(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "火");
        response = conversation.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }
}

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
package com.alibaba.langengine.sqlite.memory.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.ConversationChain;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.sqlite.memory.ConversationSqliteMemory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SqliteConfig.class})
public class ConversationSqliteMemoryTest {

    @Resource
    private SqliteCache sqliteCache;

    @Test
    public void test_ConversationSqliteMemory() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0.0d);

        ConversationSqliteMemory memory = new ConversationSqliteMemory(sqliteCache, UUID.randomUUID().toString());

        ConversationChain conversation = new ConversationChain();
        conversation.setLlm(llm);
        conversation.setVerbose(true);
        conversation.setMemory(memory);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "假设你是一个小朋友，接下来我将你对对联，只需要答下一句，不要有多余的描述和联想。当我问：云，你就回答：雨， 当我问：雪，你回答：风。");
        Map<String, Object> response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "云");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "花");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

        inputs = new HashMap<>();
        inputs.put("input", "火");
        response = conversation.predict(inputs);
        System.out.println(JSON.toJSONString(response));

//        ConversationSqliteMemory newMemory = new ConversationSqliteMemory(sqliteCache, "1");
//        conversation.setMemory(newMemory);
//        inputs = new HashMap<>();
//        inputs.put("input", "水");
//        response = conversation.predict(inputs);
//        System.out.println(JSON.toJSONString(response));
    }
}

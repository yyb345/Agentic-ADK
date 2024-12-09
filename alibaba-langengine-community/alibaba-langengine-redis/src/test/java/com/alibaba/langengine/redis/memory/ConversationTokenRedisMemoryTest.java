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
package com.alibaba.langengine.redis.memory;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.ConversationChain;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.redis.memory.cache.AiRedisTemplate;
import com.alibaba.langengine.redis.memory.cache.RedisCache;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

/**
 * TODO: 待优化
 * @author aihe.ah
 * @time 2023/11/13
 * 功能说明：
 */
public class ConversationTokenRedisMemoryTest {

    @Test
    public void testRdbTokenCount() {
        // 需要本地启动一个redis服务器
        JedisPool jedisPool = new JedisPool();
        try {
            AiRedisTemplate aiRedisTemplate = new AiRedisTemplate(jedisPool);

            RedisCache redisCache = new RedisCache(aiRedisTemplate);
            redisCache.setRedisExpireTimeSeconds(60 * 60 * 24 * 7);

            ConversationTokenRedisMemory windowRedisMemory =
                new ConversationTokenRedisMemory(redisCache, "test", 100);

            ConversationChain conversation = new ConversationChain();
            ChatOpenAI llm = new ChatOpenAI();
            conversation.setLlm(llm);
            conversation.setVerbose(true);
            conversation.setMemory(windowRedisMemory);

            HashMap<String, Object> inputs = new HashMap<>();
            inputs.put("input",
                "假设你是一个小朋友，接下来我将你对对联，只需要答下一句，不要有多余的描述和联想。当我问：云，你就回答：雨， 当我问：雪，你回答：风。");
            Map<String, Object> response = conversation.predict(inputs);
            System.out.println(response);

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

        } finally {
            jedisPool.close();
        }

    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
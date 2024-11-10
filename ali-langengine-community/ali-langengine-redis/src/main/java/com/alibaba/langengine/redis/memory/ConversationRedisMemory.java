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

import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.redis.memory.cache.RedisCache;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 用于存储会话记忆的redis
 *
 * @author liuchunhe.lch on 2023/9/2 09:52
 */
@Data
public class ConversationRedisMemory extends BaseChatMemory {

    private BaseChatMessageHistory chatMemory;

    public ConversationRedisMemory(RedisCache redisCache) {
        this(redisCache, null);
    }

    public ConversationRedisMemory(RedisCache redisCache, String sessionId) {
        ChatMessageRedisHistory chatMessageRedisHistory = new ChatMessageRedisHistory();
        chatMessageRedisHistory.setRedisCache(redisCache);
        if(!StringUtils.isEmpty(sessionId)) {
            chatMessageRedisHistory.setSessionId(sessionId);
        } else {
            chatMessageRedisHistory.setSessionId(UUID.randomUUID().toString());
        }
        chatMemory = chatMessageRedisHistory;
    }

    /**
     * 将始终返回内存变量列表
     *
     * @return
     */
    @Override
    public List<String> memoryVariables() {
        return Arrays.asList(new String[] { getMemoryKey() });
    }

    @Override
    public Map<String, Object> loadMemoryVariables(Map<String, Object> inputs) {
        Map<String, Object> map = new HashMap<>();
        map.put(getMemoryKey(), buffer());
        return map;
    }

    public Object buffer() {
        if(isReturnMessages()) {
            return getChatMemory().getMessages();
        } else {
            return MessageConverter.getBufferString(getChatMemory().getMessages(), getHumanPrefix(), getAiPrefix(), getSystemPrefix(), null, getToolPrefix());
        }
    }
}

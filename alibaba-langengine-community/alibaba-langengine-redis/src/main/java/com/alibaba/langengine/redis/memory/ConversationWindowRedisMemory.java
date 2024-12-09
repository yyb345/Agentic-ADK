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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.langengine.core.memory.impl.ConversationBufferWindowMemory;
import com.alibaba.langengine.core.messages.BaseMessage;

import com.alibaba.langengine.redis.memory.cache.RedisCache;
import org.apache.commons.lang3.StringUtils;

/**
 * @author aihe.ah
 * @time 2023/11/13
 * 功能说明：
 */
public class ConversationWindowRedisMemory extends ConversationBufferWindowMemory {

    /**
     * 窗口数量，返回最近的窗口数量消息
     */

    public ConversationWindowRedisMemory(RedisCache redisCache,
                                         String sessionId) {
        super();
        ChatMessageRedisHistory chatMessageRedisHistory = new ChatMessageRedisHistory();
        chatMessageRedisHistory.setRedisCache(redisCache);
        if (!StringUtils.isEmpty(sessionId)) {
            chatMessageRedisHistory.setSessionId(sessionId);
        } else {
            chatMessageRedisHistory.setSessionId(UUID.randomUUID().toString());
        }
        setChatMemory(chatMessageRedisHistory);
    }

    /**
     * 再拿到模型结果之后，preOutput部分
     *
     * 会把模型返回的output内容，重新保存一下
     *
     * @param inputs
     * @param outputs
     */
    @Override
    public void saveContext(Map<String, Object> inputs, Map<String, Object> outputs) {
        List<BaseMessage> messages = getChatMemory().getMessages();
        super.saveContext(inputs, outputs);
        int size = messages.size();
        if (size > getHistoryCount() * 2) {
            messages = messages.subList(size - getHistoryCount() * 2, size);
            getChatMemory().setMessages(messages);
        }
    }

}

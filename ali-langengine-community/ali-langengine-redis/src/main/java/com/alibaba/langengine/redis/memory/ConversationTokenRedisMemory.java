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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.tokenizers.GPT3Tokenizer;
import com.alibaba.langengine.core.tokenizers.Tokenizer;

import com.alibaba.langengine.redis.memory.cache.RedisCache;
import lombok.Setter;

/**
 * @author aihe.ah
 * @time 2023/11/13
 * 功能说明：
 */
public class ConversationTokenRedisMemory extends ConversationRedisMemory {

    /**
     * Maximum number of tokens to be kept in the memory pool.
     */
    private final Integer tokenSize;

    @Setter
    private Tokenizer tokenizer;

    public ConversationTokenRedisMemory(RedisCache redisCache, String sessionId, Integer tokenSize) {
        super(redisCache, sessionId);
        this.tokenSize = tokenSize;
        this.tokenizer = new GPT3Tokenizer();
    }

    /**
     * Trim the messages to fit within the token limit.
     *
     * @param messages The list of messages to be trimmed.
     * @return The trimmed list of messages.
     */
    private List<BaseMessage> trimMessages(List<BaseMessage> messages) {
        int currBufferLength = tokenizer.getTokenCount(
            MessageConverter.getBufferString(messages, getHumanPrefix(), getAiPrefix(), getSystemPrefix(), null, getToolPrefix()));

        while (currBufferLength > tokenSize) {
            int firstBufferLength = tokenizer.getTokenCount(
                    MessageConverter.getBufferString(Arrays.asList(messages.get(0)), getHumanPrefix(), getAiPrefix(),
                    getSystemPrefix(), null, getToolPrefix()));
            messages.remove(0);
            currBufferLength -= firstBufferLength;
        }
        return messages;
    }

    @Override
    public void saveContext(Map<String, Object> inputs, Map<String, Object> outputs) {
        super.saveContext(inputs, outputs);
        List<BaseMessage> messages = getChatMemory().getMessages();
        getChatMemory().setMessages(trimMessages(messages));
    }

    @Override
    public Object buffer() {
        List<BaseMessage> messages = new ArrayList<>(getChatMemory().getMessages());
        messages = trimMessages(messages);

        if (isReturnMessages()) {
            return messages;
        } else {
            return MessageConverter.getBufferString(messages, getHumanPrefix(), getAiPrefix(), getSystemPrefix(),
                null, getToolPrefix());
        }
    }
}

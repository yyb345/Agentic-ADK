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
package com.alibaba.langengine.core.memory.impl;

import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.memory.ChatMessageHistory;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于存储会话记忆的缓冲区
 *
 * @author xiaoxuan.lp
 */
@Data
public class ConversationBufferMemory extends BaseChatMemory {

    private BaseChatMessageHistory chatMemory = new ChatMessageHistory();

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

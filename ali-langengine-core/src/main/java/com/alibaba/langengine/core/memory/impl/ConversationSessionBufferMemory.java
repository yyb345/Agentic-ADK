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

import com.alibaba.langengine.core.caches.InMemoryCache;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.memory.ChatMessageSessionHistory;
import lombok.Data;

/**
 * 用于存储会话记忆的缓冲区，带sessionId
 *
 * @author xiaoxuan.lp
 */
@Data
public class ConversationSessionBufferMemory extends BaseChatMemory {

    private BaseChatMessageHistory chatMemory;

    public ConversationSessionBufferMemory() {
        this(new InMemoryCache());
    }

    public ConversationSessionBufferMemory(InMemoryCache inMemoryCache) {
        ChatMessageSessionHistory chatMessageSessionHistory = new ChatMessageSessionHistory();
        chatMessageSessionHistory.setInMemoryCache(inMemoryCache);
        chatMemory = chatMessageSessionHistory;
    }
}

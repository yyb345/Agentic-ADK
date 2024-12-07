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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.messages.BaseMessage;

import lombok.Data;

/**
 * 用于存储对话记忆的缓冲区
 *
 * @author xiaoxuan.lp
 */
@Data
public class ConversationBufferWindowMemory extends ConversationBufferMemory {

    /**
     * 记录存储条数
     */
    private int historyCount = 5;

    @Override
    public Map<String, Object> loadMemoryVariables(Map<String, Object> inputs) {
        List<BaseMessage> windowMessages = getWindowMessages();
        getChatMemory().setMessages(windowMessages);

        Map<String, Object> map = new HashMap<>();
        map.put(getMemoryKey(), buffer());
        return map;
    }

    @Override
    public void saveContext(Map<String, Object> inputs, Map<String, Object> outputs) {
        super.saveContext(inputs, outputs);
        List<BaseMessage> windowMessages = getWindowMessages();
        getChatMemory().setMessages(windowMessages);
    }

    /**
     * 获取窗口消息列表
     *
     * @return 窗口消息列表
     */
    private List<BaseMessage> getWindowMessages() {
        if (historyCount <= 0) {
            return new ArrayList<>();
        }

        List<BaseMessage> messages = (List<BaseMessage>)buffer();
        if (messages.size() / 2 > historyCount) {
            return messages.stream().skip(historyCount * 2L).collect(Collectors.toList());
        } else {
            return messages;
        }
    }
}

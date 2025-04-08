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
package com.alibaba.langengine.core.prompt;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatPromptValue extends PromptValue {

    private List<BaseMessage> messages = new ArrayList<>();

    @Override
    public List<BaseMessage> toMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return MessageConverter.getBufferString(messages);
    }
}

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
package com.alibaba.langengine.core.prompt.impl;

import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import lombok.Data;

import java.util.Map;

/**
 * AI message prompt template. This is a message that is not sent to the user.
 *
 * @author xiaoxuan.lp
 */
@Data
public class AIMessagePromptTemplate extends BaseStringMessagePromptTemplate {

    @Override
    public BaseMessage format(Map<String, Object> args) {
        String text = getPrompt().format(args);
        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent(text);
        return aiMessage;
    }
}

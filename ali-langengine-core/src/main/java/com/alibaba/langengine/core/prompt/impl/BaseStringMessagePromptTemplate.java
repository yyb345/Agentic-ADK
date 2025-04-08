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

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.BaseMessagePromptTemplate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class for message prompt templates that use a string prompt template.
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class BaseStringMessagePromptTemplate extends BaseMessagePromptTemplate {

    /**
     * String prompt template.
     */
    private StringPromptTemplate prompt;

    @Override
    public List<BaseMessage> formatMessages(Map<String, Object> args) {
        List<BaseMessage> baseMessages = new ArrayList<>();
        BaseMessage baseMessage = format(args);
        baseMessages.add(baseMessage);
        return baseMessages;
    }

    @Override
    public List<String> getInputVariables() {
        return prompt.getInputVariables();
    }

    /**
     * To a BaseMessage.
     *
     * @return
     */
    public abstract BaseMessage format(Map<String, Object> args);
}

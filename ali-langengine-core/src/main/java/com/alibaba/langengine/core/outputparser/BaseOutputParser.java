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
package com.alibaba.langengine.core.outputparser;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableStringVar;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.function.Consumer;

/**
 * 解析LLM调用输出的类
 *
 * @param <T>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseOutputParser<T> extends Runnable<RunnableInput, T> {

    /**
     * 关于如何格式化 LLM 输出的说明
     *
     * @return
     */
    public String getFormatInstructions() {
        return null;
    }

    /**
     * 返回类型键
     *
     * @return
     */
    public String getParserType() {
        return null;
    }

    /**
     * 解析LLM调用的输出
     *
     * @param text
     * @return
     */
    public abstract T parse(String text);

    public T parseWithPrompt(String completion, PromptValue prompt) {
        return parse(completion);
    }

    public T invoke(RunnableInput input, RunnableConfig config) {
        if(input instanceof BaseMessage) {
            BaseMessage baseMessage = (BaseMessage) input;
            return parse(baseMessage.getContent());
        } else if(input instanceof RunnableStringVar) {
            RunnableStringVar stringVar = (RunnableStringVar) input;
            return parse(stringVar.getValue());
        } else {
            throw new RuntimeException("OutputParser invalid input type, must be str, or BaseMessage.");
        }
    }

    @Override
    public T stream(RunnableInput input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config);
    }
}

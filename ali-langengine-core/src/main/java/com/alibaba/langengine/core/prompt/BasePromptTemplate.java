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

import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for all prompt templates, returning a prompt.
 * 所有提示模板的基类，返回提示
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BasePromptTemplate extends Runnable<RunnableInput, RunnableOutput> {

    /**
     * A list of the names of the variables the prompt template expects.
     * 提示模板所需的变量名称列表。
     */
    private List<String> inputVariables;

    /**
     * How to parse the output of calling an LLM on this formatted prompt.
     * 如何解析在此格式化提示上调用 LLM 的输出。
     */
    private BaseOutputParser outputParser;

    /**
     * 创建聊天消息
     *
     * @param args
     * @return
     */
    public abstract PromptValue formatPrompt(Map<String, Object> args);

    /**
     * format
     *
     * @param args
     * @return
     */
    public abstract String format(Map<String, Object> args);

    public abstract String getPromptType();

    public RunnableOutput invoke(RunnableInput input, RunnableConfig config) {
        if(input instanceof RunnableHashMap) {
            PromptValue prompt = formatPrompt((RunnableHashMap)input);
            return prompt;
        }
        return null;
    }

    public RunnableOutput stream(RunnableInput input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(input instanceof RunnableHashMap) {
            PromptValue prompt = formatPrompt((RunnableHashMap)input);
            return prompt;
        }
        return null;
    }

    public RunnableOutput streamLog(RunnableInput input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return stream(input, config, chunkConsumer);
    }
}

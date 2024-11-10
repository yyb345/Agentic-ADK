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
package com.alibaba.langengine.core.agent;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 代理单边基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseSingleActionAgent  {

    private LLMChain llmChain;

    /**
     * 代理的返回值
     *
     * @return
     */
    public List<String> returnValues() {
        return Arrays.asList(new String[] { "output" });
    }

    public List<String> getAllowedTools() {
        return null;
    }

    public Object plan(List<AgentAction> intermediateSteps, Map<String, Object> inputs) {
        return plan(intermediateSteps, inputs, null);
    }
    public Object plan(List<AgentAction> intermediateSteps, Map<String, Object> inputs, Map<String, Object> extraAttributes) {
        return plan(intermediateSteps, inputs, null, null, extraAttributes);
    }

    /**
     * 给定输入来决定做什么
     *
     * @param intermediateSteps 中间结果
     * @param inputs
     * @param executionContext
     * @param extraAttributes
     * @return
     */
    public abstract Object plan(List<AgentAction> intermediateSteps, Map<String, Object> inputs, Consumer<String> consumer, ExecutionContext executionContext, Map<String, Object> extraAttributes);

    /**
     * 返回输入key
     *
     * @return
     */
    public abstract List<String> getInputKeys();

    public abstract void setCallbackManager(BaseCallbackManager callbackManager);
}

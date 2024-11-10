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
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;

/**
 * 负责调用语言模型并决定动作的类
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class Agent extends BaseSingleActionAgent {

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        getLlmChain().setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
    }

    private AgentOutputParser outputParser;
    private List<String> allowedTools;

    public List<String> returnValues() {
        return Arrays.asList(new String[] { "output" });
    }

    public String stop() {
        return "\n" + observationPrefix().trim();
    }

    /**
     * 附加观察的前缀
     *
     * @return
     */
    public abstract String observationPrefix();

    /**
     * 附加 LLM 调用的前缀
     *
     * @return
     */
    public abstract String llmPrefix();

    public void init(boolean isCH) {
        // do nothings...
    }

    /**
     * 构建让代理继续其思考过程的暂存器
     *
     * @param intermediateSteps
     * @return
     */
    public String constructScratchpad(List<AgentAction> intermediateSteps) {
        String thoughts = "";
        for (AgentAction action : intermediateSteps) {
            thoughts += action.getLog();
            if(thoughts.endsWith("\n")) {
                thoughts = thoughts.substring(0, thoughts.length() - 1);
            }
            thoughts += "\n" + observationPrefix() + action.getObservation() + "\n" + llmPrefix();
        }
        return thoughts;
    }

    public Object plan(List<AgentAction> intermediateSteps, Map<String, Object> inputs, Consumer<String> consumer, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        Map<String, Object> fullInputs = getFullInputs(intermediateSteps, inputs);
        if(executionContext != null) {
            executionContext.setChildExecutionType("childChain-" + intermediateSteps.size());
        }
        String fullOutput = getLlmChain().predictToOutput(fullInputs, consumer, executionContext, extraAttributes);
        return outputParser.parse(fullOutput);
    }

    /**
     * 从中间步骤为 LLMChain 创建完整的输入
     *
     * @param intermediateSteps
     * @param inputs
     * @return
     */
    public Map<String, Object> getFullInputs(List<AgentAction> intermediateSteps, Map<String, Object> inputs) {
        String thoughts = constructScratchpad(intermediateSteps);

        Map<String, Object> fullInputs = new HashMap<>();
        fullInputs.putAll(inputs);
        fullInputs.put("agent_scratchpad", thoughts);
        fullInputs.put("stop", stop());
        return fullInputs;
    }

    @Override
    public List<String> getInputKeys() {
        List<String> inputKeys = new ArrayList<>(getLlmChain().getInputKeys());
        inputKeys.remove("agent_scratchpad");
        return inputKeys;
    }
}

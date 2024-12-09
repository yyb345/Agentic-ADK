/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.agentframework.delegation.core;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.agentframework.utils.FrameworkSystemContextUtils;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInterface;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class LLMDelegation extends DelegationBase {

    @Override
    public Map<String, Object> executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request) {
        String className = getClass().getName();
        log.info("{} start", className);
        FrameworkSystemContext systemContext =  FrameworkSystemContextUtils.getSystemContext(request, executionContext);
        Double temperature = properties.getDouble("temperature");
        String model = properties.getString("model");
        Integer maxTokens = properties.getInteger("maxTokens");
        Integer topK = properties.getInteger("topK");
        Double topP = properties.getDouble("topP");
        Double frequencyPenalty = properties.getDouble("frequencyPenalty");
        Double presencePenalty = properties.getDouble("presencePenalty");
        String stop = properties.getString("stop");
        String promptValue = getPropertyString(properties, "prompt", "");
        promptValue = DelegationHelper.replaceNewLine(promptValue);
        promptValue = DelegationHelper.replacePromptToVelocity(promptValue, request);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(promptValue);

        log.info("prompt is {}", JSONObject.toJSONString(prompt));

        BaseLanguageModel llm = getLLM(executionContext, properties, request);
        llm.setModel(model);
        llm.setTemperature(temperature);
        llm.setMaxTokens(maxTokens);
        llm.setTopK(topK);
        llm.setTopP(topP);
        llm.setFrequencyPenalty(frequencyPenalty);
        llm.setPresencePenalty(presencePenalty);

        log.info("{} llm model is {}, temperature is {}, maxTokens is {}, topK is {}, topP is {}, frequencyPenalty is {}, presencePenalty is {}, stop is {}", className,
                llm.getModel(), llm.getTemperature(), llm.getMaxTokens(), llm.getTopK(), llm.getTopP(),
                llm.getFrequencyPenalty(), llm.getFrequencyPenalty(), llm.getPresencePenalty(), stop);

        RunnableInterface chain = null;
        if(!StringUtils.isEmpty(stop)) {
            RunnableInterface modelBinding =  llm.bind(new HashMap<String, Object>() {{
                put("stop", Arrays.asList(stop.split(",")));
            }});
            chain = Runnable.sequence(prompt, modelBinding);
        } else {
            chain = Runnable.sequence(prompt, llm);
        }

        RunnableHashMap input = new RunnableHashMap();
        RunnableConfig config = new RunnableConfig();
        Object runnableOutput;
        if(systemContext.getChunkConsumer() != null) {
            runnableOutput = chain.stream(input, config, systemContext.getChunkConsumer());
        } else {
            runnableOutput = chain.invoke(input, config);
        }
        if(runnableOutput instanceof AIMessage) {
            AIMessage aiMessage = (AIMessage) runnableOutput;
            Map<String, Object> output = JSONObject.parseObject(JSONObject.toJSONString(aiMessage), Map.class);
            return output;
        }
        return new HashMap<>();
    }

    public abstract BaseLanguageModel getLLM(ExecutionContext executionContext, JSONObject properties, JSONObject request);
}

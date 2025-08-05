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
package com.alibaba.langengine.core.agent.expert;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.prompt.PromptConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 专家Chain
 *
 * 参考论文：https://arxiv.org/abs/2305.14688
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ExpertChain extends LLMChain {

    /**
     * ExpertPrompting
     */
    private BasePromptTemplate expertPrompt;

    /**
     * 专家提示大模型对象
     */
    private BaseLanguageModel expertLlm;

    private boolean isCH;

    public void setExpertLlm(BaseLanguageModel llm) {
        this.expertLlm = llm;
        if(getCallbackManager() != null) {
            this.expertLlm.setCallbackManager(getCallbackManager().getChild());
        }
    }

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        this.expertLlm.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
    }

    public ExpertChain() {
        this(false);
    }

    public ExpertChain(boolean isCH) {
        if(isCH) {
            setExpertPrompt(PromptConstants.EXPERT_IDENTITY_PROMPT_CH);
            setPrompt(PromptConstants.EXPERT_PROMPTING_PROMPT_CH);
        } else {
            setExpertPrompt(PromptConstants.EXPERT_IDENTITY_PROMPT_EN);
            setPrompt(PromptConstants.EXPERT_PROMPTING_PROMPT_EN);
        }
    }

    @Override
    public LLMResult generate(List<Map<String, Object>> inputsList, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        LLMResult llmResult = expertGenerate(inputsList, executionContext, extraAttributes);
        if (llmResult.getGenerations().size() > 0) {
            List<Generation> generations = llmResult.getGenerations().get(0);
            if (generations.size() > 0) {
                String text = generations.get(0).getText();
                inputsList.get(0).put("expert_identity", text);
            }
        }
        return super.generate(inputsList, executionContext, consumer, extraAttributes);
    }

    private LLMResult expertGenerate(List<Map<String, Object>> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        List<PromptValue> promptValues = inputs.stream()
                .map(input -> expertPrompt.formatPrompt(input)).collect(Collectors.toList());
        Map<String, Object> input = inputs.get(0);
        List<String> stops;
        if(input.containsKey("stop")) {
            stops = Arrays.asList(new String[] { input.get("stop").toString() });
        } else {
            stops = STOP_LIST;
        }
        if(executionContext != null) {
            executionContext.setExecutionType("expertLlm");
        }
        return expertLlm.generatePrompt(promptValues, stops, executionContext, null, extraAttributes);
    }
}

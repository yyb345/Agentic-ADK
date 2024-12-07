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
package com.alibaba.langengine.core.chain.router;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A router chain that uses an LLM chain to perform routing.
 * 使用LLM链执行路由的路由器链。
 *
 * @author xiaoxuan.lp
 */
@Data
public class LLMRouterChain extends RouterChain {

    /**
     * LLM chain used to perform routing
     * LLM链用于执行路由
     */
    private LLMChain llmChain;

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Object result = llmChain.predictAndParse(inputs, extraAttributes);
        if(result instanceof Map) {
            return (Map)result;
        }
        return null;
    }

    @Override
    public List<String> getInputKeys() {
        return llmChain.getInputKeys();
    }

    public static LLMRouterChain fromLlm(BaseLanguageModel llm, BasePromptTemplate prompt){
        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);
        LLMRouterChain llmRouterChain = new LLMRouterChain();
        llmRouterChain.setLlmChain(chain);
        return llmRouterChain;
    }
}

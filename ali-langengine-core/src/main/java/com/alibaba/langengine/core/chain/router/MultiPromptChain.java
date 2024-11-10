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

import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.ConversationChain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolCoreLoaders;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.langengine.core.chain.router.PromptConstants.MULTI_PROMPT_ROUTER_TEMPLATE;
import static com.alibaba.langengine.core.chain.router.PromptConstants.MULTI_PROMPT_ROUTER_TEMPLATE_CN;

/**
 * A multi-route chain that uses an LLM router chain to choose amongst prompts.
 * 使用 LLM 路由器链在提示中进行选择的多路由链。
 *
 * @author xiaoxuan.lp
 */
@Data
public class MultiPromptChain extends MultiRouteChain {

    private static final String[] OUTPUT_KEYS = new String[] { "text" };

    @Override
    public List<String> getOutputKeys() {
        return new ArrayList<>();
    }

    /**
     * Convenience constructor for instantiating from destination prompts.
     * 用于从目标提示实例化的便捷构造函数。
     *
     * @param llm
     * @param promptInfos
     * @return
     */
    public static MultiPromptChain fromPrompts(BaseLanguageModel llm,
                                               List<Map<String, Object>> promptInfos) {
        return fromPrompts(llm, promptInfos, null, null, null, false);
    }

    public static MultiPromptChain fromPrompts(BaseLanguageModel llm,
                                               List<Map<String, Object>> promptInfos,
                                               boolean isCH) {
        return fromPrompts(llm, promptInfos, null, null, null, isCH);
    }

    public static MultiPromptChain fromPrompts(BaseLanguageModel llm,
                                               List<Map<String, Object>> promptInfos,
                                               PromptTemplate routerPrompt,
                                               boolean isCH) {
        return fromPrompts(llm, promptInfos, routerPrompt, null, null, isCH);
    }

    public static MultiPromptChain fromPrompts(BaseLanguageModel llm,
                                               List<Map<String, Object>> promptInfos,
                                               RouterChain routerChain) {
        return fromPrompts(llm, promptInfos, null, null, routerChain, false);
    }

    /**
     * Convenience constructor for instantiating from destination prompts.
     * 用于从目标提示实例化的便捷构造函数。
     *
     * @param llm
     * @param promptInfos
     * @param defaultChain
     * @return
     */
    public static MultiPromptChain fromPrompts(BaseLanguageModel llm,
                                               List<Map<String, Object>> promptInfos,
                                               PromptTemplate routerPrompt,
                                               Chain defaultChain,
                                               RouterChain routerChain,
                                               boolean isCH) {
        if(routerChain == null) {
            List<String> destinations = promptInfos.stream()
                    .map(p -> String.format("%s: %s",  p.get("name"), p.get("description")))
                    .collect(Collectors.toList());
            String destinationsStr = destinations.stream().collect(Collectors.joining("\n"));

            Map<String, Object> params = new HashMap<>();
            params.put("destinations", destinationsStr);

            if(routerPrompt == null) {
                routerPrompt = new PromptTemplate();
                String prompt = isCH ? MULTI_PROMPT_ROUTER_TEMPLATE_CN : MULTI_PROMPT_ROUTER_TEMPLATE;
                String routerTemplate = PromptConverter.replacePrompt(prompt, params);
                routerPrompt.setTemplate(routerTemplate);
            }
            routerPrompt.setInputVariables(Arrays.asList(new String[]{ "input" }));
            routerPrompt.setOutputParser(new RouterOutputParser());

            routerChain = LLMRouterChain.fromLlm(llm, routerPrompt);
        }

        Map<String, Chain> destinationChains = new HashMap<>();
        for (Map<String, Object> pInfo : promptInfos) {
            String name = pInfo.get("name").toString();
            String promptTemplate = pInfo.get("prompt_template").toString();
            PromptTemplate prompt = new PromptTemplate();
            prompt.setTemplate(promptTemplate);
            prompt.setInputVariables(Arrays.asList(new String[]{ "input", "context" }));

            boolean isContain = pInfo.containsKey("tool");
            if(isContain) {
                BaseTool baseTool = (BaseTool) pInfo.get("tool");
                List<BaseTool> baseTools = new ArrayList<>();
                baseTools.add(baseTool);
                AgentExecutor agentExecutor = ToolCoreLoaders.initializeAgent(baseTools, llm);
                destinationChains.put(name, agentExecutor);
            } else {
                LLMChain chain = new LLMChain();
                chain.setLlm(llm);
                chain.setPrompt(prompt);
                destinationChains.put(name, chain);
            }
        }

        Chain _defaultChain = defaultChain;
        if(defaultChain == null) {
            _defaultChain = new ConversationChain();
            _defaultChain.setLlm(llm);
        }

        MultiPromptChain multiPromptChain = new MultiPromptChain();
        multiPromptChain.setRouterChain(routerChain);
        multiPromptChain.setDestinationChains(destinationChains);
        multiPromptChain.setDefaultChain(_defaultChain);
        return multiPromptChain;
    }
}

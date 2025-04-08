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
package com.alibaba.langengine.core.agent.mrkl;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.langengine.core.agent.mrkl.PromptConstants.*;

/**
 * MRKL链的代理
 * 模块化推理、知识和语言（ the Modular Reasoning, Knowledge and Language，简称为MRKL）
 *
 * @author xiaoxuan.lp
 */
@Data
public class ZeroShotAgent extends Agent {

    private BaseLanguageModel llm;

    private List<BaseTool> tools;
    private String prefix = PromptConstants.PREFIX;
    private String suffix = PromptConstants.SUFFIX;

    private String formatInstructions = PromptConstants.FORMAT_INSTRUCTIONS;



    public void ZeroShotAgent() {
        setOutputParser(new MRKLOutputParser());
    }

    @Override
    public void init(boolean isCH) {
        PromptTemplate prompt;
        if(isCH) {
            prompt = createPrompt(tools, PREFIX_CH, SUFFIX_CH, FORMAT_INSTRUCTIONS_CH);
        } else {
            prompt = createPrompt(tools, prefix, suffix, formatInstructions);
        }
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);
        setLlmChain(llmChain);
        setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        if(getOutputParser() == null) {
            setOutputParser(new MRKLOutputParser());
        }
    }

    /**
     * Prefix to append the observation with.
     *
     * @return
     */
    @Override
    public String observationPrefix() {
        return "Observation: ";
    }

    /**
     * Prefix to append the llm call with.
     *
     * @return
     */
    @Override
    public String llmPrefix() {
        return "Thought:";
    }

    @Override
    public List<String> getInputKeys() {
        return null;
    }

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        llm.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        if(tools != null) {
            tools.stream().forEach(tool -> tool.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null));
        }
        super.setCallbackManager(callbackManager);
    }

    /**
     * 以零镜头代理的风格创建提示
     *
     * @param tools
     * @param prefix
     * @param suffix
     * @param formatInstructions
     * @return
     */
    public PromptTemplate createPrompt(List<BaseTool> tools,
                                       String prefix,
                                       String suffix,
                                       String formatInstructions) {
        if(prefix == null) {
            prefix = PromptConstants.PREFIX;
        }
        if(suffix == null) {
            suffix = PromptConstants.SUFFIX;
        }
        if(formatInstructions == null) {
            formatInstructions = PromptConstants.FORMAT_INSTRUCTIONS;
        }

        List<String> toolStringList = new ArrayList<>();
        List<String> toolNameList = new ArrayList<>();
        for (BaseTool tool : tools) {
            toolStringList.add(String.format("%s: %s args: %s", tool.getName(), tool.getDescription(), JSON.toJSONString(tool.getArgs())));
            toolNameList.add(tool.getName());
        }
        String toolStrings = toolStringList.stream().collect(Collectors.joining("\n"));
        String toolNames = toolNameList.stream().collect(Collectors.joining(", "));
        Map<String, Object> toolNameMap = new HashMap<>();
        toolNameMap.put("tool_names", toolNames);
        formatInstructions = PromptConverter.replacePrompt(formatInstructions, toolNameMap);

        List<String> templateStringList = new ArrayList<>();
        templateStringList.add(prefix);
        templateStringList.add(toolStrings);
        templateStringList.add(formatInstructions);
        templateStringList.add(suffix);
        String template = templateStringList.stream().collect(Collectors.joining("\n\n"));
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);
        return promptTemplate;
    }
}

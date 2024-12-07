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
package com.alibaba.langengine.core.agent.conversational;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An agent that holds a conversation in addition to using tools.
 *
 * @author xiaoxuan.lp
 */
@Data
public class ConversationalAgent extends Agent {

    /**
     * Prefix to use before AI output.
     */
    private String aiPrefix = "AI";

    @Override
    public String observationPrefix() {
        return "Observation: ";
    }

    @Override
    public String llmPrefix() {
        return "Thought:";
    }

    /**
     * Construct an agent from an LLM and tools.
     *
     * @param llm
     * @param tools
     * @param callbackManager
     * @param outputParser
     * @param prefix
     * @param suffix
     * @param formatInstructions
     * @param aiPrefix
     * @param humanPrefix
     * @param inputVariables
     * @param memory
     * @return
     */
    public static Agent fromLlmAndTools(BaseLanguageModel llm,
                                        List<BaseTool> tools,
                                        BaseCallbackManager callbackManager,
                                        AgentOutputParser outputParser,
                                        String prefix,
                                        String suffix,
                                        String formatInstructions,
                                        String aiPrefix,
                                        String humanPrefix,
                                        List<String> inputVariables,
                                        BaseMemory memory) {
        return fromLlmAndTools(llm, tools, callbackManager, outputParser, prefix, suffix, formatInstructions, aiPrefix, humanPrefix, inputVariables, memory, false);
    }

    /**
     * Construct an agent from an LLM and tools.
     *
     * @param llm
     * @param tools
     * @param callbackManager
     * @param outputParser
     * @param prefix
     * @param suffix
     * @param formatInstructions
     * @param aiPrefix
     * @param humanPrefix
     * @param inputVariables
     * @param memory
     * @param isCH
     * @return
     */
    public static Agent fromLlmAndTools(BaseLanguageModel llm,
                                        List<BaseTool> tools,
                                        BaseCallbackManager callbackManager,
                                        AgentOutputParser outputParser,
                                        String prefix,
                                        String suffix,
                                        String formatInstructions,
                                        String aiPrefix,
                                        String humanPrefix,
                                        List<String> inputVariables,
                                        BaseMemory memory,
                                        boolean isCH) {
        if(prefix == null) {
            prefix = (isCH ? PromptConstants.PREFIX_CH : PromptConstants.PREFIX);
        }
        if(suffix == null) {
            suffix = (isCH ? PromptConstants.SUFFIX_CH : PromptConstants.SUFFIX);
        }
        if(formatInstructions == null) {
            formatInstructions = (isCH ? PromptConstants.FORMAT_INSTRUCTIONS_CH : PromptConstants.FORMAT_INSTRUCTIONS);
        }
        if(inputVariables == null) {
            inputVariables = Arrays.asList(new String[] { "input", "chat_history", "agent_scratchpad" });
        }
        if(aiPrefix == null) {
            aiPrefix = "AI";
        }
        if(humanPrefix == null) {
            humanPrefix = "Human";
        }
        if(outputParser == null) {
            outputParser = new ConvoOutputParser();
        }
        PromptTemplate prompt = createPrompt(tools, prefix, suffix, formatInstructions, aiPrefix, humanPrefix, inputVariables);

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);
        if(callbackManager != null) {
            llmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
        if(memory != null) {
            llmChain.setMemory(memory);
        }

        Agent agent = new ConversationalAgent();
        agent.setLlmChain(llmChain);
        agent.setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        agent.setOutputParser(outputParser);
//        agent.setCallbackManager(callbackManager);
        return agent;
    }

    public static PromptTemplate createPrompt(List<BaseTool> tools,
                                              String prefix,
                                              String suffix,
                                              String formatInstructions,
                                              String aiPrefix,
                                              String humanPrefix,
                                              List<String> inputVariables) {
        List<String> toolStringList = new ArrayList<>();
        List<String> toolNameList = new ArrayList<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                StructuredTool structuredTool = (StructuredTool) tool;
                String structSchema = structuredTool.formatStructSchema();
                toolStringList.add(String.format("%s: %s, args: %s", tool.getName(), tool.getDescription(), structSchema));
                toolNameList.add(tool.getName());
            } else {
                toolStringList.add(String.format("%s: %s", tool.getName(), tool.getDescription()));
                toolNameList.add(tool.getName());
            }
        }
        String toolStrings = toolStringList.stream().collect(Collectors.joining("\n"));
        String toolNames = toolNameList.stream().collect(Collectors.joining(", "));
        Map<String, Object> toolNameMap = new HashMap<>();
        toolNameMap.put("tool_names", toolNames);
        toolNameMap.put("ai_prefix", aiPrefix);
        toolNameMap.put("human_prefix", humanPrefix);
        formatInstructions = PromptConverter.replacePrompt(formatInstructions, toolNameMap);

        List<String> templateStringList = new ArrayList<>();
        templateStringList.add(prefix);
        templateStringList.add(toolStrings);
        templateStringList.add(formatInstructions);
        templateStringList.add(suffix);
        String template = templateStringList.stream().collect(Collectors.joining("\n\n"));
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);
        promptTemplate.setInputVariables(inputVariables);
        return promptTemplate;
    }

    @Override
    public List<String> getInputKeys() {
        return null;
    }
}

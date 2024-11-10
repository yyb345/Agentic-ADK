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
package com.alibaba.langengine.core.agent.structured2;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.*;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * StructuredChatAgent V2
 *
 * @author xiaoxuan.lp
 */
@Data
public class StructuredChatAgentV2 extends Agent {

    @Override
    public String observationPrefix() {
        return "Observation: ";
    }

    @Override
    public String llmPrefix() {
        return "Thought:";
    }

    @Override
    public String stop() {
        return "Observation:";
    }

    @Override
    public String constructScratchpad(List<AgentAction> intermediateSteps) {
        String agentScratchpad = super.constructScratchpad(intermediateSteps);
        return agentScratchpad;
//        if(!StringUtils.isEmpty(agentScratchpad)) {
//            return "This was your previous work (but I haven't seen any of it! I only see what you return as final answer):\n" + agentScratchpad;
//        } else {
//            return agentScratchpad;
//        }
    }

    public static Agent fromLlmAndTools(BaseLanguageModel llm,
                                        List<BaseTool> tools,
                                        BaseCallbackManager callbackManager,
                                        AgentOutputParser outputParser,
                                        String prefix,
                                        String suffix,
                                        String formatInstructions,
                                        String example,
                                        List<String> inputVariables,
                                        BaseMemory memory,
                                        boolean isCH,
                                        Agent agent) {
        if(prefix == null) {
            prefix = llm.getStructuredChatAgentPrefixPrompt(memory, isCH);
        }
        if(suffix == null) {
            suffix = llm.getStructuredChatAgentSuffixPrompt(memory, isCH);
        }
        if(formatInstructions == null) {
            formatInstructions = llm.getStructuredChatAgentInstructionsPrompt(memory, isCH);
        }
        if(outputParser == null) {
            outputParser = llm.getStructuredChatOutputParser();
        }
        if (inputVariables == null) {
            inputVariables = new ArrayList<>();
            inputVariables.add("input");
            inputVariables.add("agent_scratchpad");
        }
        String toolDesc = llm.getToolDescriptionPrompt(memory, isCH);
        BasePromptTemplate prompt = createPrompt(tools, prefix, suffix, formatInstructions, toolDesc, example, inputVariables);

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);
        llmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        if(memory != null) {
            llmChain.setMemory(memory);
        }

        if(agent == null) {
            agent = new StructuredChatAgentV2();
        }
        agent.setLlmChain(llmChain);
//        agent.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        agent.setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        agent.setOutputParser(outputParser);
        return agent;
    }

    public static BasePromptTemplate createPrompt(List<BaseTool> tools,
                                                  String prefix,
                                                  String suffix,
                                                  String formatInstructions,
                                                  String toolDesc,
                                                  String example,
                                                  List<String> inputVariables) {
        List<String> toolStrings = new ArrayList<>();
        for (BaseTool tool : tools) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("name_for_model", tool.getName());
            inputs.put("name_for_human", tool.getHumanName());
            inputs.put("description_for_model", tool.getDescription());
            String structSchema;
            if(tool instanceof StructuredTool) {
                StructuredTool structuredTool = (StructuredTool) tool;
                structSchema = structuredTool.formatStructSchema();
            } else {
                structSchema = Pattern.compile("\\}").matcher(Pattern.compile("\\{").matcher(tool.getArgs().toString()).replaceAll("{{")).replaceAll("}}");
            }
            inputs.put("parameters", !StringUtils.isEmpty(structSchema) ? structSchema : "{}");
            String toolString = PromptConverter.replacePrompt(toolDesc, inputs);
            toolStrings.add(toolString);
        }
        String formattedTools = String.join("\n", toolStrings);
        String toolNames = String.join(", ", tools.stream().map(BaseTool::getName).toArray(String[]::new));
        Map<String, Object> toolNameMap = new HashMap<>();
        toolNameMap.put("tool_names", toolNames);
        formatInstructions = PromptConverter.replacePrompt(formatInstructions, toolNameMap);
        String template = String.join("\n\n", prefix, formattedTools, formatInstructions,
                example != null ? example : "", suffix);

        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);
        promptTemplate.setInputVariables(inputVariables);
        return promptTemplate;
    }
}

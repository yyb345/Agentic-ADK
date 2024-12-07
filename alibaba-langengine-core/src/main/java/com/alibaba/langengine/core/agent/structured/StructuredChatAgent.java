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
package com.alibaba.langengine.core.agent.structured;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.prompt.impl.HumanMessagePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.prompt.impl.SystemMessagePromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.alibaba.langengine.core.agent.structured.PromptConstants.HUMAN_MESSAGE_TEMPLATE;

/**
 * StructuredChatAgent
 *
 * @author xiaoxuan.lp
 */
@Data
public class StructuredChatAgent extends Agent {

    private BaseLanguageModel llm;

    private List<BaseTool> tools;

    private String prefix = PromptConstants.PREFIX;
    private String suffix = PromptConstants.SUFFIX;

    private String formatInstructions = PromptConstants.FORMAT_INSTRUCTIONS;

    public StructuredChatAgent() {
        setOutputParser(new StructuredChatOutputParser());
    }

    @Override
    public void init(boolean isCH) {
        // TODO isCH模版
        if(isCH) {
            formatInstructions = PromptConstants.FORMAT_INSTRUCTIONS_CH;
//            prefix = PromptConstants.PREFIX_CH;
//            suffix = PromptConstants.SUFFIX_CH;
        }
        PromptTemplate prompt = createPrompt(tools, prefix, suffix, formatInstructions);
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);
        setLlmChain(llmChain);
        setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        if(getOutputParser() == null) {
            setOutputParser(new StructuredChatOutputParser());
        }
    }

    @Override
    public String observationPrefix() {
        return "Observation: ";
    }

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
            tools.stream().forEach(tool -> tool.setCallbackManager(callbackManager.getChild() != null ? callbackManager.getChild() : null));
        }
        super.setCallbackManager(callbackManager);
    }

    @Override
    public String stop() {
        return "Observation:";
    }

    public PromptTemplate createPrompt(List<BaseTool> tools,
                                       String prefix,
                                       String suffix,
                                       String formatInstructions) {
        if(prefix == null) {
            prefix = PromptConstants.PREFIX;
        }
//        if(suffix == null) {
//            suffix = PromptConstants.SUFFIX;
//        }
        if(formatInstructions == null) {
            formatInstructions = PromptConstants.FORMAT_INSTRUCTIONS;
        }

        List<String> toolStringList = new ArrayList<>();
        List<String> toolNameList = new ArrayList<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                // TODO struct ...
                StructuredTool structuredTool = (StructuredTool) tool;
                String structSchema = structuredTool.formatStructSchema();
//                String structSchema = "{\"applicationName\":\"应用程序名称\",\"param\":\"具体的字段列表\"}";
//                String structSchema = "{\"type\":\"请求的类型，人群为crowd,画像为analyze\",\"param\":{\"gender\":\"性别\",\"last_visit_days\":\"最近访问网站的天数\",\"visited\":[\"访问过的网站\"]}}";
//                toolStringList.add(String.format("%s: %s, args: %s", tool.getName(), tool.getDescription(), structSchema));
                toolStringList.add(tool.getName()+":"+tool.getDescription());
                toolNameList.add(tool.getName());
            }
        }
        String toolListDescription = toolStringList.stream().collect(Collectors.joining("\n"));
        String toolNames = toolNameList.stream().collect(Collectors.joining(", "));
        Map<String, Object> toolNameMap = new HashMap<>();
        toolNameMap.put("tool_names", toolNames);
        toolNameMap.put("tool_list_description", toolListDescription);

        formatInstructions = PromptConverter.replacePrompt(formatInstructions, toolNameMap);

        List<String> templateStringList = new ArrayList<>();
        templateStringList.add(prefix);
        templateStringList.add(formatInstructions);
        templateStringList.add(suffix);
        String template = templateStringList.stream().collect(Collectors.joining("\n\n"));
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);
        return promptTemplate;
    }

    public static Agent fromLlmAndTools(BaseLanguageModel llm,
                                        List<BaseTool> tools,
                                        BaseCallbackManager callbackManager,
                                        AgentOutputParser outputParser,
                                        String prefix,
                                        String suffix,
                                        String humanMessageTemplate,
                                        String formatInstructions,
                                        List<String> inputVariables,
                                        List<BasePromptTemplate> memoryPrompts) {
        if(prefix == null) {
            prefix = PromptConstants.PREFIX;
        }
        if(suffix == null) {
            suffix = PromptConstants.SUFFIX;
        }
        if(humanMessageTemplate == null) {
            humanMessageTemplate = HUMAN_MESSAGE_TEMPLATE;
        }
        if(formatInstructions == null) {
            formatInstructions = PromptConstants.FORMAT_INSTRUCTIONS;
        }
        if(outputParser == null) {
            outputParser = new StructuredChatOutputParser();
        }
        if (inputVariables == null) {
            inputVariables = Arrays.asList(new String[] { "input", "agent_scratchpad" });
        }
        BasePromptTemplate prompt = createPrompt(tools, prefix, suffix, humanMessageTemplate, formatInstructions, inputVariables, memoryPrompts);

        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);
        llmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);

        StructuredChatAgent agent = new StructuredChatAgent();
        agent.setLlmChain(llmChain);
        agent.setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        agent.setOutputParser(outputParser);
        agent.setLlm(llm);
        agent.setCallbackManager(callbackManager);
        return agent;
    }

    public static BasePromptTemplate createPrompt(List<BaseTool> tools,
                                                  String prefix,
                                                  String suffix,
                                                  String humanMessageTemplate,
                                                  String formatInstructions,
                                                  List<String> inputVariables,
                                                  List<BasePromptTemplate> memoryPrompts) {
        List<String> toolStrings = new ArrayList<>();
        for (BaseTool tool : tools) {
            String argsSchema = Pattern.compile("\\}").matcher(Pattern.compile("\\{").matcher(tool.getArgs().toString()).replaceAll("{{")).replaceAll("}}");
            toolStrings.add(String.format("%s: %s, args: %s", tool.getName(), tool.getDescription(), argsSchema));
        }
        String formattedTools = String.join("\n", toolStrings);
        String toolNames = String.join(", ", tools.stream().map(BaseTool::getName).toArray(String[]::new));
        Map<String, Object> toolNameMap = new HashMap<>();
        toolNameMap.put("tool_names", toolNames);
        formatInstructions = PromptConverter.replacePrompt(formatInstructions, toolNameMap);
        String template = String.join("\n\n", prefix, formattedTools, formatInstructions, suffix);
        List<Object> messages = new ArrayList<>();
        SystemMessagePromptTemplate systemMessagePromptTemplate = new SystemMessagePromptTemplate();
        PromptTemplate systemPrompt = new PromptTemplate();
        systemPrompt.setTemplate(template);
        systemMessagePromptTemplate.setPrompt(systemPrompt);
        messages.add(systemMessagePromptTemplate);
        if (memoryPrompts != null) {
            messages.addAll(memoryPrompts);
        }
        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        PromptTemplate humanPrompt = new PromptTemplate();
        humanPrompt.setTemplate(humanMessageTemplate);
        humanMessagePromptTemplate.setPrompt(humanPrompt);
        messages.add(humanMessagePromptTemplate);

        ChatPromptTemplate chatPromptTemplate = new ChatPromptTemplate();
        chatPromptTemplate.setInputVariables(inputVariables);
        chatPromptTemplate.setMessages(messages);
        return chatPromptTemplate;
    }
}

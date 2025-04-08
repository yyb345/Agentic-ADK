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
package com.alibaba.langengine.core.agent.autogpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.prompt.autogpt.AutoGPTPromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.alibaba.langengine.core.prompt.autogpt.PromptGenerator.FINISH_NAME;

/**
 * Agent class for interacting with Auto-GPT.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class AutoGPTAgent {

    /**
     * 回调管理器
     */
    private BaseCallbackManager callbackManager;

    public BaseCallbackManager getCallbackManager() {
        if(callbackManager == null) {
            callbackManager = LangEngineConfiguration.CALLBACK_MANAGER;
        }
        return callbackManager;
    }

    /**
     * 可重试次数
     */
    private int maxLimit = 5;

    private String aiName;

    private String aiRole;

    private LLMChain chain;

    private BaseAutoGPTOutputParser outputParser;

    private List<BaseTool> tools = new ArrayList<>();

    public void setTools(List<BaseTool> tools) {
        this.tools = tools;
        if (getCallbackManager() != null) {
            tools.stream().forEach(tool -> tool.setCallbackManager(getCallbackManager().getChild()));
        }
    }

    private List<BaseMessage> fullMessageHistory = new ArrayList<>();

    private VectorStoreRetriever memory;

    public static AutoGPTAgent fromLlmAndTools(String aiName,
                                               String aiRole,
                                               VectorStoreRetriever memory,
                                               List<BaseTool> tools,
                                               BaseLanguageModel llm,
                                               BaseCallbackManager callbackManager) {
        return fromLlmAndTools(aiName, aiRole, memory, tools, llm, null, callbackManager);
    }

    public static AutoGPTAgent fromLlmAndTools(String aiName,
                                               String aiRole,
                                               VectorStoreRetriever memory,
                                               List<BaseTool> tools,
                                               BaseLanguageModel llm,
                                               BaseAutoGPTOutputParser outputParser,
                                               BaseCallbackManager callbackManager) {
        AutoGPTAgent autoGPT = new AutoGPTAgent();
        autoGPT.setAiName(aiName);
        autoGPT.setMemory(memory);

        AutoGPTPromptTemplate prompt = new AutoGPTPromptTemplate();
        prompt.setAiName(aiName);
        prompt.setAiRole(aiRole);
        prompt.setTools(tools);

        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);
        chain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        autoGPT.setChain(chain);

        if(outputParser != null) {
            autoGPT.setOutputParser(outputParser);
        } else {
            autoGPT.setOutputParser(new AutoGPTOutputParser());
        }
        autoGPT.setCallbackManager(callbackManager);
        autoGPT.setTools(tools);

        return autoGPT;
    }

    public String run(List<String> goals) {
        return run(goals, null);
    }

    public String run(List<String> goals, ExecutionContext executionContext) {
        return run(goals, executionContext, null);
    }

    public String run(List<String> goals, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        String userInput = "Determine which next command to use, and respond using the format specified above:";
        int loopCount = 0;
        String result = null;
        while(true) {
            // Discontinue if continuous limit is reached
            loopCount += 1;
            if(loopCount > maxLimit) {
                return null;
            }
            // Send message to AI, get response
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("goals", goals);
            inputs.put("messages", fullMessageHistory);
            inputs.put("memory", memory);
            inputs.put("user_input", userInput);
            Map<String, Object> assistantReplyMap = chain.run(inputs, executionContext, null, extraAttributes);
            String assistantReply = (String) assistantReplyMap.get("text");
            log.warn("assistantReply:" + assistantReply);

            HumanMessage humanMessage = new HumanMessage();
            humanMessage.setContent(userInput);
            fullMessageHistory.add(humanMessage);
            AIMessage aiMessage = new AIMessage();
            aiMessage.setContent(assistantReply);
            fullMessageHistory.add(aiMessage);

            // Get command name and arguments
            AutoGPTAction autoGPTAction = outputParser.parse(assistantReply);
            if(autoGPTAction == null) {
                return null;
            }
            Map<String, BaseTool> toolMap = new TreeMap<>();
            tools.stream().forEach(tool -> toolMap.put(tool.getName(), tool));

            if(autoGPTAction.getName().equals(FINISH_NAME)) {
                return (String) autoGPTAction.getArgs().get("response");
            }

            if(toolMap.containsKey(autoGPTAction.getName())) {
                BaseTool tool = toolMap.get(autoGPTAction.getName());
                ToolExecuteResult observation = tool.run(JSON.toJSONString(autoGPTAction.getArgs()), executionContext); // TODO ...
                result = String.format("Command %s returned: %s", tool.getName(), observation.getOutput());
            } else if(autoGPTAction.getName().equals("ERROR")) {
                result = String.format("Error: %s. ", JSON.toJSONString(autoGPTAction.getArgs()));
            } else {
                result = "Unknown command '{action.name}'. Please refer to the 'COMMANDS' list for available commands and only respond in the specified JSON format.";
            }

            String memoryToAdd = String.format("Assistant Reply: %s \nResult: %s ", assistantReply, result);

            Document document = new Document();
            document.setPageContent(memoryToAdd);
            memory.addDocuments(Arrays.asList(new Document[]{ document }));

            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setContent(result);
            fullMessageHistory.add(systemMessage);
        }
    }

    /**
     * 对象序列化
     *
     * @return
     */
    public String serialize() {
        try {
            return JacksonUtils.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

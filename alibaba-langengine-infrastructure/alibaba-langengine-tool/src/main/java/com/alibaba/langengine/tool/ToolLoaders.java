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
package com.alibaba.langengine.tool;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.agent.conversational.ConversationalAgent;
import com.alibaba.langengine.core.agent.reactdoc.ReActDocstoreAgent;
import com.alibaba.langengine.core.agent.reactdoc.ReActOutputParser;
import com.alibaba.langengine.core.agent.selfask.SelfAskOutputParser;
import com.alibaba.langengine.core.agent.selfask.SelfAskWithSearchAgent;
import com.alibaba.langengine.core.agent.semantickernel.SemanticKernelAgent;
import com.alibaba.langengine.core.agent.semantickernel.planning.BasePlanner;
import com.alibaba.langengine.core.agent.structured.StructuredChatAgent;
import com.alibaba.langengine.core.agent.structured2.StructuredChatAgentV2;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolCoreLoaders;
import com.alibaba.langengine.tool.bing.WebSearchAPITool;
import com.alibaba.langengine.tool.google.SerpapiTool;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具加载器
 *
 * @author xiaoxuan.lp
 */
public class ToolLoaders {

    public static List<BaseTool> loadLools(List<String> toolNames, BaseLanguageModel llm) {
        List<BaseTool> baseTools = new ArrayList<>();
        for (String toolName : toolNames) {
            if(toolName.equals("llm-math")) {
                LLMMathTool tool = new LLMMathTool();
                baseTools.add(tool);
            } if(toolName.equals("llm-math-advance")) {
                LLMMathAdvanceTool tool = new LLMMathAdvanceTool();
                tool.setLlm(llm);
                baseTools.add(tool);
            } else if(toolName.equals("serpapi")) {
                SerpapiTool tool = new SerpapiTool();
                baseTools.add(tool);
            } else if(toolName.equals("error-res")) {
                ErrorCodeAPITool tool = new ErrorCodeAPITool();
                baseTools.add(tool);
            } else if(toolName.equals("create_application")) {
                CreateAppTool tool = new CreateAppTool();
                baseTools.add(tool);
            } else if(toolName.equals("BingWebSearchAPI")) {
                WebSearchAPITool tool = new WebSearchAPITool();
                baseTools.add(tool);
            }else if(toolName.equals("ApiLogTool")){
                ApiLogTool apiLogTool = new ApiLogTool();
                baseTools.add(apiLogTool);
            }else if(toolName.equals("DocTool")){
                DocTool docTool = new DocTool();
                baseTools.add(docTool);
            }
        }
        return baseTools;
    }

    public static AgentExecutor initializeAgent(List<BaseTool> tools, BaseLanguageModel llm) {
        return ToolCoreLoaders.initializeAgent(tools, llm);
    }

    public static AgentExecutor initializeAgent(List<BaseTool> tools, BaseLanguageModel llm, boolean isCH) {
        return ToolCoreLoaders.initializeAgent(tools, llm, isCH);
    }

    public static AgentExecutor initializeStructuredAgent(List<BaseTool> tools, BaseLanguageModel llm) {
        return initializeStructuredAgent(tools, llm, false);
    }

    public static AgentExecutor initializeStructuredAgent(List<BaseTool> tools, BaseLanguageModel llm, boolean isCH) {
        AgentExecutor agentExecutor = new AgentExecutor();
        StructuredChatAgent agent = new StructuredChatAgent();
        agent.setLlm(llm);
        agent.setTools(tools);
        agent.init(isCH);
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
        return agentExecutor;
    }

    public static AgentExecutor initializeStructredAgentWithParser(List<BaseTool> tools, BaseLanguageModel llm, AgentOutputParser agentOutputParser) {
        AgentExecutor agentExecutor = new AgentExecutor();
        StructuredChatAgent agent = new StructuredChatAgent();
        agent.setLlm(llm);
        agent.setTools(tools);
        agent.setOutputParser(agentOutputParser);
        agent.init(true);
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
//        agentExecutor.setMaxIterations(1);
        return agentExecutor;
    }

    public static AgentExecutor initializeConversationalAgent(List<BaseTool> tools,
                                                              BaseLanguageModel llm,
                                                              BaseMemory memory) {
        return initializeConversationalAgent(tools, llm, memory, false);
    }

    public static AgentExecutor initializeConversationalAgent(List<BaseTool> tools,
                                                              BaseLanguageModel llm,
                                                              BaseMemory memory,
                                                              boolean isCH) {
        AgentExecutor agentExecutor = new AgentExecutor();
        Agent agent = ConversationalAgent.fromLlmAndTools(llm, tools,
                null, null, null, null, null,
                null, null, null, memory, isCH);
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
        return agentExecutor;
    }

    public static AgentExecutor initializeSelfAskWithSearchAgent(List<BaseTool> tools,
                                                                 BaseLanguageModel llm) {
        return initializeSelfAskWithSearchAgent(tools, llm, false);
    }

    public static AgentExecutor initializeSelfAskWithSearchAgent(List<BaseTool> tools,
                                                                 BaseLanguageModel llm,
                                                                 boolean isCH) {
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(SelfAskWithSearchAgent.createPrompt(tools, isCH));

        SelfAskWithSearchAgent agent = new SelfAskWithSearchAgent();
        agent.setIsCH(isCH);
        agent.setLlmChain(llmChain);
        agent.setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        agent.setOutputParser(new SelfAskOutputParser());

        AgentExecutor agentExecutor = new AgentExecutor();
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
        return agentExecutor;
    }

    public static AgentExecutor initializeReActDocstoreAgent(List<BaseTool> tools,
                                                             BaseLanguageModel llm) {
        return initializeReActDocstoreAgent(tools, llm, false);
    }

    public static AgentExecutor initializeReActDocstoreAgent(List<BaseTool> tools,
                                                             BaseLanguageModel llm,
                                                             boolean isCH) {
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(ReActDocstoreAgent.createPrompt(tools, isCH));

        ReActDocstoreAgent agent = new ReActDocstoreAgent();
        agent.setLlmChain(llmChain);
        agent.setAllowedTools(tools.stream().map(tool -> tool.getName()).collect(Collectors.toList()));
        agent.setOutputParser(new ReActOutputParser(isCH));

        AgentExecutor agentExecutor = new AgentExecutor();
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
        return agentExecutor;
    }

    public static AgentExecutor initializeStructuredChatAgentV2(List<BaseTool> tools,
                                                               BaseLanguageModel llm,
                                                               BaseMemory memory) {
        return initializeStructuredChatAgentV2(tools, llm, memory, false);
    }

    public static AgentExecutor initializeStructuredChatAgentV2(List<BaseTool> tools,
                                                              BaseLanguageModel llm,
                                                              BaseMemory memory,
                                                              boolean isCH) {
        return initializeStructuredChatAgentV2(tools, llm, memory, isCH, null, null);
    }

    public static AgentExecutor initializeStructuredChatAgentV2(List<BaseTool> tools,
                                                                BaseLanguageModel llm,
                                                                BaseMemory memory,
                                                                boolean isCH,
                                                                Agent agent) {
        return initializeStructuredChatAgentV2(tools, llm, memory, isCH, agent, null, null);
    }

    public static AgentExecutor initializeStructuredChatAgentV2(List<BaseTool> tools,
                                                                BaseLanguageModel llm,
                                                                BaseMemory memory,
                                                                boolean isCH,
                                                                Agent agent,
                                                                CallbackManager callbackManager) {
        return initializeStructuredChatAgentV2(tools, llm, memory, isCH, agent, null, callbackManager);
    }

    public static AgentExecutor initializeStructuredChatAgentV2(List<BaseTool> tools,
                                                                BaseLanguageModel llm,
                                                                BaseMemory memory,
                                                                boolean isCH,
                                                                String example) {
        return initializeStructuredChatAgentV2(tools, llm, memory, isCH, null, example, null);
    }

    public static AgentExecutor initializeStructuredChatAgentV2(List<BaseTool> tools,
                                                                BaseLanguageModel llm,
                                                                BaseMemory memory,
                                                                boolean isCH,
                                                                Agent agent,
                                                                String example,
                                                                CallbackManager callbackManager) {
        AgentExecutor agentExecutor = new AgentExecutor();
        agent = StructuredChatAgentV2.fromLlmAndTools(llm, tools, callbackManager,
                null, null, null, null, example, null, memory, isCH, agent);
        agentExecutor.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
        agentExecutor.setMemory(memory);
        agentExecutor.setCH(isCH);
        agentExecutor.setLlm(llm);
        return agentExecutor;
    }

    public static AgentExecutor initializeSemanticKernelAgent(List<BaseTool> tools, BaseLanguageModel llm, BasePlanner planner) {
        AgentExecutor agentExecutor = new AgentExecutor();
        Agent agent = SemanticKernelAgent.fromLlmAndTools(llm, tools, planner);
        agentExecutor.setAgent(agent);
        tools.forEach(tool -> tool.setName(tool.getName() + (!StringUtils.isEmpty(tool.getFunctionName()) ? ("." + tool.getFunctionName()) : "")));
        agentExecutor.setTools(tools);
        return agentExecutor;
    }
}

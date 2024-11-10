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
package com.alibaba.langengine.core.agent.planexecute;

import com.alibaba.langengine.core.agent.Agent;
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.planexecute.executors.ChainExecutor;
import com.alibaba.langengine.core.agent.planexecute.planners.LLMPlanner;
import com.alibaba.langengine.core.agent.planexecute.planners.PlanningOutputParser;
import com.alibaba.langengine.core.agent.structured.StructuredChatAgent;
import com.alibaba.langengine.core.agent.structured2.StructuredChatAgentV2;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.prompt.impl.HumanMessagePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.langengine.core.agent.planexecute.PromptConstants.*;

/**
 * 辅助类
 *
 * @author xiaoxuan.lp
 */
public class Utils {

    /**
     * Load a chat planner.
     *
     * @param llm
     * @param systemPrompt
     * @return
     */
    public static LLMPlanner loadChatPlanner(BaseLanguageModel llm,
                                             String systemPrompt) {
        return loadChatPlanner(llm, systemPrompt, false);
    }

    /**
     * Load a chat planner.
     *
     * @param llm
     * @param systemPrompt
     * @param isCH
     * @return
     */
    public static LLMPlanner loadChatPlanner(BaseLanguageModel llm,
                                             String systemPrompt,
                                             boolean isCH) {
        if(systemPrompt == null) {
            systemPrompt = (isCH ? PromptConstants.SYSTEM_PROMPT_CH : PromptConstants.SYSTEM_PROMPT);
        }

        List<Object> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent(systemPrompt);
        messages.add(systemMessage);

        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate("{input}");
        humanMessagePromptTemplate.setPrompt(promptTemplate);
        messages.add(humanMessagePromptTemplate);
        ChatPromptTemplate chatPromptTemplate = ChatPromptTemplate.fromMessages(messages);

        LLMChain llmChain =new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(chatPromptTemplate);

        LLMPlanner llmPlanner = new LLMPlanner();
        llmPlanner.setLlmChain(llmChain);
        llmPlanner.setOutputParser(new PlanningOutputParser());
        llmPlanner.setStop(Arrays.asList(new String[]{ "<END_OF_PLAN>" }));
        return llmPlanner;
    }

    /**
     * Load an agent executor.
     *
     * @param llm
     * @param tools
     * @param includeTaskInPrompt
     * @return
     */
    public static ChainExecutor loadAgentExecutor(BaseLanguageModel llm,
                                                  List<BaseTool> tools,
                                                  Boolean includeTaskInPrompt) {
        return loadAgentExecutor(llm, tools, includeTaskInPrompt, false);
    }

    /**
     * Load an agent executor.
     *
     * @param llm
     * @param tools
     * @param includeTaskInPrompt
     * @param isCH
     * @return
     */
    public static ChainExecutor loadAgentExecutor(BaseLanguageModel llm,
                                                  List<BaseTool> tools,
                                                  Boolean includeTaskInPrompt,
                                                  boolean isCH) {
        if(includeTaskInPrompt == null) {
            includeTaskInPrompt = false;
        }

        List<String> inputVariables = new ArrayList<>();
        inputVariables.add("previous_steps");
        inputVariables.add("current_step");
        inputVariables.add("agent_scratchpad");
        String template = (isCH ? HUMAN_MESSAGE_TEMPLATE_CH : HUMAN_MESSAGE_TEMPLATE);

        if(includeTaskInPrompt) {
            inputVariables.add("objective");
            template = TASK_PREFIX + template;
        }

        Agent agent = StructuredChatAgent.fromLlmAndTools(llm, tools, null,
                null, null, null, template,
                null, inputVariables, null);

//        Agent agent = StructuredChatAgentV2.fromLlmAndTools(llm, tools, null,
//                null, null, null, null, null, null, null, isCH, null);


        AgentExecutor agentExecutor = new AgentExecutor();
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);

        ChainExecutor chainExecutor = new ChainExecutor();
        chainExecutor.setChain(agentExecutor);

        return chainExecutor;
    }
}

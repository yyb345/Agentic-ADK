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
package com.alibaba.langengine.agentframework.delegation.cotexecutor;

import com.alibaba.langengine.agentframework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.outputparser.CotPlanningOutputParser;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.support.MessageBuildingUtils;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.support.ToolBuildingUtils;
import com.alibaba.langengine.agentframework.delegation.cotexecutor.tools.ComponentTool;
import com.alibaba.langengine.agentframework.utils.AgentResponseUtils;
import com.alibaba.langengine.agentframework.config.ComponentStreamCallback;
import com.alibaba.langengine.agentframework.delegation.constants.CotCallingConstant;
import com.alibaba.langengine.agentframework.utils.IdGeneratorUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.agent.domain.ComponentCallingInput;
import com.alibaba.langengine.agentframework.model.domain.*;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelGetRequest;
import com.alibaba.langengine.agentframework.model.service.response.LanguageModelGetResponse;
import com.alibaba.langengine.core.agent.planexecute.ListStepContainer;
import com.alibaba.langengine.core.agent.planexecute.Plan;
import com.alibaba.langengine.core.agent.planexecute.Step;
import com.alibaba.langengine.core.agent.planexecute.StepResponse;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 计划与执行执行器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class PlanAndExecuteExecutor extends BaseCotExecutor implements CotCallingConstant {

    @Override
    public Map<String, Object> callLlm(FrameworkSystemContext systemContext,
                                       FrameworkCotCallingDelegation delegation,
                                       String knowledgeContext) {
        Long startTime = System.currentTimeMillis();
        log.info("PlanAndExecuteExecctor startTime:" + startTime);
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();

        List<BaseTool> tools = new ArrayList<>();
        List<FunctionDefinition> functions = new ArrayList<>();
        ToolBuildingUtils.buildTool(systemContext, tools, functions, startTime, delegation);

        List<BaseMessage> messages = new ArrayList<>();
        String rolePrompt = PLAN_AND_EXECUTE_SYSTEM_PROMPT;
//        if(!CollectionUtils.isEmpty(systemContext.getAgentRelation().getComponentList())) {
//            rolePrompt += "\n\n## 工具集合\n";
//            for (ComponentCallingInput component : systemContext.getAgentRelation().getComponentList()) {
//                rolePrompt += "- " + component.getComponentId() + ":" + component.getComponentDesc() + ", parameters:" + JSON.toJSONString(component.getInputParams()) + "\n";
//            }
//        }
        List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, rolePrompt, null);
        ChatPromptTemplate planPrompt = ChatPromptTemplate.fromChatMessages(messages);

        LlmTemplateConfig llmTemplateConfig = convertCotLlmTemplateConfig(delegation.getCotLlmTemplateConfig(), false, systemContext, delegation);
        LanguageModelGetRequest request = new LanguageModelGetRequest();
        request.setLlmTemplateConfig(llmTemplateConfig);
        request.setSystemContext(systemContext);
        request.setFlag("cotLlmFetch");
        AgentResult<LanguageModelGetResponse> agentResult = delegation.getLanguageModelService().getLanguageModel(request);
        if(!agentResult.isSuccess()) {
            throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, agentResult.getErrorMsg(), systemContext.getRequestId());
        }
        BaseLanguageModel model = agentResult.getData().getLanguageModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("functions", functions);
            put("stop", Arrays.asList(new String[] { "<END_OF_PLAN>" }));
        }});

        RunnableInterface planChain = Runnable.sequence(
                planPrompt,
                modelBinding
        );

        RunnableHashMap inputs = new RunnableHashMap();
        Object runnableOutput = planChain.invoke(inputs);
        if(runnableOutput == null) {
            throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR,
                    "PlanAndExecute callLlm error, output is empty",
                    systemContext.getRequestId());
        }
        if(!(runnableOutput instanceof AIMessage)) {
            throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR,
                    "PlanAndExecute callLlm error, output is not aiMessage",
                    systemContext.getRequestId());
        }

        AIMessage aiMessage = (AIMessage) runnableOutput;
        log.info("PlanAndExecuteExecutor aiMessage:" + JSON.toJSONString(aiMessage));
        if(!StringUtils.isEmpty(aiMessage.getContent())) {
            CotPlanningOutputParser planningOutputParser = new CotPlanningOutputParser();
            RunnableHashMap planMap = planningOutputParser.parse(aiMessage.getContent());
            if(planMap == null || planMap.get("plan") == null) {

                String content = aiMessage.getContent();
                AgentAPIInvokeResponse agentAPIResponse = new AgentAPIInvokeResponse();
                ChatMessage message = new ChatMessage();
                agentAPIResponse.getMessage().add(message);

                String messageId = IdGeneratorUtils.nextId();
                String sectionId = IdGeneratorUtils.nextId();
                message.setRole(ChatMessageRole.ASSISTANT.value());
                message.setType(ChatMessage.TYPE_ANSWER);
                message.setContent(content);
                message.setMessageId(messageId);
                message.setSectionId(sectionId);
                message.setSessionId(systemContext.getSessionId());
                message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
                message.setSenderId(systemContext.getUserId());
                message.getExtraInfo().setTimeCost(AgentResponseUtils.getTimeCost(startTime));

                AgentAPIResult<AgentAPIInvokeResponse> apiResult = AgentAPIResult.success(agentAPIResponse, systemContext.getRequestId());
                if(chunkConsumer != null) {
                    delegation.onStreamNext(systemContext, apiResult);
                }

                return new HashMap<String, Object>() {{
                    put("output", aiMessage.getContent());
                }};
            }

            Plan plan = (Plan) planMap.get("plan");
            log.info("plan:" + JSON.toJSONString(plan));

            ListStepContainer stepContainer = new ListStepContainer();
            Map<String, Object> planPunnableOutput = new HashMap<>();
            int index = 0;
            int total = plan.getSteps().size();
            for (Step step : plan.getSteps()) {
                boolean forceStream = false;
                if(index + 1 == total) {
                    forceStream = true;
                }
                planPunnableOutput = callLLmWithExecute(systemContext, delegation, knowledgeContext, stepContainer, step, forceStream);
                log.info("executeChain response:" + JSON.toJSONString(planPunnableOutput));
                StepResponse stepResponse = new StepResponse();
                stepResponse.setResponse(planPunnableOutput.get("output").toString());
                stepContainer.addStep(step, stepResponse);
                log.info("stepContainer is " + JSON.toJSONString(stepContainer));
                index++;
            }
            return planPunnableOutput;
        } else if(aiMessage.getAdditionalKwargs() != null && aiMessage.getAdditionalKwargs().get("function_call") != null) {
            Map<String, Object> functionCall = (Map<String, Object>)aiMessage.getAdditionalKwargs().get("function_call");
            String name = (String)functionCall.get("name");
            String arguments = (String) functionCall.get("arguments");

            List<ComponentCallingInput> componentList = systemContext.getAgentRelation().getComponentList();
            ComponentTool tool = new ComponentTool();
            if(componentList != null && componentList.size() > 0) {
                List<ComponentCallingInput> filterComponentList = componentList.stream().filter(e -> e.getComponentId().equals(name))
                        .collect(Collectors.toList());
                ComponentCallingInput component = filterComponentList.get(0);
                tool.setName(component.getComponentId());
                tool.setDescription(component.getComponentDesc());
                tool.setFunctionName(component.getComponentVersion());
                tool.setToolCallingService(delegation.getToolCallingService());
                tool.setMessageConsumer(systemContext.getChunkConsumer());
                tool.setRequestId(systemContext.getRequestId());
                tool.setUserId(systemContext.getUserId());
                tool.setApikeyCall(systemContext.getApikeyCall());
                tool.setApiKey(systemContext.getApikey());
                tool.setEnv(systemContext.getEnv());
                tool.setCallbackManager(new CallbackManager());
                tool.getCallbackManager().addHandler(new ComponentStreamCallback());
                tool.setSystemContext(systemContext);

                ToolExecuteResult toolExecuteResult = tool.execute(arguments);

                FunctionMessage functionMessage = new FunctionMessage();
                functionMessage.setName(component.getComponentId());
                functionMessage.setContent(toolExecuteResult.getOutput());
                List<BaseMessage> intermediateMessages = new ArrayList<>();
                intermediateMessages.add(aiMessage);
                intermediateMessages.add(functionMessage);

                return super.callLlm(systemContext, delegation, knowledgeContext, intermediateMessages);
            }
        }

        throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR,
                "PlanAndExecute callllm error, output aiMessage is not valid",
                systemContext.getRequestId());
    }

    private Map<String, Object> callLLmWithExecute(FrameworkSystemContext systemContext,
                                                   FrameworkCotCallingDelegation delegation,
                                                   String knowledgeContext,
                                                   ListStepContainer stepContainer,
                                                   Step step,
                                                   Boolean forceStream) {
        systemContext.setForceStream(forceStream);

        AgentRelation agentRelation = systemContext.getAgentRelation();

        // 获取角色prompt
        String rolePrompt = agentRelation.getRolePrompt();
        if (StringUtils.isEmpty(rolePrompt)) {
            rolePrompt = SYSTEM_PROMPT;
        }

        String stepPrompt = "\n\nPrevious steps: {previous_steps}\n" +
                "\n" +
                "Current objective: {current_step}\n" +
                "\n";
        Map<String, Object> inputs = new HashMap<String, Object>() {{
            put("previous_steps", stepContainer.getSteps().stream().map(s -> s.getStepResponse().getResponse())
                    .collect(Collectors.joining("\n")));
            put("current_step", step.getValue());
        }};
        rolePrompt += PromptConverter.replacePrompt(stepPrompt, inputs);

        // 单独增加知识库记忆上下文
        if(!StringUtils.isEmpty(knowledgeContext)) {
            rolePrompt += "\n\n## 以下是你已知的知识内容\n - " + knowledgeContext + "\n";
        }

        Long startTime = System.currentTimeMillis();
        log.info("PlanAndExecuteExecutor executor startTime:" + startTime);

        LlmTemplateConfig llmTemplateConfig = convertCotLlmTemplateConfig(delegation.getCotLlmTemplateConfig(), false, systemContext, delegation);
        LanguageModelGetRequest request = new LanguageModelGetRequest();
        request.setLlmTemplateConfig(llmTemplateConfig);
        request.setSystemContext(systemContext);
        request.setFlag("cotLlmFetch");
        AgentResult<LanguageModelGetResponse> agentResult = delegation.getLanguageModelService().getLanguageModel(request);
        if(!agentResult.isSuccess()) {
            throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, agentResult.getErrorMsg(), systemContext.getRequestId());
        }
        BaseLanguageModel model = agentResult.getData().getLanguageModel();

        // prompt
        List<BaseTool> tools = new ArrayList<>();
        List<FunctionDefinition> functions = new ArrayList<>();
        ToolBuildingUtils.buildTool(systemContext, tools, functions, startTime, delegation);

        List<BaseMessage> messages = new ArrayList<>();
        List<BaseMessage> historyMessages = MessageBuildingUtils.buildMessageReturnHistory(systemContext, messages, delegation, knowledgeContext, rolePrompt, null);
        ChatPromptTemplate executePrompt = ChatPromptTemplate.fromChatMessages(messages);

        if(functions.size() == 0) {
            return invokeLangRunnableChain(executePrompt, model, llmTemplateConfig, historyMessages, startTime, systemContext, delegation);
        } else {
            return invokeLangRunnableAgent(executePrompt, model, functions, tools, llmTemplateConfig, historyMessages, startTime, systemContext, delegation);
        }
    }
}

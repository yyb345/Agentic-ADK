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
package com.alibaba.agentmagic.framework.delegation;

import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.agentmagic.framework.delegation.provider.DelegationHelper;
import com.alibaba.agentmagic.framework.domain.ProcessExecuteLog;
import com.alibaba.agentmagic.framework.manager.AgentTaskInstanceManager;
import com.alibaba.agentmagic.framework.utils.SeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.dataobject.AgentProcessInstanceConstant;
import com.alibaba.langengine.agentframework.model.dataobject.AgentTaskInstanceDO;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.agentframework.model.domain.ProcessSystemContext;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.*;
import com.alibaba.langengine.agentframework.model.service.request.LanguageModelCallRequest;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.FunctionMessage;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.persister.util.InstanceSerializerFacade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.langengine.agentframework.model.constant.ProcessConstants.ACTIVITY_VARIABLE_PREFIX;
import static com.alibaba.langengine.agentframework.model.constant.ProcessConstants.OUT_PARALLEL_START_KEY;

/**
 * 所有委托的节点执行基类
 *
 * @param <T>
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public abstract class FrameworkDelegationBase<T> implements JavaDelegation {

    public LanguageModelCallRequest getLanguageModelCallRequest() {
        return new LanguageModelCallRequest();
    }

    @Resource
    protected FrameworkEngineConfiguration agentEngineConfiguration;

    public RetrievalService getRetrievalService() {
        return agentEngineConfiguration.getRetrievalService();
    }

    public LanguageModelService getLanguageModelService() {
        return agentEngineConfiguration.getLanguageModelService();
    }

    public String getCotLlmTemplateConfig() {
        return agentEngineConfiguration.getCotLlmTemplateConfig();
    }

    public String getLlmTemplateConfig() {
        return agentEngineConfiguration.getLlmTemplateConfig();
    }

    public Integer getCotRetryCount() {
        return agentEngineConfiguration.getCotRetryCount();
    }

    public String getCotFallbackLlmTemplateConfig() {
        return agentEngineConfiguration.getCotFallbackLlmTemplateConfig();
    }

    public Boolean getSysPromptContainFunctionEnabled() {
        return agentEngineConfiguration.getSysPromptContainFunctionEnabled();
    }

    public ToolCallingService getToolCallingService() {
        return agentEngineConfiguration.getToolCallingService();
    }

    public MemoryService getMemoryService() {
        return agentEngineConfiguration.getMemoryService();
    }

    public RankService getRankService() {
        return agentEngineConfiguration.getRankService();
    }

    public ScriptService getScriptService() {
        return agentEngineConfiguration.getScriptService();
    }

    public DynamicScriptService getDynamicScriptService() {
        return agentEngineConfiguration.getDynamicScriptService();
    }

    public String getGrayStrategyConfig() {
        return agentEngineConfiguration.getGrayStrategyConfig();
    }

    public FrameworkEngineConfiguration getAgentEngineConfiguration() {
        return agentEngineConfiguration;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        ProcessExecuteLog.start(executionContext);

        String processDefinitionId = executionContext.getProcessDefinition().getId();
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        String grayStrategyConfig = agentEngineConfiguration.getGrayStrategyConfig();

        JSONObject requestJson = getRequest(executionContext);
        JSONObject propertiesJson = getProperties(executionContext);
        JSONObject realPropertiesJson = DelegationHelper.preProcessActivityContext(executionContext, requestJson, propertiesJson, grayStrategyConfig);

        try {
            // 记录调试日志
            updateInputProcessExecuteLog(executionContext, realPropertiesJson);

            // 调用节点执行方法
            T result = executeInternal(executionContext, realPropertiesJson, requestJson);
            if (result != null) {
                JSONObject responseJson = getResponse(executionContext);
                saveRequestContext(executionContext, realPropertiesJson, requestJson, responseJson, result);
            }
        } catch (AgentMagicException exception) {
            throw exception;
        } catch (Throwable throwable) {
            String requestId = DelegationHelper.getSystemString(requestJson, SystemConstant.REQUEST_ID_KEY);
            log.error("delegation[" + processDefinitionId + "," + activityId + "] execute error", throwable);
            throw new AgentMagicException(AgentMagicErrorCode.SYSTEM_ERROR, throwable, requestId);
        }
    }

    /**
     * 节点执行
     * @param executionContext
     * @param properties 节点属性（含上下文会填）
     * @param request 节点请求上下文
     */
    public abstract T executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request);

    /**
     * 记录调试日志，子节点自己实现
     * @param executionContext
     * @param properties
     */
    protected void updateInputProcessExecuteLog(ExecutionContext executionContext, JSONObject properties) {

    }

    protected void saveRequestContext(ExecutionContext executionContext, JSONObject properties,
                                      JSONObject request, JSONObject response, T param) {
        DelegationHelper.saveRequestContext(executionContext, properties, request, response, param);
    }

    /**
     * 从BpmnXml上下文获取Json属性列表
     *
     * @param executionContext
     * @return
     */
    protected JSONObject getProperties(ExecutionContext executionContext) {
        return DelegationHelper.getProperties(executionContext);
    }

    /**
     * 从调用中获取上下文变量值
     *
     * @param executionContext
     * @return
     */
    protected JSONObject getRequest(ExecutionContext executionContext) {
        return DelegationHelper.getRequest(executionContext);
    }

    protected JSONObject getResponse(ExecutionContext executionContext) {
        return DelegationHelper.getResponse(executionContext);
    }

    public List<String> extractQuestions(String text) {
        List<String> questionsList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d\\.\\s(.*?)(?=\\n\\d\\.\\s|$)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            questionsList.add(matcher.group(1).trim());
        }
        return questionsList;
    }

    public BaseMessage convertMessageIntermediateSteps(List<AgentAction> intermediateSteps) {
        if(intermediateSteps.size() == 0) {
            return null;
        }
        FunctionMessage functionMessage = new FunctionMessage();
        for (AgentAction agentAction : intermediateSteps) {
            functionMessage.setName(agentAction.getTool());
            functionMessage.setContent(agentAction.getObservation());
        }
        return functionMessage;
    }

    public int persistProcessInstanceTask(AgentTaskInstanceManager agentTaskInstanceManager,
                                          ExecutionContext executionContext,
                                          JSONObject request,
                                          FrameworkSystemContext systemContext,
                                          String taskId) {
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        String processInstanceId = executionContext.getExecutionInstance().getProcessInstanceId();
        String agentCode = systemContext.getAgentCode();
        String userId = systemContext.getUserId();
        String parallelStartActivityId = request.getString(OUT_PARALLEL_START_KEY);
        // 记录异步节点
        ProcessSystemContext processSystemContext = SeUtils.getProcessSystemContext(request);
        processSystemContext.putCallbackNode(executionContext.getActivityInstance().getProcessDefinitionActivityId());
        // 记录持久化
        processSystemContext.setHasRecord(true);
        executionContext.getRequest().put(ProcessSystemContext.KEY, processSystemContext);

        // 进入节点时，保存节点状态
        AgentTaskInstanceDO processInstanceDO = new AgentTaskInstanceDO();
        processInstanceDO.setAgentCode(agentCode);
        if(!StringUtils.isEmpty(parallelStartActivityId)) {
            processInstanceDO.setParallelStartActivityId(parallelStartActivityId);
            processInstanceDO.setHasParallel(1);
        } else {
            processInstanceDO.setHasParallel(0);
        }
        processInstanceDO.setTaskId(taskId);
        processInstanceDO.setProcessInstanceId(processInstanceId);
        processInstanceDO.setContext(getPersistContext(executionContext.getRequest()));

        processInstanceDO.setProcessDefinitionId(executionContext.getProcessDefinition().getId());
        processInstanceDO.setProcessDefinitionVersion(Integer.valueOf(executionContext.getProcessDefinition().getVersion()));
        processInstanceDO.setActivityId(activityId);

        ProcessInstance processInstance = executionContext.getProcessInstance();
        String serializedProcessInstance = InstanceSerializerFacade.serialize(processInstance);
        processInstanceDO.setSerializedProcessInstance(serializedProcessInstance);
        processInstanceDO.setProcessInstanceId(processInstanceId);
        // 初始化为暂停状态
        processInstanceDO.setStatus(AgentProcessInstanceConstant.STATUS_PAUSE);
        processInstanceDO.setNeedRetry(0);
        processInstanceDO.setRetryTimes(0);
        processInstanceDO.setOwnerId(userId);
        Date now = new Date();
        processInstanceDO.setGmtCreate(now);
        processInstanceDO.setGmtModified(now);
        log.info("processInstance addTaskInstance request is " + JSON.toJSONString(processInstanceDO));
        int num = agentTaskInstanceManager.addTaskInstance(processInstanceDO);
        return num;
    }

    private String getPersistContext(Map<String, Object> request) {
        Map<String, Object> persistContextMap = new HashMap<>(request);
        Iterator<Map.Entry<String, Object>> iterator = persistContextMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getKey().startsWith("log_node_")) {
                iterator.remove();
            }
        }
        return JSON.toJSONString(persistContextMap);
    }

    public Object getActivityVariable(FrameworkSystemContext systemContext, JSONObject request, String activityId) {
        // 如果是非异步，直接返回空
        if(systemContext.getAsync() != null && !systemContext.getAsync()) {
            return null;
        }
        return request.get(ACTIVITY_VARIABLE_PREFIX + activityId);
    }

    public Object getActivityVariableIfContain(FrameworkSystemContext systemContext, JSONObject request, String activityId) {
        // 如果是非异步，直接返回空
        if (systemContext.getAsync() != null && !systemContext.getAsync()) {
            return null;
        }
        // 如果不包含并行节点变量，直接返回空
        if (request.get(OUT_PARALLEL_START_KEY) == null) {
            return null;
        }
        return request.get(ACTIVITY_VARIABLE_PREFIX + activityId);
    }
}

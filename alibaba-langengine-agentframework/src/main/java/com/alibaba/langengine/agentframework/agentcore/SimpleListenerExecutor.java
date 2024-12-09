package com.alibaba.langengine.agentframework.agentcore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.constants.ProcessConstant;
import com.alibaba.langengine.agentframework.delegation.constants.SystemConstant;
import com.alibaba.langengine.agentframework.delegation.provider.AgentTraceHelper;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.domain.ProcessExecuteLog;
import com.alibaba.langengine.agentframework.manager.AgentProcessDefineManager;
import com.alibaba.langengine.agentframework.model.dataobject.AgentProcessDefineDO;
import com.alibaba.langengine.agentframework.model.domain.ProcessSystemContext;
import com.alibaba.smart.framework.engine.bpmn.assembly.event.EndEvent;
import com.alibaba.smart.framework.engine.bpmn.assembly.event.StartEvent;
import com.alibaba.smart.framework.engine.common.util.CollectionUtil;
import com.alibaba.smart.framework.engine.configuration.InstanceAccessor;
import com.alibaba.smart.framework.engine.configuration.ListenerExecutor;
import com.alibaba.smart.framework.engine.constant.ExtensionElementsConstant;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.listener.Listener;
import com.alibaba.smart.framework.engine.listener.ListenerAggregation;
import com.alibaba.smart.framework.engine.model.assembly.BaseElement;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElementContainer;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElements;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinition;
import com.alibaba.smart.framework.engine.model.instance.ExecutionInstance;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.persister.custom.session.PersisterSession;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import com.alibaba.smart.framework.engine.service.command.ExecutionCommandService;
import com.alibaba.smart.framework.engine.service.command.RepositoryCommandService;
import com.alibaba.smart.framework.engine.service.query.ExecutionQueryService;
import com.alibaba.smart.framework.engine.service.query.RepositoryQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.agentframework.model.constant.ProcessConstants.ACTIVITY_VARIABLE_PREFIX;

@Slf4j
@Component
public class SimpleListenerExecutor implements ListenerExecutor {

    @Resource
    private AgentProcessDefineManager agentProcessDefineManager;

    @Override
    public void execute(PvmEventConstant event, ExtensionElementContainer extensionElementContainer, ExecutionContext context) {
        BaseElement baseElement = context.getBaseElement();
        if(event.equals(PvmEventConstant.PROCESS_START) || event.equals(PvmEventConstant.PROCESS_END) ){
            JSONObject requestJson = new JSONObject(context.getRequest());
            if(requestJson.containsKey(ProcessConstant.SYSTEM)) {
                requestJson.getJSONObject(ProcessConstant.SYSTEM).put(ProcessConstant.PROCESS_INSTANCE_ID,context.getProcessInstance().getInstanceId());
            }

            if(event.equals(PvmEventConstant.PROCESS_START)) {
                AgentTraceHelper.traceProcessStart(context);
            } else if(event.equals(PvmEventConstant.PROCESS_END)) {
                log.info("process end with " + context.getProcessDefinition().getId() + ":" + context.getProcessDefinition().getVersion());
                AgentTraceHelper.traceProcessEnd(context);

                // 工作流结束标记
//                context.getResponse().put(ProcessConstants.SYS_PROCESS_RESPONSE_KEY, true);
            }
            return;
        }
        if(baseElement instanceof StartEvent){
            handleStartEvent(event, extensionElementContainer, context);
        } else if(baseElement instanceof EndEvent){
            handleEndEvent(event, extensionElementContainer, context);
        }else {
            handleOtherEvent(event, extensionElementContainer, context);
        }
    }

    /**
     * 开始节点
     * @param event
     * @param extensionElementContainer
     * @param context
     */
    private void handleStartEvent(PvmEventConstant event, ExtensionElementContainer extensionElementContainer, ExecutionContext context) {
        if (event != PvmEventConstant.ACTIVITY_START && event != PvmEventConstant.ACTIVITY_END) {
            return;
        }
        // 进入时，添加系统参数
        if(event.equals(PvmEventConstant.ACTIVITY_START)) {
            AgentTraceHelper.traceNodeStart(context, "Start");
            ProcessExecuteLog.start(context);

            String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
            JSONObject propertiesJson = DelegationHelper.getProperties(context);
            // 当propertiesJson不为空时，获得inputParameters字段
            if(propertiesJson != null && propertiesJson.containsKey(SystemConstant.INPUT_PARAMETERS_KEY)) {
                JSONObject requestJson = DelegationHelper.getRequest(context);

                JSONObject inputParameters = propertiesJson.getJSONArray(SystemConstant.INPUT_PARAMETERS_KEY).getJSONObject(0);

                DelegationHelper.replaceJson(inputParameters, requestJson);

                if(inputParameters != null) {
                    for(String key : inputParameters.keySet()) {
                        //外部传入的全局变量更新
                        if(requestJson.get(key) != null) {
                            inputParameters.put(key, requestJson.get(key));
                        }
                        // 将全局变量放入request中
                        context.getRequest().put(key,inputParameters.get(key));
                    }
                    context.getRequest().put(ACTIVITY_VARIABLE_PREFIX + activityId, inputParameters);
                }
            }

            ProcessSystemContext processSystemContext = new ProcessSystemContext();
            processSystemContext.setProcessInstanceId(context.getProcessInstance().getInstanceId());
            context.getRequest().put(ProcessSystemContext.KEY,processSystemContext);
        } else if(event.equals(PvmEventConstant.ACTIVITY_END)) {
            AgentTraceHelper.traceNodeEnd(context);
            this.setQueryForRequest(context);
            ProcessExecuteLog.stop(context);
        }
    }

    private void handleEndEvent(PvmEventConstant event, ExtensionElementContainer extensionElementContainer, ExecutionContext context) {
        // TODO 将finalOutputParams的参数添加到response中
        if(event == PvmEventConstant.ACTIVITY_START) {
            String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
            // 写入finalOutputParameters
            Map<String, Object> endResponse = DelegationHelper.saveFinalRequestContext(context);
            context.getRequest().put(ACTIVITY_VARIABLE_PREFIX + activityId, endResponse);

            AgentTraceHelper.traceNodeStart(context, "End");
            ProcessExecuteLog.start(context);
            return;
        } else if(event == PvmEventConstant.ACTIVITY_END) {
            AgentTraceHelper.traceNodeEnd(context);
            this.setQueryForRequest(context);
            ProcessExecuteLog.stop(context);
        }
    }

    private void handleOtherEvent(PvmEventConstant event, ExtensionElementContainer extensionElementContainer, ExecutionContext context) {
        String eventName = event.name();
        ExtensionElements extensionElements = extensionElementContainer.getExtensionElements();
        if(null != extensionElements){
            ListenerAggregation extension = (ListenerAggregation)extensionElements.getDecorationMap().get(
                    ExtensionElementsConstant.EXECUTION_LISTENER);

            if(null !=  extension){
                List<String> listenerClassNameList = extension.getEventListenerMap().get(eventName);
                if(CollectionUtil.isNotEmpty(listenerClassNameList)){
                    InstanceAccessor instanceAccessor = context.getProcessEngineConfiguration()
                            .getInstanceAccessor();
                    for (String listenerClassName : listenerClassNameList) {

                        Listener listener = (Listener)instanceAccessor.access(listenerClassName);
                        listener.execute(event, context);
                    }
                }
            }
        }

        if(event.equals(PvmEventConstant.ACTIVITY_START)) {
            AgentTraceHelper.traceNodeStart(context);
            ProcessExecuteLog.start(context);
        } else if(event.equals(PvmEventConstant.ACTIVITY_END)) {
            AgentTraceHelper.traceNodeEnd(context);
            ProcessExecuteLog.stop(context);
        }
    }

    public void deployProcessDefinition(ExecutionContext context, String defineId, Integer version) {
        RepositoryQueryService repositoryQueryService = context.getProcessEngineConfiguration().getSmartEngine().getRepositoryQueryService();
        try {
            ProcessDefinition processDefinition = repositoryQueryService.getCachedProcessDefinition(defineId, String.valueOf(version));
            if(processDefinition != null) {
                return;
            }
            RepositoryCommandService repositoryCommandService = context.getProcessEngineConfiguration().getSmartEngine().getRepositoryCommandService();
            AgentProcessDefineDO processDefineDO = agentProcessDefineManager.getByProcessDefinitionIdAndVersion(defineId, version);
            if(processDefineDO == null) {
                throw new EngineException(String.format("not found ProcessDefine,processDefinitionId=%s,version=%s", defineId, version));
            }
            repositoryCommandService.deployWithUTF8Content(processDefineDO.getProcessDefinitionContent());
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    private void setQueryForRequest(ExecutionContext context) {
        JSONObject request = DelegationHelper.getRequest(context);
        String query = DelegationHelper.getSystemString(request, SystemConstant.QUERY_KEY);

        Map<String, Object> requestParam = new HashMap<>();
        try {
            if(StringUtils.isEmpty(query)) {
                String globalVariablesStr = JSON.toJSONString(DelegationHelper.getSystem(request, SystemConstant.GLOBAL_VARIABLES_KEY));
                if(StringUtils.isEmpty(globalVariablesStr)) {
                    return;
                }
                JSONArray globalVariables = JSON.parseArray(globalVariablesStr);
                if(CollectionUtils.isEmpty(globalVariables)) {
                    return;
                }
                for (int i = 0; i < globalVariables.size(); i++) {
                    JSONObject json = globalVariables.getJSONObject(i);
                    String name = json.getString("variableName");
                    if(!request.containsKey(name)) {
                        continue;
                    }
                    requestParam.put(name, request.getString(name));
                }
            } else {
                requestParam.put("query", query);
            }
        } catch (Throwable e) {
            log.error("setQueryForRequest error, ", e);
        }
        Map<String, Object> enirchMap = new HashMap<>();
        enirchMap.put("nodeRequest", requestParam);
        ProcessExecuteLog.update(context, enirchMap);
    }
}

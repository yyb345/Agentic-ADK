package com.alibaba.langengine.agentframework.delegation.core;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.delegation.constants.SystemConstant;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.domain.ProcessExecuteLog;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 委托的节点执行基类
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public abstract class DelegationBase implements JavaDelegation {

    @Override
    public void execute(ExecutionContext executionContext) {
        ProcessExecuteLog.start(executionContext);

        String processDefinitionId = executionContext.getProcessDefinition().getId();
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();

        JSONObject requestJson = getRequest(executionContext);
        JSONObject propertiesJson = getProperties(executionContext);
        JSONObject realPropertiesJson = DelegationHelper.preProcessActivityContext(executionContext, requestJson, propertiesJson, null);

        try {
            // 记录调试日志
            updateInputProcessExecuteLog(executionContext, realPropertiesJson);

            // 调用节点执行方法
            Map<String, Object> result = executeInternal(executionContext, realPropertiesJson, requestJson);
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
    public abstract Map<String, Object> executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request);

    /**
     * 记录调试日志，子节点自己实现
     * @param executionContext
     * @param properties
     */
    protected void updateInputProcessExecuteLog(ExecutionContext executionContext, JSONObject properties) {

    }

    protected void saveRequestContext(ExecutionContext executionContext, JSONObject properties,
                                      JSONObject request, JSONObject response, Map<String, Object> param) {
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

    public String getPropertyString(JSONObject properties, String key, String defaultValue) {
        String propertyString = properties.getString(key);
        if(propertyString == null) {
            propertyString = defaultValue;
        }
        return propertyString;
    }
}

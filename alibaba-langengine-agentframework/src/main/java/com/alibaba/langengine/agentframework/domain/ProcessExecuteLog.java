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
package com.alibaba.langengine.agentframework.domain;

import com.alibaba.langengine.agentframework.config.ApplicationContextUtil;
import com.alibaba.langengine.agentframework.delegation.constants.SystemConstant;
import com.alibaba.langengine.agentframework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.utils.FrameworkUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.service.LoggingService;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.ConditionExpression;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

/**
 * 工作流执行日志
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class ProcessExecuteLog {

    public static final String LOG_NODE_PREFIX = "log_node_";
    private static final Logger DEFAULT_LOG = LoggerFactory.getLogger(DefaultLog.class);
    private static final Logger LOG = LoggerFactory.getLogger(ProcessExecuteLog.class);
    private static final String SPLIT = "#!$";

    @Value("${com.alibaba.agentmagic.sls.enable}")
    private static String enableSlsClientLog;


    private Long startTime;
    private Long endTime;
    private String agentCode;
    private String agentVersion;
    private String agentName;
    private String tenantCode;
    private Boolean componentAsync;
    private String query;
    private String sessionId;
    private String processDefineId;
    private String processDefineVersion;
    private String processDefineName;
    private String processInstanceId;
    private String activityId;
    private String activityName;
    private String activityType;
    private Boolean stream;
    private Long executeTime;
    private String requestId;
    private String request;
    private String response;
    private Boolean success;
    private String errorCode;
    private String errorMessage;
    private String errorDetail;
    private String templateType;
    private String parentTemplateType;
    private String firstTokenTime;
    private String firstTokenCost;
    private String costToken;
    private String modelName;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb,"agentCode", agentCode);
        append(sb,"agentName", agentName);
        append(sb,"tenantCode", tenantCode);
        append(sb,"componentAsync", componentAsync);
        append(sb,"query", query);
        append(sb,"sessionId", sessionId);
        append(sb,"processDefineId", processDefineId);
        append(sb,"processDefineVersion", processDefineVersion);
        append(sb,"processDefineName", processDefineName);
        append(sb,"processInstanceId", processInstanceId);
        append(sb,"activityId", activityId);
        append(sb,"activityName", activityName);
        append(sb,"activityType", activityType);
        append(sb,"stream", stream);
        append(sb,"executeTime", executeTime);
        append(sb,"requestId", requestId);
        append(sb,"request", request);
        append(sb,"response", response);
        append(sb,"success", success);
        append(sb,"errorCode", errorCode);
        append(sb,"errorMessage", errorMessage);
        append(sb,"errorDetail", errorDetail, Arrays.asList(new String[] { "\n", "\r" }));
        append(sb,"modelName", modelName);
        append(sb,"startTime", startTime);
        append(sb,"endTime", endTime);
        append(sb,"agentVersion", agentVersion);
        append(sb,"firstTokenTime", firstTokenTime);
        append(sb,"firstTokenCost", firstTokenCost);
        append(sb,"templateType", templateType);
        append(sb,"parentTemplateType", parentTemplateType);
        append(sb,"costToken", costToken, true);
        return sb.toString();
    }

    private void append(StringBuilder sb,String key, Object value) {
        append(sb,key, value, null, false);
    }

    private void append(StringBuilder sb,String key, Object value, List<String> splitChars) {
        append(sb,key, value, splitChars, false);
    }

    private void append(StringBuilder sb, String key, Object value, boolean last) {
        append(sb, key, value, null, last);
    }

    private void append(StringBuilder sb, String key, Object value, List<String> splitChars, boolean last) {
        if (value == null) {
            value = "null";
        }
//        value = StringEscapeUtils.escapeJava(value.toString());
        if(splitChars != null && splitChars.size() > 0) {
            for (String splitChar : splitChars) {
                value = value.toString().replace(splitChar, "");
            }
        }
        sb.append(value);
//                .replace("\n", "")
//                .replace("\r","")
//                .replace("=","[eq]")
//                .replace("|"," "));
        if (!last) {
            sb.append(SPLIT);
        }
    }

    public static void start(ExecutionContext context) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        String activityName = FrameworkUtils.getActivityName(context, activityId);
        if (StringUtils.isEmpty(activityName)) {
            activityName = activityId;
        }
        String activityType = FrameworkUtils.getActivityType(context);

        JSONObject request = DelegationHelper.getRequest(context);
        String agentCode = DelegationHelper.getSystemString(request, SystemConstant.AGENT_CODE_KEY);
        String agentVersion = DelegationHelper.getSystemString(request, SystemConstant.AGENT_VERSION_KEY);
        String agentName = DelegationHelper.getSystemString(request, SystemConstant.AGENT_NAME_KEY);
        String tenantCode = DelegationHelper.getSystemString(request, SystemConstant.TENANT_CODE_KEY);
        Boolean componentAsync = DelegationHelper.getSystemBoolean(request, SystemConstant.COMPONENT_ASYNC);
        String query = DelegationHelper.getSystemString(request, SystemConstant.QUERY_KEY);
        String sessionId = DelegationHelper.getSystemString(request, SystemConstant.SESSION_ID_KEY);
        String processDefineId = context.getProcessDefinition().getId();
        String processDefineVersion = context.getProcessDefinition().getVersion();
        String processDefineName = context.getProcessDefinition().getName();
        String processInstanceId = context.getProcessInstance().getInstanceId();
        Boolean stream = DelegationHelper.getSystem(request, SystemConstant.CHUNK_CONSUMER_KEY) != null;
        String requestId = DelegationHelper.getSystemString(request, SystemConstant.REQUEST_ID_KEY);
        String templateType = getTemplateTypeByActivityType(activityType);
        String parentTemplateType = StringUtils.equalsIgnoreCase("start", templateType) ? "" : "start";

        //兼容重复创建的情况
        boolean repeatCreate = request.get(LOG_NODE_PREFIX + activityId) != null;
        ProcessExecuteLog originLog = null;
        if(repeatCreate) {
            originLog = (ProcessExecuteLog)request.get(LOG_NODE_PREFIX + activityId);
        }


        ProcessExecuteLog log = new ProcessExecuteLog();

        log.setStartTime(System.currentTimeMillis());
        log.setAgentCode(agentCode);
        log.setAgentVersion(agentVersion);
        log.setAgentName(agentName);
        log.setTenantCode(tenantCode);
        log.setComponentAsync(componentAsync);
        log.setQuery(query);
        log.setSessionId(sessionId);
        log.setProcessDefineId(processDefineId);
        log.setProcessDefineVersion(processDefineVersion);
        log.setProcessDefineName(processDefineName);
        log.setProcessInstanceId(processInstanceId);
        log.setActivityId(activityId);
        log.setActivityName(activityName);
        log.setActivityType(activityType);
        log.setStream(stream);
        log.setRequestId(requestId);
        if(!repeatCreate || StringUtils.isEmpty(originLog.getTemplateType())) {
            log.setTemplateType(templateType);
        } else {
            log.setTemplateType(originLog.getTemplateType());
        }
        if(!repeatCreate || StringUtils.isEmpty(originLog.getParentTemplateType())) {
            log.setParentTemplateType(parentTemplateType);
        } else {
            log.setParentTemplateType(originLog.getParentTemplateType());
        }

        //处理一次性变量
        try {
            if(context.getRequest().get("temporaryParentTemplateType") != null) {
                log.setParentTemplateType(MapUtils.getString(context.getRequest(), "temporaryParentTemplateType"));
                context.getRequest().remove("temporaryParentTemplateType");
            }
        } catch (Throwable e) {
            DEFAULT_LOG.error("request or response json parse error", e);
        }

        context.getRequest().put(LOG_NODE_PREFIX + activityId, log);
    }

    public  static void stop(ProcessExecuteLog log) {
        pushLog(log);
    }

    public static void update(String activityId, Map<String, Object> request, Map<String, Object> updateMap) {
        if(request == null) {
            return;
        }
        if(request.get(LOG_NODE_PREFIX + activityId) == null) {
            return;
        }
        ProcessExecuteLog log = (ProcessExecuteLog)request.get(LOG_NODE_PREFIX + activityId);
        if(log == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : updateMap.entrySet()) {
            if(entry.getKey().equals("nodeRequest")) {
                log.setRequest(JSON.toJSONString(entry.getValue()));
            } else if(entry.getKey().equals("modelName")) {
                log.setModelName(entry.getValue() != null ? entry.getValue().toString() : null);
            }
            if(entry.getKey().equals("firstTokenTime")) {
                log.setFirstTokenTime(entry.getValue().toString());
            }
            if(entry.getKey().equals("costToken")) {
                log.setCostToken(entry.getValue().toString());
            }
            if(entry.getKey().equals("firstTokenCost")) {
                log.setFirstTokenCost(entry.getValue().toString());
            }
            if(entry.getKey().equals("templateType")) {
                log.setTemplateType(entry.getValue().toString());
            }
            if(entry.getKey().equals("parentTemplateType")) {
                log.setParentTemplateType(entry.getValue().toString());
            }
            // TODO 可统一更新其他key
        }
    }

    public static void update(ExecutionContext context, Map<String, Object> updateMap) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        update(activityId, context.getRequest(), updateMap);
    }

    public static void stop(ExecutionContext context) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        ProcessExecuteLog log = (ProcessExecuteLog)context.getRequest().get(LOG_NODE_PREFIX + activityId);
        if(log == null) {
            return;
        }
        Long endTime = System.currentTimeMillis();
        log.setEndTime(endTime);
        Long executeTime = endTime - log.getStartTime();
        log.setExecuteTime(executeTime);
        log.setSuccess(true);

        try {
            if(context.getRequest().get("out_" + activityId) != null) {
                log.setResponse(JSON.toJSONString(context.getRequest().get("out_" + activityId)));
            }
        } catch (Throwable e) {
            DEFAULT_LOG.error("request or response json parse error", e);
        }

        //选择器的终止打印在class ：AgentSequenceFlowBehavior中完成
        if(StringUtils.equalsIgnoreCase(log.getTemplateType(), TemplateTypeEnums.exclusiveGateway.getCode())) {
            return;
        }
        pushLog(log);
    }

    public static void stop(ExecutionContext context, AgentMagicException agentMagicException) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        ProcessExecuteLog log = (ProcessExecuteLog)context.getRequest().get(LOG_NODE_PREFIX + activityId);
        if(log == null) {
            return;
        }
        Long endTime = System.currentTimeMillis();
        log.setEndTime(endTime);
        Long executeTime = endTime - log.getStartTime();
        log.setExecuteTime(executeTime);
        log.setSuccess(false);
        log.setErrorCode(agentMagicException.getErrorCode());
        log.setErrorMessage(agentMagicException.getErrorMessage());
        log.setErrorDetail(agentMagicException.getErrorDetail());

        pushLog(log);
    }

    public static void stop(ExecutionContext context, ConditionExpression conditionExpression, Boolean result, Map<String, Object> enirchMap) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        start(context);
        update(context, enirchMap);
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        ProcessExecuteLog log = (ProcessExecuteLog)context.getRequest().get(LOG_NODE_PREFIX + activityId);
        if(log == null) {
            return;
        }
        Long endTime = System.currentTimeMillis();
        log.setEndTime(endTime);
        Long executeTime = endTime - log.getStartTime();
        log.setExecuteTime(executeTime);
        log.setSuccess(true);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("expression", conditionExpression.getExpressionContent());
        log.setRequest(JSON.toJSONString(requestMap));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        log.setResponse(JSON.toJSONString(resultMap));

        pushLog(log);
    }

    private static String getTemplateTypeByActivityType(String activityType) {
        if(StringUtils.isEmpty(activityType)) {
            return "";
        }
        TemplateTypeEnums type = TemplateTypeEnums.instanceByActivityType(activityType);
        if(Objects.isNull(type)) {
            return "";
        }
        return type.getCode();
    }

    private static void pushLog(ProcessExecuteLog log) {
        if(Objects.isNull(log)) {
            return;
        }
        String logStr = log.toString();
        if(StringUtils.equalsIgnoreCase(enableSlsClientLog, Boolean.TRUE.toString()) && logStr.length() > 10000) {
            DEFAULT_LOG.warn("pushLog for long log, requestId: {}", log.getRequestId());
            pushLogBySlsClient(log);
        } else {
            DEFAULT_LOG.warn("pushLog for normal log, requestId: {}", log.getRequestId());
            LOG.warn(logStr);
        }
    }

    private static void pushLogBySlsClient(ProcessExecuteLog log) {
        if(Objects.isNull(log)) {
            return;
        }
        try{
            LoggingService service = (LoggingService) ApplicationContextUtil.getBean(LoggingService.class);
            service.push(Collections.singletonList(log));
        } catch (Throwable e) {
            DEFAULT_LOG.error("pushLogBySlsClient fail", e);
        }
    }

}

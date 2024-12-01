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
package com.alibaba.agentmagic.framework.delegation.cotexecutor.tools;

import com.alibaba.agentmagic.framework.config.ComponentStreamCallback;
import com.alibaba.agentmagic.framework.delegation.cotexecutor.support.CardMessageFormat;
import com.alibaba.agentmagic.framework.delegation.cotexecutor.support.StreamMessageUtils;
import com.alibaba.agentmagic.framework.domain.ProcessExecuteLog;
import com.alibaba.agentmagic.framework.domain.TemplateTypeEnums;
import com.alibaba.agentmagic.framework.domain.ToolExecuteLog;
import com.alibaba.agentmagic.framework.utils.AgentResponseUtils;
import com.alibaba.agentmagic.framework.utils.IdGeneratorUtils;
import com.alibaba.agentmagic.framework.utils.ThreadLocalUtils;
import com.alibaba.agentmagic.framework.utils.ToolCallingLogContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.agent.domain.ComponentCallingInput;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.ToolCallingService;
import com.alibaba.langengine.agentframework.model.service.request.ToolCallingInvokeRequest;
import com.alibaba.langengine.agentframework.model.service.response.ToolCallingInvokeResponse;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.tokenizers.QwenTokenizer;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.agentmagic.framework.delegation.constants.ToolCallingConstant.*;

/**
 * 组件工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class ComponentTool extends StructuredTool {

    private static final QwenTokenizer tokenizer = new QwenTokenizer();

    private ToolCallingService toolCallingService;
    private Consumer<Object> messageConsumer;
    private String sessionId;
    private String requestId;
    private String userId;
    private boolean apikeyCall;
    private String apiKey;
    private String env;
    private Long startTime;
    private FrameworkSystemContext systemContext;

    @Setter
    private Map<String, Object> extraInvokeContext;

    @Setter
    private boolean needNewTraceId = false;

    @Setter
    private TemplateTypeEnums callerType;

    public static ComponentTool build(FrameworkSystemContext systemContext, ToolCallingService toolCallingService,
                                      ComponentCallingInput component, Map<String, Object> extraInvokeContext) {
        ComponentTool tool = new ComponentTool();
        tool.setName(component.getComponentId());
        tool.setDescription(component.getComponentDesc());
        tool.setFunctionName(component.getComponentVersion());
        tool.setToolCallingService(toolCallingService);
        tool.setMessageConsumer(systemContext.getChunkConsumer());
        tool.setRequestId(systemContext.getRequestId());
        tool.setUserId(systemContext.getUserId());
        tool.setApikeyCall(systemContext.getApikeyCall());
        tool.setApiKey(systemContext.getApikey());
        tool.setEnv(systemContext.getEnv());
        tool.setCallbackManager(new CallbackManager());
        tool.getCallbackManager().addHandler(new ComponentStreamCallback());
        tool.setSystemContext(systemContext);
        tool.setExtraInvokeContext(extraInvokeContext);
        return tool;
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        log.info("ComponentTool toolInput:" + toolInput);

        ToolCallingInvokeRequest toolCallingInvokeRequest = new ToolCallingInvokeRequest();
        toolCallingInvokeRequest.setApiKeyCall(apikeyCall);
        toolCallingInvokeRequest.setApiKey(apiKey);
//        if(!StringUtils.isEmpty(apiKey)) {
//            toolCallingInvokeRequest.setApiKey(apiKey);
//        } else {
//            toolCallingInvokeRequest.setToolScene("test");
//        }
        toolCallingInvokeRequest.setToolId(getName());
        toolCallingInvokeRequest.setToolVersion(getFunctionName());
        toolCallingInvokeRequest.setToolParams(toolInput);
        toolCallingInvokeRequest.setEnv(env);
        toolCallingInvokeRequest.setNeedNewTraceId(needNewTraceId);
//        toolCallingInvokeRequest.setChunkConsumer(messageConsumer);
        String toolJson = JSON.toJSONString(toolCallingInvokeRequest);
        log.info("toolCallingInvokeRequest:" + JSON.toJSONString(toolCallingInvokeRequest));

        String firstCost = null;
        try {
            firstCost = ThreadLocalUtils.getFirstCost();
            if(firstCost == null) {
                firstCost = AgentResponseUtils.getTimeCost(startTime);
                ThreadLocalUtils.set(firstCost);
            }
        } catch (Exception ex) {
            log.error("ComponentTool ThreadLocalUtils.getFirstCost error:" + firstCost, ex);
        }

        // function_call请求流式
        String sectionId = IdGeneratorUtils.nextId();
        StreamMessageUtils.sendFunctionCallStreamMessage(systemContext, startTime, firstCost, sectionId, toolJson);

        Long toolInvokeStartTime = System.currentTimeMillis();

        // 附加上下文信息，如用于将应用组件的message输出到structData里
        if (toolCallingInvokeRequest.getInvokeContext() == null) {
            toolCallingInvokeRequest.setInvokeContext(new HashMap<>());
        }
        if (extraInvokeContext != null) {
            toolCallingInvokeRequest.getInvokeContext().putAll(extraInvokeContext);
        }

        // 调用工具
        AgentResult<ToolCallingInvokeResponse> agentResult = toolCallingService.invoke(toolCallingInvokeRequest);

        // 记录组件调用开始日志
        if (TemplateTypeEnums.agent.equals(callerType)) {
            keepExecuteLogRecord(agentResult, System.currentTimeMillis() - toolInvokeStartTime,
                    toolCallingInvokeRequest.getToolScene(), toolCallingInvokeRequest.getToolParams());
        } else {
            // cot的工具调用属于节点的子属性，所以这里要start一下
            this.setStartProcessLogInfo(systemContext);
        }

        // tool_response请求流式
        StreamMessageUtils.sendToolResponseStreamMessage(systemContext, toolInvokeStartTime, firstCost, sectionId, agentResult);

        if(!agentResult.isSuccess()) {
            // TODO
            log.error("ToolCallingDelegation execute error:{}", AgentResult.getAgentResultError(agentResult));
            this.setStopOrExceptionProcessLogInfo(false, agentResult);
            throw new AgentMagicException(agentResult.getErrorCode(), agentResult.getErrorMsg(), agentResult.getErrorDetail(), requestId);
        }

        Map<String, Object> toolApiResult = agentResult.getData() == null ? null : agentResult.getData().getToolApiResult();
        ToolExecuteResult toolExecuteResult = new ToolExecuteResult(toolApiResult == null ? "{}" : JSON.toJSONString(toolApiResult), false);

        // 将组件最终结果输出按卡片输出
        JSONArray cardMessage = CardMessageFormat.format(
                getCardConfig(getName(), getFunctionName(), systemContext),
                toolApiResult);
        if (cardMessage != null) {
            StreamMessageUtils.sendCardStreamMessage(systemContext, cardMessage);
        }

        if(agentResult.getData() == null
                || agentResult.getData().getToolApiResult() == null
                || agentResult.getData().getToolApiResult().isEmpty()) {
            List<BaseTool> nextTools = systemContext.getAllTools().stream()
                    .filter(e -> !e.getName().equals(getName()))
                    .collect(Collectors.toList());
            toolExecuteResult.setNextTools(nextTools);
        } else {
            toolExecuteResult.setNextTools(systemContext.getAllTools());
        }
        this.setStopOrExceptionProcessLogInfo(true, agentResult);
        return toolExecuteResult;
    }

    private void setStartProcessLogInfo(FrameworkSystemContext systemContext) {
        ProcessExecuteLog log = new ProcessExecuteLog();
        String agentCode = systemContext.getAgentCode();
        String requestId = systemContext.getRequestId();
        log.setAgentCode(agentCode);
        log.setRequestId(requestId);
        log.setSuccess(true);
        log.setParentTemplateType("cot");
        log.setStartTime(System.currentTimeMillis());

        log.setTemplateType("component");
        log.setActivityName("组件节点");
        log.setActivityType(TemplateTypeEnums.tool.getActivityType());
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("toolName",getName());
        requestMap.put("toolDesc", getDescription());
        requestMap.put("input", systemContext.getQuery());
        log.setRequest(JSON.toJSONString(requestMap));

        Map<String, ProcessExecuteLog> map = ToolCallingLogContext.getLog();
        if(MapUtils.isEmpty(map)) {
            map = new HashMap<>();
            map.put(getName(), log);
            ToolCallingLogContext.set(map);
        } else {
            map.put(getName(), log);
        }
    }

    private void setStopOrExceptionProcessLogInfo(boolean success, AgentResult<ToolCallingInvokeResponse> agentResult) {

        Map<String, ProcessExecuteLog> map = ToolCallingLogContext.getLog();
        if(MapUtils.isNotEmpty(map) && map.containsKey(getName())) {
            ProcessExecuteLog log = map.get(getName());
            log.setEndTime(System.currentTimeMillis());
            log.setExecuteTime(log.getEndTime() - log.getStartTime());
            log.setSuccess(success);
            Map<String, Object> responseMap = new HashMap<>();
            if(success) {
                if(Objects.isNull(agentResult.getData())) {
                    return;
                }
                responseMap.put("response", agentResult.getData());
                try {
                    String jsonStr = JSON.toJSONString(agentResult.getData());
                    log.setCostToken(String.valueOf(tokenizer.getTokenCount(jsonStr)));
                } catch (Exception exception) {
                    log.setCostToken(String.valueOf(tokenizer.getTokenCount(JSON.toJSONString(agentResult))));
                }

            } else {
                log.setErrorCode(agentResult.getErrorCode());
                log.setErrorDetail(agentResult.getErrorDetail());
            }
            log.setResponse(JSON.toJSONString(responseMap));
            ProcessExecuteLog.stop(log);
        }
    }

    public void keepExecuteLogRecord(AgentResult<ToolCallingInvokeResponse> agentResult,
                                     long cost, String toolScene, String toolParams) {
        try {
            String toolId = getName();
            String toolVersion = getFunctionName();
            ToolExecuteLog toolExecuteLog = new ToolExecuteLog();
            toolExecuteLog.setStartTime(startTime);
            toolExecuteLog.setAgentCode(systemContext.getAgentCode());
            toolExecuteLog.setExecuteTime(cost);
            toolExecuteLog.setToolName(getName());
            toolExecuteLog.setToolDesc(getName() + "-" + getFunctionName());
            toolExecuteLog.setRequestId(requestId);
            toolExecuteLog.setUserId(userId);
            if(!agentResult.isSuccess()) {
                toolExecuteLog.setSuccess(false);
                toolExecuteLog.setCode(agentResult.getErrorCode());
                toolExecuteLog.setMessage(agentResult.getErrorMsg());
                // tool日志打点
                toolExecuteLog.doLog();

                log.error("execute tool error is {}", AgentResult.getAgentResultError(agentResult));
                throw new AgentMagicException(agentResult.getErrorCode(), agentResult.getErrorMsg(), agentResult.getErrorDetail(), requestId);
            }

            // tool日志打点
            toolExecuteLog.doLog();

            // 日志打点，更新工具请求
            Map<String, Object> nodeRequest = new HashMap<>();
            nodeRequest.put(TOOL_SCENE_KEY, toolScene);
            nodeRequest.put(TOOL_ID_KEY, toolId);
            nodeRequest.put(TOOL_VERSION_KEY, toolVersion);
            nodeRequest.put(TOOL_PARAMS_KEY, toolParams);
            ProcessExecuteLog.update(systemContext.getExecutionContext(), new HashMap<String, Object>() {{ put("nodeRequest", nodeRequest); }});
        } catch (Exception exp) {
            log.error("keepLogRecord exception.", exp);
        }
    }

    public void setToolCallingService(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    public void setMessageConsumer(Consumer<Object> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setApikeyCall(boolean apikeyCall) {
        this.apikeyCall = apikeyCall;
    }

    public void setSystemContext(FrameworkSystemContext systemContext) {
        this.systemContext = systemContext;
    }

    private JSONArray getCardConfig(String apiCode, String apiVersion, FrameworkSystemContext systemContext) {
        if (systemContext == null || systemContext.getAgentRelation() == null ||
                systemContext.getAgentRelation().getCotToolsCardConfig() == null) {
            log.info("cotToolsCardConfig is null");
            return null;
        }
        String key = apiCode + "##" + apiVersion;
        String cardConfig = systemContext.getAgentRelation().getCotToolsCardConfig().getString(key);
        if (StringUtils.isEmpty(cardConfig)) {
            log.info("card config is empty key={}, config={} ", key, systemContext.getAgentRelation().getCotToolsCardConfig());
            return null;
        }
        if (cardConfig.startsWith("[")) {
            return JSONArray.parseArray(cardConfig);
        }
        if (cardConfig.startsWith("{")) {
            JSONObject item = JSON.parseObject(cardConfig);
            return new JSONArray().fluentAdd(item);
        }
        log.warn("cardConfig is not json array or json object. cardConfig={}", cardConfig);
        return null;
    }

}

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
package com.alibaba.agentmagic.framework.utils;

import com.alibaba.agentmagic.framework.constants.ProcessConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.domain.NodeRetryResult;
import com.alibaba.langengine.agentframework.model.domain.ProcessSystemContext;
import com.alibaba.langengine.agentframework.model.exception.ProcessException;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.alibaba.agentmagic.framework.utils.FrameworkUtils.getJsonPropertiesFromContext;

/**
 * 流程工具类
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class SeUtils {

    public static CompletableFuture<AgentResult<Map<String, Object>>> getResultFuture(ExecutionContext context) {
        if(context != null && context.getRequest() != null
                && context.getRequest().get(ProcessConstant.RESULT_FUTURE) != null
                && context.getRequest().get(ProcessConstant.RESULT_FUTURE) instanceof CompletableFuture) {
            return (CompletableFuture<AgentResult<Map<String, Object>>>) context.getRequest().get(ProcessConstant.RESULT_FUTURE);
        }
        return null;
    }

    public static ProcessSystemContext getProcessSystemContext(JSONObject requestJson) {
        if(requestJson == null) {
            return null;
        }
        if(requestJson.get(ProcessSystemContext.KEY) == null) {
            return null;
        }
        if(requestJson.get(ProcessSystemContext.KEY) instanceof ProcessSystemContext) {
            return (ProcessSystemContext)requestJson.get(ProcessSystemContext.KEY);
        }else if(requestJson.get(ProcessSystemContext.KEY) instanceof JSONObject) {
            return requestJson.getObject(ProcessSystemContext.KEY,ProcessSystemContext.class);
        }
        return JSONObject.parseObject(JSON.toJSONString(requestJson.get(ProcessSystemContext.KEY)),ProcessSystemContext.class);
    }

    /**
     * 获取外部业务id，方便做全链路日志追踪
     */
    public static String getTraceId(JSONObject requestJson) {
        try {
            String traceId = requestJson.getString(ProcessConstant.REQUEST_TRACE_ID);
            if(StringUtils.isBlank(traceId)) {
                if(requestJson.get(ProcessConstant.SYSTEM) != null) {
                    return requestJson.getJSONObject(ProcessConstant.SYSTEM).getString(ProcessConstant.REQUEST_TRACE_ID);
                }
            }
            return traceId;
        } catch (Throwable e) {
            log.error("unexpected err", e);
            return null;
        }
    }

    public static Map<String, Object> addInstanceIdForResponse(Map<String, Object> processResponse, String processInstanceId) {
        if(processResponse != null && processResponse.get(ProcessConstant.DISPLAY_PROCESS_INSTANCE_ID) == null) {
            processResponse.put(ProcessConstant.DISPLAY_PROCESS_INSTANCE_ID,processInstanceId);
        }
        return processResponse;
    }

    public static void clearCallbackNodeTag(ExecutionContext executionContext) {
        ProcessSystemContext processSystemContext = getProcessSystemContext(new JSONObject(executionContext.getRequest()));
        processSystemContext.clearCallbackNode();
        processSystemContext.clearRetryTimes();
        executionContext.getRequest().put(ProcessSystemContext.KEY,processSystemContext);
    }


    public static NodeRetryResult isNeedRetry(ExecutionContext executionContext, ProcessException exception) {
        if(exception != null && StringUtils.isNotBlank(exception.getErrorCode())) {
            if(StringUtils.startsWithIgnoreCase(exception.getErrorCode(), ProcessConstant.ISV_ERROR_CODE_PREFIX)) {
                return NodeRetryResult.returnNoNeedRetry();
            }
        }
        return isNeedRetry(executionContext);
    }

    /**
     * 判断是否可以重试
     *
     * @return
     */
    public static NodeRetryResult isNeedRetry(ExecutionContext executionContext) {
        // 获取节点ID
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        // 解析节点属性
        JSONObject propertiesJson = getJsonPropertiesFromContext(executionContext, activityId);
        // 最大重试次数,和初始重试间隔
        Integer retry = propertiesJson.getIntValue(ProcessConstant.CONNECTOR_RETRY);
        Long retryInterval = propertiesJson.getLongValue(ProcessConstant.CONNECTOR_RETRY_INTERVAL);
        if(retry <= 0) {
            return NodeRetryResult.returnNoNeedRetry();
        }
        // 解析节点参数
        JSONObject requestJson = new JSONObject(executionContext.getRequest());
        // 获取流程变量
        ProcessSystemContext processSystemContext = getProcessSystemContext(requestJson);
        if(processSystemContext == null) {
            return NodeRetryResult.returnNoNeedRetry();
        }
        if(retry <= processSystemContext.getNodeRetryTimes(activityId)) {
            return NodeRetryResult.returnNoNeedRetry(processSystemContext.getNodeRetryTimes(activityId), retry);
        }
        // 重试，需要清除Callback标，并增加重试次数
        processSystemContext.clearCallbackNode(activityId);
        processSystemContext.addRetryTimes(activityId);
        executionContext.getRequest().put(ProcessSystemContext.KEY,processSystemContext);

        Integer retryCount = processSystemContext.getNodeRetryTimes(activityId);
        Long timeInterval = 0L;
        if(retryCount <= 3 && retryCount >= 0) {
            timeInterval = retryInterval * retryCount;
        }else {
            timeInterval = retryInterval * 3;
        }
        return NodeRetryResult.returnNeedRetry(retryCount,retry,timeInterval);
    }

    public static boolean isLastRetry(ExecutionContext executionContext) {
        if(executionContext.getRequest() != null && executionContext.getRequest().get(ProcessConstant.SYSTEM) != null) {
            JSONObject requestJson = new JSONObject(executionContext.getRequest());
            JSONObject system = requestJson.getJSONObject(ProcessConstant.SYSTEM);
            return system.getBooleanValue(ProcessConstant.SYS_LAST_RETRY);
        }
        return true;
    }

    public static void setLastRetry(ExecutionContext executionContext,Boolean isLastRetry) {
        if(executionContext.getRequest() != null) {
            JSONObject requestJson = new JSONObject(executionContext.getRequest());
            if(executionContext.getRequest().get(ProcessConstant.SYSTEM) != null) {
                JSONObject system = requestJson.getJSONObject(ProcessConstant.SYSTEM);
                system.put(ProcessConstant.SYS_LAST_RETRY,isLastRetry);
            }
        }
    }

    public static BigDecimal getNumber(Object o) {
        if(o == null) {
            return null;
        }
        if(o instanceof BigDecimal) {
            return (BigDecimal) o;
        }
        String str = String.valueOf(o);
        if(NumberUtils.isParsable(str)) {
            return new BigDecimal(str);
        }
        return null;
    }

}

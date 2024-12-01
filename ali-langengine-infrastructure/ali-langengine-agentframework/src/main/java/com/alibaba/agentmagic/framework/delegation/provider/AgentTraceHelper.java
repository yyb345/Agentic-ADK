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
package com.alibaba.agentmagic.framework.delegation.provider;

import com.alibaba.agentmagic.framework.utils.FrameworkUtils;
import com.alibaba.agentmagic.framework.utils.TraceDebugLogUtils;
import com.alibaba.langengine.agentframework.model.domain.TraceOutputDO;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.ConditionExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.*;
import static com.alibaba.langengine.agentframework.model.constant.ProcessConstants.ACTIVITY_VARIABLE_PREFIX;

@Slf4j
public class AgentTraceHelper {

    private static final String TRACE_PROCESS_PREFIX = "trace_process_";
    private static final String TRACE_NODE_PREFIX = "trace_node_";

    public static void traceProcessStart(ExecutionContext context) {
        Boolean traceOutput = DelegationHelper.getSystemBoolean(context.getRequest(), TRACE_OUTPUT_KEY);
        if(traceOutput != null && traceOutput) {
            TraceOutputDO traceOutputDO;
            if(context.getResponse().get(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId()) != null) {
                traceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId());
            } else {
                traceOutputDO = new TraceOutputDO();
            }
            traceOutputDO.setStartTime(System.currentTimeMillis());
            context.getResponse().put(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId(), traceOutputDO);
        }
    }

    public static void traceProcessEnd(ExecutionContext context) {
        Boolean traceOutput = DelegationHelper.getSystemBoolean(context.getRequest(), TRACE_OUTPUT_KEY);
        if(traceOutput != null && traceOutput) {
            for (Map.Entry<String, Object> responseEntry : context.getResponse().entrySet()) {
                if(responseEntry.getKey().startsWith(TRACE_NODE_PREFIX)) {
                    context.getResponse().put(responseEntry.getKey(), responseEntry.getValue());
                }
            }

            TraceOutputDO traceOutputDO;
            if(context.getResponse().get(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId()) != null) {
                traceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId());
            } else {
                traceOutputDO = new TraceOutputDO();
            }
            traceOutputDO.setCostTime(System.currentTimeMillis() - traceOutputDO.getStartTime());
            context.getResponse().put(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId(), traceOutputDO);
        }
    }

    public static void traceNodeStart(ExecutionContext context) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        traceNodeStart(context, activityId, null);
    }

    public static void traceNodeStart(ExecutionContext context, Map<String, Object> nodeRequest) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        traceNodeStart(context, activityId, nodeRequest);
    }

    public static void traceNodeStart(ExecutionContext context, String defaultActivityName) {
        traceNodeStart(context, defaultActivityName, null);
    }

    public static void traceNodeStart(ExecutionContext context, String defaultActivityName, Map<String, Object> nodeRequest) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        String activityName = FrameworkUtils.getActivityName(context, activityId);
        if (StringUtils.isEmpty(activityName)) {
            activityName = defaultActivityName;
        }
        Boolean traceOutput = DelegationHelper.getSystemBoolean(context.getRequest(), TRACE_OUTPUT_KEY);
        if(traceOutput != null && traceOutput) {
            TraceOutputDO traceOutputDO;
            if(context.getResponse().get(TRACE_NODE_PREFIX + activityId) != null) {
                traceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_NODE_PREFIX + activityId);
            } else {
                traceOutputDO = new TraceOutputDO();
            }
            traceOutputDO.setStartTime(System.currentTimeMillis());
            traceOutputDO.setActivityName(activityName);
            if(nodeRequest != null && nodeRequest.size() > 0) {
                traceOutputDO.setRequest(nodeRequest);
            }
            context.getResponse().put(TRACE_NODE_PREFIX + activityId, traceOutputDO);
        }
    }

    public static void traceNodeEnd(ExecutionContext context) {
        if(context.getExecutionInstance() == null) {
            return;
        }
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        Boolean traceOutput = DelegationHelper.getSystemBoolean(context.getRequest(), TRACE_OUTPUT_KEY);
        if(traceOutput != null && traceOutput) {
            TraceOutputDO traceOutputDO;
            if(context.getResponse().get(TRACE_NODE_PREFIX + activityId) != null) {
                traceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_NODE_PREFIX + activityId);
            } else {
                traceOutputDO = new TraceOutputDO();
            }
            traceOutputDO.setCostTime(System.currentTimeMillis() - traceOutputDO.getStartTime());
            Object req = context.getRequest().get(ACTIVITY_VARIABLE_PREFIX + activityId);
            if (req != null) {
                if (req instanceof Map) {
                    Map<String, Object> reqMap = (Map<String, Object>) req;
                    Map<String, Object> newReqMap = new HashMap<>(reqMap);
                    traceOutputDO.setResponse(newReqMap);
                } else {
                    traceOutputDO.setResponse(req);
                }
            }

            TraceDebugLogUtils.extractAndAssignDebugLog(context, traceOutputDO);
            context.getResponse().put(TRACE_NODE_PREFIX + activityId, traceOutputDO);
        }
    }

    public static void traceTransition(ExecutionContext context, ConditionExpression conditionExpression, Boolean result) {
        Boolean traceOutput = DelegationHelper.getSystemBoolean(context.getRequest(), TRACE_OUTPUT_KEY);
        if (traceOutput != null && traceOutput) {
            if(context.getExecutionInstance() == null) {
                return;
            }
            String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
            TraceOutputDO traceOutputDO;
            if (context.getResponse().get(TRACE_NODE_PREFIX + activityId) != null) {
                traceOutputDO = (TraceOutputDO) context.getResponse().get(TRACE_NODE_PREFIX + activityId);
            } else {
                traceOutputDO = new TraceOutputDO();
            }
            traceOutputDO.setStartTime(System.currentTimeMillis());
            traceOutputDO.setCostTime(System.currentTimeMillis() - traceOutputDO.getStartTime());

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("expression", conditionExpression.getExpressionContent());
            traceOutputDO.setRequest(requestMap);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", result);
            traceOutputDO.setResponse(resultMap);

            context.getResponse().put(TRACE_NODE_PREFIX + activityId, traceOutputDO);
        }
    }

    public static boolean traceNodeException(ExecutionContext context, AgentMagicException agentMagicException) {
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        String requestId = DelegationHelper.getSystemString(context.getRequest(), REQUEST_ID_KEY);
        boolean traceOutput = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), TRACE_OUTPUT_KEY, false);
        if(traceOutput) {
            TraceOutputDO traceOutputDO;
            if(context.getResponse().get(TRACE_NODE_PREFIX + activityId) != null) {
                traceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_NODE_PREFIX + activityId);
            } else {
                traceOutputDO = new TraceOutputDO();
            }
            Map<String, Object> error = new HashMap<>();
            error.put("errorCode", agentMagicException.getErrorCode());
            error.put("errorMessage", agentMagicException.getErrorMessage());
            error.put("errorDetail", agentMagicException.getErrorDetail());
            error.put("requestId", requestId);
            traceOutputDO.setResponse(error);
            if(traceOutputDO.getStartTime() == null) {
                traceOutputDO.setStartTime(System.currentTimeMillis());
            }
            traceOutputDO.setCostTime(System.currentTimeMillis() - traceOutputDO.getStartTime());
            traceOutputDO.setStatus(0);
            context.getResponse().put(TRACE_NODE_PREFIX + activityId, traceOutputDO);

            TraceOutputDO processTraceOutputDO;
            if(context.getResponse().get(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId()) != null) {
                processTraceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId());
            } else {
                processTraceOutputDO = new TraceOutputDO();
            }
            processTraceOutputDO.setCostTime(System.currentTimeMillis() - traceOutputDO.getStartTime());
            processTraceOutputDO.setStatus(0);
            context.getResponse().put(TRACE_PROCESS_PREFIX + context.getProcessInstance().getInstanceId(), processTraceOutputDO);

            // 中断流程
            context.setNeedPause(true);
            return true;
        }
        return false;
    }

    public static TraceOutputDO getTraceOutputByNode(ExecutionContext context, String activityId) {
        TraceOutputDO traceOutputDO;
        if(context.getResponse().get(TRACE_NODE_PREFIX + activityId) != null) {
            traceOutputDO = (TraceOutputDO)context.getResponse().get(TRACE_NODE_PREFIX + activityId);
        } else {
            traceOutputDO = new TraceOutputDO();
        }
        if(traceOutputDO.getRequest() == null) {
            traceOutputDO.setRequest(new HashMap<>());
        }
        return traceOutputDO;
    }

    public static void setTraceOutputResponseByNode(ExecutionContext context, String activityId, TraceOutputDO traceOutputDO) {
        context.getResponse().put(TRACE_NODE_PREFIX + activityId, traceOutputDO);
    }

//    public static void setTraceOutputResponseByNode(ExecutionContext executionContext, Map<String, Object> traceInfoMap) {
//        if (executionContext == null || traceInfoMap == null || traceInfoMap.isEmpty()) {
//            return;
//        }
//        Boolean traceOutput = DelegationHelper.getSystemBoolean(executionContext.getRequest(), TRACE_OUTPUT_KEY);
//        if(traceOutput == null || !traceOutput) {
//            return;
//        }
//        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
//        TraceOutputDO traceOutputDO = AgentTraceHelper.getTraceOutputByNode(executionContext, activityId);
//        // 使用本方法设置的时候要注意
//        // AgentTraceHelper.traceNodeEnd 在节点（delegation）executeInternal返回的的map不为null的时候，会把节点返回的map覆盖这里的response，导致这里的response失效，
//        // 如果节点本身是带输出的，建议直接把trace的内容直接丢到节点输出的map里，不要单独设置到这个response里。
//        // 如果节点本身是不输出的，executeInternal一定要返回null，这里的trace才会生效
//        traceOutputDO.setResponse(traceInfoMap);
//        log.info("setTraceOutputResponseByNode activityId={}, traceOutputDO={}", activityId, JSONObject.toJSONString(traceOutputDO));
//        executionContext.getResponse().put(TRACE_NODE_PREFIX + activityId, traceOutputDO);
//    }
}

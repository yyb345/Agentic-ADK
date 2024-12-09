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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.langengine.agentframework.model.domain.TraceOutputDO;
import com.alibaba.langengine.agentframework.model.service.response.DynamicScriptInvokeResponse;
import com.alibaba.smart.framework.engine.context.ExecutionContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author aihe.ah aihe.ah@alibaba-inc.com
 * @date : 2024/10/31
 * 使用场景：
 * 功能描述：
 */
@Slf4j
public class TraceDebugLogUtils {

    /**
     * 拼接调试日志
     */
    public static String generateDebugLogKey(String activityId) {
        return "debug_log_" + activityId;
    }

    public static void storeDebugLog(ExecutionContext executionContext,
        DynamicScriptInvokeResponse dynamicScriptInvokeResponse) {

        if (dynamicScriptInvokeResponse == null || executionContext == null) {
            log.warn("Invalid input: dynamicScriptInvokeResponse or executionContext is null");
            return;
        }
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        String debugLog = dynamicScriptInvokeResponse.getDebugLog();
        if (StringUtils.isNotEmpty(debugLog)) {
            try {
                executionContext.getRequest().put(TraceDebugLogUtils.generateDebugLogKey(activityId), debugLog);
            } catch (Exception e) {
                log.error("Failed to handle debug log", e);
            }
        }
    }

    public static void extractAndAssignDebugLog(ExecutionContext context, TraceOutputDO traceOutputDO) {

        if (context == null || traceOutputDO == null) {
            log.warn("Invalid input: context, traceOutputDO, or activityId is null or empty");
            return;
        }

        try {
            String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
            Object o = context.getRequest().get(TraceDebugLogUtils.generateDebugLogKey(activityId));
            if (o != null) {
                traceOutputDO.setDebugLog(String.valueOf(o));
            }
        } catch (Exception e) {
            log.error("Failed to retrieve and set debug log", e);
        }
    }
}

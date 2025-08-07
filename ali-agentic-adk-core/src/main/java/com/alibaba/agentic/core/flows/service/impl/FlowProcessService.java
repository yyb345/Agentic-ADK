/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.flows.service.impl;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.model.instance.ExecutionInstance;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.persister.custom.session.PersisterSession;
import com.alibaba.smart.framework.engine.service.command.ExecutionCommandService;
import com.alibaba.smart.framework.engine.service.query.ExecutionQueryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
public class FlowProcessService {

    private final SmartEngine smartEngine;


    public ProcessInstance startFlow(FlowDefinition flowDefinition, Map<String, Object> request, Map<String, Object> response) {
        try {
            PersisterSession.create();
            return smartEngine.getProcessCommandService().start(flowDefinition.getDefinitionId(), flowDefinition.getVersion(),
                    new HashMap<>(Map.of(ExecutionConstant.ORIGIN_REQUEST, request.get(ExecutionConstant.ORIGIN_REQUEST),
                            ExecutionConstant.SYSTEM_CONTEXT, request.get(ExecutionConstant.SYSTEM_CONTEXT))),
                    response);
        } catch (Exception e) {
            log.error("FlowProcessService startFlow fail", e);
            throw new RuntimeException(e);
        } finally {
            PersisterSession.destroySession();
        }
    }

    public ProcessInstance startFlow(String flowDefinitionId, String flowVersion, Map<String, Object> request, Map<String, Object> response) {
        try {
            PersisterSession.create();
            return smartEngine.getProcessCommandService().start(flowDefinitionId, flowVersion
                    , Map.of(ExecutionConstant.ORIGIN_REQUEST, request.get(ExecutionConstant.ORIGIN_REQUEST),
                            ExecutionConstant.SYSTEM_CONTEXT, request.get(ExecutionConstant.SYSTEM_CONTEXT)), response);
        } catch (Exception e) {
            log.error("FlowProcessService startFlow fail", e);
            throw new RuntimeException(e);
        } finally {
            PersisterSession.destroySession();
        }
    }


    public void signal(ProcessInstance processInstance, String activityId, Map<String, Object> request, Map<String, Object> response) {
        try {
            PersisterSession.create();
            PersisterSession.currentSession().putProcessInstance(processInstance);
            ExecutionQueryService executionQueryService = smartEngine.getExecutionQueryService();
            ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
            List<ExecutionInstance> executionInstanceList = executionQueryService.findActiveExecutionList(processInstance.getInstanceId());
            if (CollectionUtils.isEmpty(executionInstanceList)) {
                log.error("no active executionInstance error");
                return;
            }
            for (ExecutionInstance executionInstance : executionInstanceList) {
                if (executionInstance.getProcessDefinitionActivityId().equals(activityId)) {
                    executionCommandService.signal(executionInstance.getInstanceId(),
                            new HashMap<>(Map.of(ExecutionConstant.ORIGIN_REQUEST, request.get(ExecutionConstant.ORIGIN_REQUEST),
                                    ExecutionConstant.SYSTEM_CONTEXT, request.get(ExecutionConstant.SYSTEM_CONTEXT),
                                    ExecutionConstant.IS_CALLBACK, true)),
                            new HashMap<>(response));
                    return;
                }
            }
        } catch (Exception e) {
            log.error("signal error", e);
            throw new RuntimeException(e);
        } finally {
            PersisterSession.destroySession();
        }
    }


}

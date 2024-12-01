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
package com.alibaba.agentmagic.framework.process;

import com.alibaba.agentmagic.framework.utils.FrameworkUtils;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinitionSource;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.persister.custom.session.PersisterSession;
import com.alibaba.smart.framework.engine.service.command.ProcessCommandService;
import com.alibaba.smart.framework.engine.service.command.RepositoryCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AgentProcessService {

    @Resource
    private SmartEngine smartEngine;

    public AgentResult<Map<String,Object>> startProcessInstanceByBpmnXml(String processDefinitionContent, Map<String, Object> context) {
        String processDefinitionId = null;
        String version = null;
        try {
            PersisterSession.create();
            ProcessDefinitionSource processDefinitionSource = deployProcessDefinitionByBpmnXml(processDefinitionContent);

            ProcessCommandService processCommandService = smartEngine.getProcessCommandService();
            processDefinitionId = processDefinitionSource.getFirstProcessDefinition().getId();
            version = processDefinitionSource.getFirstProcessDefinition().getVersion();
            Map<String, Object> request = FrameworkUtils.buildRequest(context);
            Map<String, Object> response = new HashMap<>();
            ProcessInstance processInstance = processCommandService.start(processDefinitionId, version, request, response);
            response.put("processDefinitionId", processDefinitionId);
            response.put("processDefinitionVersion", version);
            return AgentResult.success(processInstance.getInstanceId(), response);
        } catch (AgentMagicException e) {
            return AgentResult.fail(e, null);
        } catch (Throwable e) {
            String msg = String.format("startProcessInstanceByBpmnXml-exception: processDefinitionId=%s,version=%s",
                    processDefinitionId,
                    version);
            log.error(msg, e);
            return AgentResult.fail(AgentMagicErrorCode.PROCESS_START_ERROR, e, null);
        } finally {
            PersisterSession.destroySession();
        }
    }

    public AgentResult<Map<String,Object>> startProcessInstanceByBpmnXml(String processDefinitionContent, Map<String, Object> context, Map<String, Object> response) {
        String processDefinitionId = null;
        String version = null;
        String processInstanceId = null;
        try {
            PersisterSession.create();
            ProcessDefinitionSource processDefinitionSource = deployProcessDefinitionByBpmnXml(processDefinitionContent);

            ProcessCommandService processCommandService = smartEngine.getProcessCommandService();
            processDefinitionId = processDefinitionSource.getFirstProcessDefinition().getId();
            version = processDefinitionSource.getFirstProcessDefinition().getVersion();
            Map<String, Object> request = FrameworkUtils.buildRequest(context);
            ProcessInstance processInstance = processCommandService.start(processDefinitionId, version, request, response);
            processInstanceId = processInstance.getInstanceId();
            return AgentResult.success(processInstanceId, response);
        } catch (AgentMagicException e) {
            return AgentResult.fail(e, null, processInstanceId);
        } catch (Throwable e) {
            String msg = String.format("startProcessInstanceByBpmnXml-exception: processDefinitionId=%s,version=%s",
                    processDefinitionId,
                    version);
            log.error(msg, e);
            return AgentResult.fail(AgentMagicErrorCode.PROCESS_START_ERROR, e, null, processInstanceId);
        } finally {
            PersisterSession.destroySession();
        }
    }

    public ProcessDefinitionSource deployProcessDefinitionByBpmnXml(String processDefinitionContent) {
        try {
            RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
            return repositoryCommandService.deployWithUTF8Content(processDefinitionContent);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }
}

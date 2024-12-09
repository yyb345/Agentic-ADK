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
package com.alibaba.langengine.agentframework.behavior;

import com.alibaba.langengine.agentframework.model.domain.AgentProcessInstance;
import com.alibaba.smart.framework.engine.common.util.DateUtil;
import com.alibaba.smart.framework.engine.common.util.IdAndVersionUtil;
import com.alibaba.smart.framework.engine.configuration.IdGenerator;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.constant.RequestMapSpecialKeyConstant;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.instance.factory.ProcessInstanceFactory;
import com.alibaba.smart.framework.engine.model.instance.InstanceStatus;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.util.ObjUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Agent流程实例工厂重写
 * 【注意】不要动目录
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@ExtensionBinding(group = ExtensionConstant.COMMON, bindKey = ProcessInstanceFactory.class, priority = 1)
public class AgentProcessInstanceFactory implements ProcessInstanceFactory {

    @Override
    public ProcessInstance create( ProcessEngineConfiguration processEngineConfiguration,String processDefinitionId, String processDefinitionVersion, Map<String, Object> request) {
        log.info("AgentProcessInstanceFactory create processDefinitionId:" + processDefinitionId + ", version:" + processDefinitionVersion);
        AgentProcessInstance defaultProcessInstance = new AgentProcessInstance();
        IdGenerator idGenerator = processEngineConfiguration.getIdGenerator();

        defaultProcessInstance.setInstanceId(idGenerator.getId());
        defaultProcessInstance.setStatus(InstanceStatus.running);
        defaultProcessInstance.setStartTime(DateUtil.getCurrentDate());

        defaultProcessInstance.setProcessDefinitionIdAndVersion(IdAndVersionUtil.buildProcessDefinitionKey(processDefinitionId,processDefinitionVersion));
        defaultProcessInstance.setProcessDefinitionId(processDefinitionId);
        defaultProcessInstance.setProcessDefinitionVersion(processDefinitionVersion);

        if (null != request) {
            String startUserId = ObjUtil.obj2Str(request.get(RequestMapSpecialKeyConstant.PROCESS_INSTANCE_START_USER_ID));
            defaultProcessInstance.setStartUserId(startUserId);

            String processDefinitionType = ObjUtil.obj2Str(request.get(RequestMapSpecialKeyConstant.PROCESS_DEFINITION_TYPE));
            defaultProcessInstance.setProcessDefinitionType(processDefinitionType);

            String bizUniqueId = ObjUtil.obj2Str(request.get(RequestMapSpecialKeyConstant.PROCESS_BIZ_UNIQUE_ID));
            defaultProcessInstance.setBizUniqueId(bizUniqueId);

            String title = ObjUtil.obj2Str(request.get(RequestMapSpecialKeyConstant.PROCESS_TITLE));
            defaultProcessInstance.setTitle(title);

            String comment = ObjUtil.obj2Str(request.get(RequestMapSpecialKeyConstant.PROCESS_INSTANCE_COMMENT));
            defaultProcessInstance.setComment(comment);
        }

        return defaultProcessInstance;
    }

    @Override
    public ProcessInstance createChild(ProcessEngineConfiguration processEngineConfiguration,
                                       String processDefinitionId, String processDefinitionVersion,
                                       Map<String, Object> request, String parentInstanceId,
                                       String parentExecutionInstanceId) {
        ProcessInstance childProcessInstance = this.create(processEngineConfiguration,   processDefinitionId,processDefinitionVersion,
            request);
        childProcessInstance.setParentInstanceId(parentInstanceId);
        childProcessInstance.setParentExecutionInstanceId(parentExecutionInstanceId);

        return childProcessInstance;
    }

}

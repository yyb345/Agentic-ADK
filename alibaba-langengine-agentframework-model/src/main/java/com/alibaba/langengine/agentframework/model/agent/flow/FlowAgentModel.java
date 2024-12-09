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
package com.alibaba.langengine.agentframework.model.agent.flow;

import com.alibaba.langengine.agentframework.model.agent.AgentModel;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.agent.domain.InstanceFlow;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flow Agent Model
 *
 * @author xiaoxuan.lp
 */
@Data
public class FlowAgentModel extends AgentModel {

    /**
     * 流程定义id，必填
     */
    private String processDefinitionId;

    /**
     * 流程定义version，必填
     */
    private String processDefinitionVersion;

    /**
     * 测试使用
     */
    private String flowSchema;

    /**
     * 测试使用，工作流UI dsl(如果flowSchema为空，使用flowDsl)
     */
    private String flowDsl;

    /**
     * 工作流组件节点是否异步化
     */
    private boolean componentAsync = false;

    /**
     * 是否追踪输出
     */
    private boolean traceOutput = false;

    /**
     * 对应的场景子流程列表
     */
    private List<InstanceFlow> sceneInstanceFlows;

    /**
     * Agent关联主体
     */
    private AgentRelation relation;

    /**
     * 上下文
     */
    private Map<String, Object> context = new ConcurrentHashMap<>();
}

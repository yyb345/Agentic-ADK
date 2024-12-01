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
package com.alibaba.langengine.agentframework.model.domain;

import com.alibaba.langengine.agentframework.model.dataobject.AgentTaskInstanceDO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 流程实例恢复请求体
 *
 * @author xiaoxuan.lp
 */
@Data
public class ProcessInstanceSignalRequest {

    /**
     * 流程实例DO
     */
    private AgentTaskInstanceDO agentTaskInstance;

    /**
     * agentCode
     */
    private String agentCode;

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 节点id
     */
    private String activityId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 节点上下文
     */
    private Map<String, Object> context;

    /**
     * async=true使用
     */
    private Map<String, Object> invokeContext;

    // ------ 以下字段为并行节点使用 start ------ //

    /**
     * 如果不为空，则恢复玩直接跳转到该节点
     */
    private String jumpActivityId;

    /**
     * 并行多个节点上下文
     */
    private Map<String, Object> parallelContext;

    /**
     * 并行处理的processInstance主键IDs
     */
    private List<Long> parallelProcessInstMainIdList;

    // ------ 字段为并行节点使用 end ------ //
}

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
package com.alibaba.agentmagic.framework.manager;

import com.alibaba.langengine.agentframework.model.dataobject.AgentTaskInstanceDO;

import java.util.List;

public interface AgentTaskInstanceManager {

    /**
     * 获取待处理的任务
     *
     * @param agentCode
     * @param taskId
     * @return
     */
    AgentTaskInstanceDO getTodoTaskInstanceByTaskId(String agentCode, String processInstanceId, String taskId);

    AgentTaskInstanceDO getTodoAndFailedTaskInstanceByTaskId(String agentCode, String processInstanceId, String taskId);

    List<AgentTaskInstanceDO> getFinishedByProcessInstanceId(String agentCode, String processInstanceId);

    List<AgentTaskInstanceDO> getRunningByParallelAll(String agentCode);

    List<AgentTaskInstanceDO> getParallelRunningByProcessInstanceId(String agentCode, String processInstanceId);

    int addTaskInstance(AgentTaskInstanceDO agentTaskInstanceDO);

    int updateTaskInstance(AgentTaskInstanceDO agentTaskInstanceDO);

    int updateStatusFinished(String agentCode, String processInstanceId, List<Long> ids);

    int updateStatusLocked(String agentCode, String processInstanceId, List<Long> ids);

//    int updateStatusFailed(String agentCode, String processInstanceId, List<Long> ids);
}

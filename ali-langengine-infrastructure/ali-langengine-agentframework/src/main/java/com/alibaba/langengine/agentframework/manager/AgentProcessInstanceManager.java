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
package com.alibaba.langengine.agentframework.manager;

import com.alibaba.langengine.agentframework.model.dataobject.AgentProcessInstanceDO;
import com.alibaba.smart.framework.engine.context.ExecutionContext;

import java.util.Date;
import java.util.List;

public interface AgentProcessInstanceManager {


    int addProcessInstance(AgentProcessInstanceDO instanceDO);

    int addOrReplace(AgentProcessInstanceDO instanceDO);

    /**
     * 尝试insert，如果processInstanceId冲突，则更新数据
     * @param instanceDO
     * @return
     */
    int addOrUpdate(AgentProcessInstanceDO instanceDO);

    int updateProcessInstance(AgentProcessInstanceDO instanceDO);

    /**
     * 更新时校验工作流实例状态必须为暂停或异常
     * @param instanceDO
     * @return
     */
    int updateProcessInstanceForSignal(AgentProcessInstanceDO instanceDO);

    int updateByInstanceId(AgentProcessInstanceDO instanceDO);

    int updateStatusByInstanceId(AgentProcessInstanceDO instanceDO);

    int updateStatusByInstanceIdAndActivityId(AgentProcessInstanceDO instanceDO);

    int updateStatusFinished(List<Long> ids);

    int updateStatusLocked(List<Long> ids);

    int updateStatusFailed(List<Long> ids);

    /**
     * 
     * @param instanceDO
     * @return
     */
    int updateRetryTimesByInstanceId(AgentProcessInstanceDO instanceDO);

//    AgentProcessInstanceDO getByProcessInstanceId(String instanceId);

//    AgentProcessInstanceDO getByProcessInstanceIdAndActivityId(String instanceId, String activityId);

    AgentProcessInstanceDO getByTaskId(String taskId);

    /**
     * 根据实例id查询
     * @param processInstanceIdList
     * @param status
     * @return
     */
    List<AgentProcessInstanceDO> getByInstanceIdList(List<String> processInstanceIdList,Integer status);

    /**
     * 获取需要重试的流程实例
     * @param dateTime
     * @param retryTimes
     * @return
     */
    List<String> getInstanceIdForRetry(Date dateTime, Integer retryTimes);


    AgentProcessInstanceDO saveProcessInstanceForPause(ExecutionContext executionContext);


//    List<AgentProcessInstanceDO> getInstanceList(ProcessInstanceQuery query);
//
//    Long getInstanceCount(ProcessInstanceQuery query);
}

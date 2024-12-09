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

import com.alibaba.smart.framework.engine.instance.impl.AbstractLifeCycleInstance;
import com.alibaba.smart.framework.engine.model.instance.ActivityInstance;
import com.alibaba.smart.framework.engine.model.instance.InstanceStatus;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Agent流程实例对象重写
 *
 * @author xiaoxuan.lp
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AgentProcessInstance extends AbstractLifeCycleInstance implements ProcessInstance{

    private String processDefinitionIdAndVersion;
    private String processDefinitionId;
    private String processDefinitionVersion;
    private String processDefinitionType;



    private  String startUserId;
    /**
     * 业务唯一标识
     */
    private String bizUniqueId;

    private String parentInstanceId;
    private String parentExecutionInstanceId;

    private boolean suspend;

    private String reason;

    private String tag;

    private String title;

    /**
     * 备注
     */
    private String comment;


    @Setter
    private InstanceStatus status = InstanceStatus.running;

    @Override
    public boolean isSuspend() {
        return InstanceStatus.suspended == this.status;
    }

    // 防止多线程并发读写，出现ConcurrentModificationException异常
    private List<ActivityInstance> activityInstances = new CopyOnWriteArrayList<ActivityInstance>();


    @Override
    public void addActivityInstance(ActivityInstance activityInstance) {
        this.activityInstances.add(activityInstance);
    }

    @Override
    public List<ActivityInstance> getActivityInstances(){
        return activityInstances;
    }

}

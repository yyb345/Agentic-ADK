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
package com.alibaba.langengine.agentframework.model.dataobject;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 *
 * @author xiaoxuan.lp
 */
public class AgentProcessInstanceDO {
    /**
     * Database Column Remarks:
     *   主键
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Long id;

    /**
     * Database Column Remarks:
     *   创建时间
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Date gmtCreate;

    /**
     * Database Column Remarks:
     *   修改时间
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Date gmtModified;

    /**
     * Database Column Remarks:
     *   流程定义id
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String processDefinitionId;

    /**
     * Database Column Remarks:
     *   流程定义版本号
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer processDefinitionVersion;

    /**
     * Database Column Remarks:
     *   流程实例id
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String processInstanceId;

    /**
     * Database Column Remarks:
     *   流程实例名称
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String processInstanceName;

    /**
     * Database Column Remarks:
     *   当前节点id
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String activityId;

    /**
     * Database Column Remarks:
     *   流程状态，0:运行 1:暂停 2:异常 3:完成
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer status;

    /**
     * Database Column Remarks:
     *   当前重试次数
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer retryTimes;

    /**
     * Database Column Remarks:
     *   是否需要重试
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer needRetry;

    /**
     * Database Column Remarks:
     *   拥有者
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String ownerId;

    /**
     * Database Column Remarks:
     *   异步任务ID
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String taskId;

    /**
     * Database Column Remarks:
     *   应用code
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String agentCode;

    /**
     * Database Column Remarks:
     *   对应前置并行开始节点id
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String parallelStartActivityId;

    /**
     * Database Column Remarks:
     *   是否包含并行节点
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer hasParallel;

    /**
     * Database Column Remarks:
     *   流程实例Map序列化
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String serializedProcessInstance;

    /**
     * Database Column Remarks:
     *   流程异常信息
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String errorMessage;

    /**
     * Database Column Remarks:
     *   流程变量
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String context;

    /**
     * Database Column Remarks:
     *   节点返回结果
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String result;

    /**
     * @return
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModified=").append(gmtModified);
        sb.append(", processDefinitionId=").append(processDefinitionId);
        sb.append(", processDefinitionVersion=").append(processDefinitionVersion);
        sb.append(", processInstanceId=").append(processInstanceId);
        sb.append(", processInstanceName=").append(processInstanceName);
        sb.append(", activityId=").append(activityId);
        sb.append(", status=").append(status);
        sb.append(", retryTimes=").append(retryTimes);
        sb.append(", needRetry=").append(needRetry);
        sb.append(", ownerId=").append(ownerId);
        sb.append(", taskId=").append(taskId);
        sb.append(", agentCode=").append(agentCode);
        sb.append(", parallelStartActivityId=").append(parallelStartActivityId);
        sb.append(", hasParallel=").append(hasParallel);
        sb.append(", serializedProcessInstance=").append(serializedProcessInstance);
        sb.append(", errorMessage=").append(errorMessage);
        sb.append(", context=").append(context);
        sb.append(", result=").append(result);
        sb.append("]");
        return sb.toString();
    }
}
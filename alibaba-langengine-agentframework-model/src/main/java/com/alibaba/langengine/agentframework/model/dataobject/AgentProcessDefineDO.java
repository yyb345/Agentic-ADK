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

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author xiaoxuan.lp
 */
public class AgentProcessDefineDO {
    /**
     * Database Column Remarks:
     *   PK
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Long id;

    /**
     * Database Column Remarks:
     *   create time
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Date gmtCreate;

    /**
     * Database Column Remarks:
     *   modification time
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
     *   流程定义id 版本号
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer processDefinitionVersion;

    /**
     * Database Column Remarks:
     *   流程定义类型
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer processDefinitionType;

    /**
     * Database Column Remarks:
     *   状态 1:上线中 2:已下线
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private Integer status;

    /**
     * Database Column Remarks:
     *   流程定义名称
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String processDefinitionName;

    /**
     * Database Column Remarks:
     *   流程定义xml
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String processDefinitionContent;

    /**
     * Database Column Remarks:
     *   日志配置
     *
     *
     * @mbg.generated
     */
    @Getter
    @Setter
    private String processLogConfig;

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
        sb.append(", processDefinitionType=").append(processDefinitionType);
        sb.append(", status=").append(status);
        sb.append(", processDefinitionName=").append(processDefinitionName);
        sb.append(", processDefinitionContent=").append(processDefinitionContent);
        sb.append(", processLogConfig=").append(processLogConfig);
        sb.append("]");
        return sb.toString();
    }
}
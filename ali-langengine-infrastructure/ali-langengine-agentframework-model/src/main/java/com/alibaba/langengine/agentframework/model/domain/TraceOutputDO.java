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

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class TraceOutputDO implements Serializable {

    private Long startTime;

    private Long costTime;

    private Long inputTokens;

    private Long outputTokens;

    private Map<String, Object> request;

    private Object response;

    private Integer status = 1;

    private String activityName;

    /**
     * 节点运行过程中额外的一些属性
     */
    private Map<String, Object> extra;

    /**
     * 节点运行过程中可能存在的调试日志
     */
    private String debugLog;
}

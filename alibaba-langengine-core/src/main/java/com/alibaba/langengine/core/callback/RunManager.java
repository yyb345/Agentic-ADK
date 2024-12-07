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
package com.alibaba.langengine.core.callback;

import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author chenguoli
 * @version : RunManager.java, v 0.1 2024年01月02日 11:08 上午 chenguoli Exp $
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public class RunManager implements Serializable {

    /**
     * id
     */
    private String runId;

    /**
     * 父id
     */
    private String parentRunId;

    /**
     * name
     */
    private String name;

    /**
     * 类型
     */
    private String runType;

    /**
     * 执行顺序
     */
    private int executionOrder = 0;

    /**
     * 元数据
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 继承元数据
     */
    private Map<String, Object> inheritableMetadata = new HashMap<>();

    /**
     * 子级run
     */
    @JsonIgnore
    private List<RunManager> childRunManagers = new CopyOnWriteArrayList<>();

    private void setRunId(String newRunId) {
        this.runId = newRunId;
        this.childRunManagers.forEach(childRunManager -> childRunManager.setParentRunId(newRunId));
    }

    public void onStart() {
        this.setRunId(UUID.randomUUID().toString().toLowerCase());
    }

    public Map<String, Object> getAllMetadata() {
        Map<String, Object> allMetadata = new HashMap<>(this.inheritableMetadata);
        allMetadata.putAll(this.metadata);
        return allMetadata;
    }

    public RunManager getChild() {
        RunManager runManager = new RunManager();
        runManager.setParentRunId(runId);
        runManager.setExecutionOrder(executionOrder + 1);
        runManager.setInheritableMetadata(getAllMetadata());
        getChildRunManagers().add(runManager);
        return runManager;
    }
}
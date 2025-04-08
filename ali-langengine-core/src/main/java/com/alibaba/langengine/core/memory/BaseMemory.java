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
package com.alibaba.langengine.core.memory;

import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 链中内存的基本接口
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseMemory implements Serializable {

    private String humanPrefix = "Human";

    private String aiPrefix = "AI";

    private String systemPrefix = "System";

    private String toolPrefix = "Tool";

    private String memoryKey = "history";

    private Boolean ignoreHuman = false;

    private Boolean ignoreAI = false;

    private Boolean ignoreSystem = false;

    private Boolean ignoreTool = false;


    /**
     * 此内存类将动态加载的输入键
     *
     * @return
     */
    public abstract List<String> memoryVariables();

    /**
     * 给定链的文本输入返回键值对
     * @return
     */
    public Map<String, Object> loadMemoryVariables() {
        return loadMemoryVariables(new HashMap<>());
    }

    /**
     * 给定链的文本输入返回键值对
     *
     * @param inputs
     * @return
     */
    public Map<String, Object> loadMemoryVariables(Map<String, Object> inputs) {
        return loadMemoryVariables(null, inputs);
    }

    public Map<String, Object> loadMemoryVariables(String sessionId) {
        return loadMemoryVariables(sessionId, new HashMap<>());
    }

    public abstract Map<String, Object> loadMemoryVariables(String sessionId, Map<String, Object> inputs);

    /**
     * 将此模型运行的上下文保存到内存中
     *
     * @param inputs
     * @param outputs
     */
    public void saveContext(Map<String, Object> inputs, Map<String, Object> outputs) {
        saveContext(null, inputs, outputs);
    }

    /**
     * 将此模型运行的上下文保存到内存中
     *
     * @param inputs
     * @param outputs
     */
    public abstract void saveContext(String sessionId, Map<String, Object> inputs, Map<String, Object> outputs);

    /**
     * 清除内存内容
     */
    public void clear() {
        clear(null);
    }

    /**
     * 清除内存内容
     */
    public abstract void clear(String sessionId);
}

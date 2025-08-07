/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.flows.service.impl;

import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION
 * 异步任务管理default实现
 *
 * @author baliang.smy
 * @date 2025/7/22 13:50
 */
@Slf4j
public class DefaultTaskInstanceService implements TaskInstanceService {

    private final static Map<String, TaskInstance> persistMap = new ConcurrentHashMap<>();


    @Override
    public String persistTaskInstance(TaskInstance taskInstance) {
        persistMap.put(taskInstance.getId(), taskInstance);
        return taskInstance.getId();
    }


    @Override
    public TaskInstance getTaskInstance(String taskId) {
        return persistMap.get(taskId);
    }


}

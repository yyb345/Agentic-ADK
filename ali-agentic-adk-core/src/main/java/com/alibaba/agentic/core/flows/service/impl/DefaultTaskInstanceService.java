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

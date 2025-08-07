package com.alibaba.agentic.core.flows.service;

import com.alibaba.agentic.core.flows.service.domain.TaskInstance;

/**
 * DESCRIPTION
 * 异步任务管理
 *
 * @author baliang.smy
 * @date 2025/7/22 11:52
 */
public interface TaskInstanceService {

    /**
     * 持久化异步任务
     *
     * @param taskInstance
     * @return
     */
    String persistTaskInstance(TaskInstance taskInstance);

    /**
     * 获取异步任务
     *
     * @param taskId
     * @return
     */
    TaskInstance getTaskInstance(String taskId);
}

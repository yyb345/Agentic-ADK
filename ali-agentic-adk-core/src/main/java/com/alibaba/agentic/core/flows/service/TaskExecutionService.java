package com.alibaba.agentic.core.flows.service;

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.flows.service.domain.AsyncRequest;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 14:07
 */
public interface TaskExecutionService {

    /**
     * 提交异步任务执行
     *
     * @param asyncRequest
     */
    void submitTask(AsyncRequest asyncRequest);

    /**
     * 异步任务回调
     *
     * @param taskId
     * @param result
     */
    void signal(String taskId, Result result);

}

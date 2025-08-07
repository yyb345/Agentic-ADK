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

import com.alibaba.agentic.core.executor.DelegationExecutor;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.flows.service.TaskExecutionService;
import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.domain.AsyncRequest;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.runner.Runner;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 14:14
 */
@Slf4j
public class DefaultTaskExecutionService implements TaskExecutionService {

    private final static Queue<AsyncRequest> queue = new LinkedBlockingQueue<>();

    private final TaskInstanceService taskInstanceService;

    private final Runner runner;

    public DefaultTaskExecutionService(TaskInstanceService taskInstanceService, Runner runner) {
        this.taskInstanceService = taskInstanceService;
        this.runner = runner;
    }

    @PostConstruct
    public void init() {
        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(() -> {
            //log.info("scheduler queue size: {}", queue.size());
            if (queue.isEmpty()) {
                return;
            }
            AsyncRequest task = queue.poll();
            Request request = task.getRequest();
            Flowable<Result> result = DelegationExecutor.invoke(task.getSystemContext(), request);
            signal(task.getTaskId(), DelegationExecutor.receive(task.getSystemContext(), request, result.blockingFirst()));
        }, 1, 1, TimeUnit.SECONDS);
    }


    @Override
    public void submitTask(AsyncRequest asyncRequest) {
        queue.add(asyncRequest);
    }

    @Override
    public void signal(String taskId, Result result) {
        TaskInstance taskInstance = taskInstanceService.getTaskInstance(taskId);
        runner.signal(taskInstance, result);
    }


}

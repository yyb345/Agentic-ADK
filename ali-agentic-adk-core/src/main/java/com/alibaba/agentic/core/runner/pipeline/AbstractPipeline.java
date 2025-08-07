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
package com.alibaba.agentic.core.runner.pipeline;

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.runner.pipe.PipeInterface;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 17:09
 */
@Slf4j
public abstract class AbstractPipeline {

    private final CopyOnWriteArrayList<PipeInterface> pipes = new CopyOnWriteArrayList<>();

    public void addPipe(PipeInterface pipe) {
        this.pipes.add(pipe);
    }

    public void addPipe(List<PipeInterface> pipeList) {
        this.pipes.addAll(pipeList);
    }

    public Flowable<Result> doPipes(PipelineRequest request) {
        return Flowable.fromIterable(pipes).
                concatMap(pipe -> {
                    if (!pipe.ignore(request)) {
                        return pipe.doPipe(request);
                    }
                    return Flowable.empty();
                }).onErrorReturn(throwable -> {
                    log.error("pipe run error", throwable);
                    return Result.fail(throwable);
                });
    }


}

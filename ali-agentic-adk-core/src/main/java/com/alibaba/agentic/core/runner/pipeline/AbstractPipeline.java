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

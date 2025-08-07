package com.alibaba.agentic.core.runner.pipe;

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import io.reactivex.rxjava3.core.Flowable;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 17:06
 */
public interface PipeInterface {

    /**
     * pipe执行
     *
     * @param pipelineRequest
     * @return
     */
    Flowable<Result> doPipe(PipelineRequest pipelineRequest);

    /**
     * 是否忽略
     *
     * @param pipelineRequest
     * @return
     */
    boolean ignore(PipelineRequest pipelineRequest);


}

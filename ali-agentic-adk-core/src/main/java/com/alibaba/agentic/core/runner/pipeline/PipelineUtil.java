package com.alibaba.agentic.core.runner.pipeline;

import com.alibaba.agentic.core.executor.Result;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 17:10
 */
@Component
public class PipelineUtil {

    private static AbstractPipeline pipeline;

    @Autowired
    private AbstractPipeline abstractPipeline;

    public static Flowable<Result> doPipe(PipelineRequest request) {
        return PipelineUtil.pipeline.doPipes(request);
    }

    @PostConstruct
    public void init() {
        PipelineUtil.pipeline = abstractPipeline;
    }

}

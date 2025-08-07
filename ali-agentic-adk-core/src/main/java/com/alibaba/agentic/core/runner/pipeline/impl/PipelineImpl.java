package com.alibaba.agentic.core.runner.pipeline.impl;

import com.alibaba.agentic.core.runner.pipe.PipeInterface;
import com.alibaba.agentic.core.runner.pipeline.AbstractPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 20:37
 */
@Component
public class PipelineImpl extends AbstractPipeline {

    @Autowired
    private List<PipeInterface> agentExecutePipeList;

    @PostConstruct
    public void init() {
        addPipe(agentExecutePipeList);
    }


}

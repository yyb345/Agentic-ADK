package com.alibaba.agentic.core.flows.service;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.FlowCanvas;

public interface AgentProcessService {


    /**
     * 将一个flow花布转换为一个bpmnxml并部署到内存，返回定义的flow信息
     *
     * @param flowCanvas {@link FlowCanvas}
     * @return {@link FlowDefinition}
     */
    FlowDefinition deploy(FlowCanvas flowCanvas);

}

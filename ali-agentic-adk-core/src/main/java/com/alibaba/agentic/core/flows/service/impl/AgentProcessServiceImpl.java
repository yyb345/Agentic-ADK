package com.alibaba.agentic.core.flows.service.impl;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.flows.service.AgentProcessService;
import org.springframework.stereotype.Component;

@Component
public class AgentProcessServiceImpl implements AgentProcessService {


    @Override
    public FlowDefinition deploy(FlowCanvas flowCanvas) {
        return flowCanvas.deploy();
    }
}

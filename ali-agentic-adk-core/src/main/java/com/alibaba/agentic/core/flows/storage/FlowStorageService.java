package com.alibaba.agentic.core.flows.storage;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;

public interface FlowStorageService {

    String saveBpmnXml(FlowDefinition flowDefinition);

    String getBpmnXml(String flowDefinitionCode, String version);

}

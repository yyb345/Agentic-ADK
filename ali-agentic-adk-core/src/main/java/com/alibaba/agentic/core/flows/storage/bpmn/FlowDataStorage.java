package com.alibaba.agentic.core.flows.storage.bpmn;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;

public interface FlowDataStorage {

    String saveBpmnXml(FlowDefinition flowDefinition);

    String getBpmnXml(String flowDefinitionCode, String version);

}
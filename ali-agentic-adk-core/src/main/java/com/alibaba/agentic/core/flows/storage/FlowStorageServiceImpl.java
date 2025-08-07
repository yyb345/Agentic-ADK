package com.alibaba.agentic.core.flows.storage;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.flows.storage.bpmn.FlowDataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowStorageServiceImpl implements FlowStorageService {

    private final FlowDataStorage flowDataStorage;

    @Autowired
    public FlowStorageServiceImpl(FlowDataStorage flowDataStorage) {
        this.flowDataStorage = flowDataStorage;
    }

    @Override
    public String saveBpmnXml(FlowDefinition flowDefinition) {
        return flowDataStorage.saveBpmnXml(flowDefinition);
    }

    @Override
    public String getBpmnXml(String flowDefinitionCode, String version) {
        return flowDataStorage.getBpmnXml(flowDefinitionCode, version);
    }


}

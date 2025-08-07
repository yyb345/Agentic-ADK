package com.alibaba.agentic.core.flows.storage.bpmn;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;

import java.util.concurrent.ConcurrentHashMap;


public class InMemoryFlowDataStorage implements FlowDataStorage {

    // 内存存储结构
    private final ConcurrentHashMap<String, String> bpmnXmlMap = new ConcurrentHashMap<>();

    @Override
    public String saveBpmnXml(FlowDefinition flowDefinition) {
        String key = flowDefinition.getDefinitionId() + ":" + flowDefinition.getVersion();
        bpmnXmlMap.put(key, flowDefinition.getBpmnXml());
        return flowDefinition.getBpmnXml();
    }


    @Override
    public String getBpmnXml(String flowDefinitionId, String version) {
        String key = flowDefinitionId + ":" + version;
        return bpmnXmlMap.getOrDefault(key, null);
    }

}
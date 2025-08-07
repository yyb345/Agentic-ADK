package com.alibaba.agentic.core.flows.storage.bpmn;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.flows.storage.redis.AiRedisTemplate;

public class RedisFlowDataStorage implements FlowDataStorage {

    private final AiRedisTemplate redisTemplate;
    private final String prefix;

    public RedisFlowDataStorage(AiRedisTemplate redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
    }

    @Override
    public String saveBpmnXml(FlowDefinition flowDefinition) {
        String key = prefix + ":bpmn:" + flowDefinition.getDefinitionId() + ":" + flowDefinition.getVersion();
        redisTemplate.set(key, flowDefinition.getBpmnXml());
        return flowDefinition.getBpmnXml();
    }

    @Override
    public String getBpmnXml(String flowDefinitionId, String version) {
        String key = prefix + ":bpmn:" + flowDefinitionId + ":" + version;
        return redisTemplate.get(key);
    }

}
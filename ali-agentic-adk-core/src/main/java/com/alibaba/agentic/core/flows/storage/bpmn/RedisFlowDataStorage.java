/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
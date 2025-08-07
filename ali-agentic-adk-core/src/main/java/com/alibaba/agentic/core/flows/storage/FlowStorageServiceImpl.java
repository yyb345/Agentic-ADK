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

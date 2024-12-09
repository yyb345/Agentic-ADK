/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.agentframework.service;

import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.service.MemoryService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Framework memory service
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class FrameworkMemoryService extends ServiceBase implements MemoryService {

    public FrameworkMemoryService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public String getHistory(String sessionId) {
        log.info("FrameworkMemoryService getHistory sessionId:" + sessionId);
        return null;
    }

    @Override
    public void saveContext(String sessionId, String query, List<ChatAttachment> attachments, Map<String, Object> outputs, int time) {
        log.info("FrameworkMemoryService saveContext sessionId:" + sessionId + ", query:" + query);
    }

}

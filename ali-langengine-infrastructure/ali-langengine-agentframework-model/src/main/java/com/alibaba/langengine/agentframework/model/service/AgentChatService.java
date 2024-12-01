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
package com.alibaba.langengine.agentframework.model.service;

import com.alibaba.langengine.agentframework.model.agent.AgentModel;

import java.util.Map;

/**
 * Agent Chat Service Interface
 *
 * @author xiaoxuan.lp
 */
public interface AgentChatService {

    /**
     * Performs a chat operation using the specified query.
     *
     * @param query the query for chat operation
     * @param agentModel the managed agent
     * @return the result of the chat operation
     */
    ResultDO<Map<String, Object>> chat(String query, AgentModel agentModel);

    /**
     * Performs a chat operation using the specified query, managed agent, session id, and debug flag.
     *
     * @param query
     * @param agentModel
     * @param sessionId
     * @param isDebug
     * @return
     */
    ResultDO<Map<String, Object>> chat(String query, AgentModel agentModel, String sessionId, Boolean isDebug);
}

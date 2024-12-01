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

import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;

import java.util.List;
import java.util.Map;

/**
 * Memory service
 *
 * @author xiaoxuan.lp
 */
public interface MemoryService {

    /**
     * 获取历史记录
     *
     * @param sessionId
     * @return
     */
    String getHistory(String sessionId);

    /**
     * 保存历史记录
     *
     * @param sessionId
     * @param query
     * @param attachments
     * @param outputs
     * @param time
     */
    void saveContext(String sessionId, String query, List<ChatAttachment> attachments, Map<String, Object> outputs, int time);

}

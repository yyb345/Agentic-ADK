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

import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.service.request.RetrievalSearchRequest;
import com.alibaba.langengine.agentframework.model.service.response.RetrievalSearchResponse;

/**
 * Retrieval service
 *
 * @author xiaoxuan.lp
 */
public interface RetrievalService {

    /**
     * 检索
     *
     * @param request
     * @return
     */
    AgentResult<RetrievalSearchResponse> search(RetrievalSearchRequest request);

    /**
     * 检索
     *
     * @param request
     * @return
     */
    AgentResult<RetrievalSearchResponse> onlineSearch(RetrievalSearchRequest request);
}

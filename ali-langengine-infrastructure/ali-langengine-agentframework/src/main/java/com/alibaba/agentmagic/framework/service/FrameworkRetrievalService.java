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
package com.alibaba.agentmagic.framework.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.service.RetrievalService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import com.alibaba.langengine.agentframework.model.service.request.RetrievalSearchRequest;
import com.alibaba.langengine.agentframework.model.service.response.FrameworkDocumentCollection;
import com.alibaba.langengine.agentframework.model.service.response.RetrievalSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;

/**
 * FrameworkRetrievalService
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class FrameworkRetrievalService extends ServiceBase implements RetrievalService {

    public FrameworkRetrievalService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public AgentResult<RetrievalSearchResponse> search(RetrievalSearchRequest request) {
        log.info("DefaultRetrievalService search request:" + JSON.toJSONString(request));
        RetrievalSearchResponse response = new RetrievalSearchResponse();
        FrameworkDocumentCollection<Map<String, Object>> documentCollection = new FrameworkDocumentCollection();
        documentCollection.setDocuments(new ArrayList<>());
        response.setDocumentCollection(documentCollection);
        return AgentResult.success(response);
    }

    @Override
    public AgentResult<RetrievalSearchResponse> onlineSearch(RetrievalSearchRequest request) {
        RetrievalSearchResponse response = new RetrievalSearchResponse();
        FrameworkDocumentCollection<Map<String, Object>> documentCollection = new FrameworkDocumentCollection();
        documentCollection.setDocuments(new ArrayList<>());
        response.setDocumentCollection(documentCollection);
        return AgentResult.success(response);
    }
}

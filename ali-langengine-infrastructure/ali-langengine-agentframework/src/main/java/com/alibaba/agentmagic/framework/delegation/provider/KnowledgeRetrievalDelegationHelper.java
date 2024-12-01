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
package com.alibaba.agentmagic.framework.delegation.provider;

import com.alibaba.agentmagic.framework.delegation.constants.KnowledgeRetrievalConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.domain.KnowledgeRetrievalInput;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.RetrievalService;
import com.alibaba.langengine.agentframework.model.service.request.RetrievalSearchRequest;
import com.alibaba.langengine.agentframework.model.service.response.FrameworkDocumentCollection;
import com.alibaba.langengine.agentframework.model.service.response.RetrievalSearchResponse;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.QUERY_KEY;
import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.REQUEST_ID_KEY;

@Slf4j
public class KnowledgeRetrievalDelegationHelper implements KnowledgeRetrievalConstant {

    public static FrameworkDocumentCollection executeInternal(ExecutionContext executionContext,
                                                              JSONObject properties,
                                                              JSONObject request,
                                                              RetrievalService retrievalService) {
        String requestId = DelegationHelper.getSystemValue(request, REQUEST_ID_KEY);
        String query = DelegationHelper.getSystemValue(request, QUERY_KEY);
        String knowledgeList = properties.getString(KNOWLEDGE_LIST_KEY);
        String knowledgeType = properties.getString(KNOWLEDGE_TYPE_KEY);
        String knowledgeMode = properties.getString(KNOWLEDGE_MODE_KEY);
        Integer knowledgeTopN = properties.getInteger(KNOWLEDGE_TOPN_KEY);

        if(StringUtils.isEmpty(knowledgeMode)) {
            knowledgeMode = KNOWLEDGE_MODE_OFFLINE;
        }
        if(null == knowledgeTopN) {
            knowledgeTopN = 5;
        }
        if(StringUtils.isEmpty(knowledgeType)) {
            knowledgeType = "document";
        }

        if(KNOWLEDGE_TYPE_DOCUMENT.equals(knowledgeType)) {
            RetrievalSearchRequest retrievalSearchRequest = new RetrievalSearchRequest();
            List<KnowledgeRetrievalInput> knowledgeInputs  = null;
            if(knowledgeList != null) {
                knowledgeInputs = JSON.parseArray(knowledgeList, KnowledgeRetrievalInput.class);
            }
            retrievalSearchRequest.setKnowledgeInputs(knowledgeInputs);
            retrievalSearchRequest.setQuery(query);
            retrievalSearchRequest.setKnowledgeTopN(knowledgeTopN);
            retrievalSearchRequest.setKnowledgeType(knowledgeType);

            AgentResult<RetrievalSearchResponse> agentResult = retrievalService.search(retrievalSearchRequest);
            if(!agentResult.isSuccess() || agentResult.getData() == null) {
                throw new AgentMagicException(AgentMagicErrorCode.RETRIEVAL_SEARCH_ERROR,
                        "retrieval search error:" + agentResult.getErrorCode() + "," + agentResult.getErrorMsg(), requestId);
            }
            return agentResult.getData().getDocumentCollection();
        }else if(KNOWLEDGE_TYPE_TABLE.equals(knowledgeType)) {
            RetrievalSearchRequest retrievalSearchRequest = new RetrievalSearchRequest();
            List<KnowledgeRetrievalInput> knowledgeInputs  = null;
            if(knowledgeList != null) {
                knowledgeInputs = JSON.parseArray(knowledgeList, KnowledgeRetrievalInput.class);
            }
            retrievalSearchRequest.setKnowledgeInputs(knowledgeInputs);
            retrievalSearchRequest.setQuery(query);
            retrievalSearchRequest.setKnowledgeTopN(knowledgeTopN);

            AgentResult<RetrievalSearchResponse> agentResult = retrievalService.onlineSearch(retrievalSearchRequest);
            if(!agentResult.isSuccess() || agentResult.getData() == null) {
                throw new AgentMagicException(AgentMagicErrorCode.RETRIEVAL_SEARCH_ERROR,
                        "retrieval onlineSearch error:" + agentResult.getErrorCode() + "," + agentResult.getErrorMsg(), requestId);
            }
            return agentResult.getData().getDocumentCollection();
        }
        return null;
    }
}

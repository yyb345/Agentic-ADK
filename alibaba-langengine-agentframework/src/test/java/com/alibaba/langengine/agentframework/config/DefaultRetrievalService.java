package com.alibaba.langengine.agentframework.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.AgentEngineConfiguration;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.service.RetrievalService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import com.alibaba.langengine.agentframework.model.service.request.RankRerankRequest;
import com.alibaba.langengine.agentframework.model.service.request.RetrievalSearchRequest;
import com.alibaba.langengine.agentframework.model.service.response.FrameworkDocumentCollection;
import com.alibaba.langengine.agentframework.model.service.response.RankRerankResponse;
import com.alibaba.langengine.agentframework.model.service.response.RetrievalSearchResponse;
import com.alibaba.langengine.core.indexes.Document;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认RetrievalService
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class DefaultRetrievalService extends ServiceBase implements RetrievalService {

    public DefaultRetrievalService(AgentEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public AgentResult<RetrievalSearchResponse> search(RetrievalSearchRequest request) {
        log.info("DefaultRetrievalService search request:" + JSON.toJSONString(request));
        List<Document> documents = ((DefaultAgentEngineConfiguration)getAgentEngineConfiguration()).getVectorStore().similaritySearch(request.getQuery(), request.getKnowledgeTopN());

        //默认重排方式
        RankRerankRequest<Document> rankRerankRequest = new RankRerankRequest();
        rankRerankRequest.setDocuments(documents);
        rankRerankRequest.setKnowledgeType(request.getKnowledgeType());
        AgentResult<RankRerankResponse<Document>> agentResult = getAgentEngineConfiguration().getRankService().rerank(rankRerankRequest);

        RetrievalSearchResponse response = new RetrievalSearchResponse();
        response.setDocumentCollection(agentResult.getData().getDocumentCollection());
        return AgentResult.success(response);
    }

    @Override
    public AgentResult<RetrievalSearchResponse> onlineSearch(RetrievalSearchRequest request) {
        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("Unimplemented method 'onlineSearch'");
        RetrievalSearchResponse response = new RetrievalSearchResponse();
        FrameworkDocumentCollection documentCollection = new FrameworkDocumentCollection();
        documentCollection.setKnowledgeType(request.getKnowledgeType());
        documentCollection.setDocuments(new ArrayList<>());
        response.setDocumentCollection(documentCollection);
        return AgentResult.success(response);
    }
}

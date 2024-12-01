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

import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.service.RetrievalStrategyService;
import com.alibaba.langengine.agentframework.model.service.ServiceBase;
import com.alibaba.langengine.agentframework.model.service.request.StrategyProcessRequest;
import com.alibaba.langengine.agentframework.model.service.response.StrategyProcessResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认RetrievalStrategyService
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class FrameworkRetrievalStrategyService extends ServiceBase implements RetrievalStrategyService {

    public FrameworkRetrievalStrategyService(FrameworkEngineConfiguration agentEngineConfiguration) {
        super(agentEngineConfiguration);
    }

    @Override
    public AgentResult<StrategyProcessResponse> process(StrategyProcessRequest request) {
        StrategyProcessResponse response = new StrategyProcessResponse();
        response.setTargetDocuments(request.getSourceDocuments());
        return AgentResult.success(response);
    }
}

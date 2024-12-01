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
package com.alibaba.langengine.agentframework.config;

import com.alibaba.langengine.agentframework.model.FrameworkEngineConfiguration;
import com.alibaba.langengine.agentframework.model.service.*;
import com.alibaba.langengine.agentframework.service.*;
import lombok.Data;

@Data
public class DefaultFrameworkEngineConfiguration implements FrameworkEngineConfiguration {

    private LanguageModelService languageModelService;
    private RetrievalService retrievalService;
    private RetrievalStrategyService retrievalStrategyService;
    private MemoryService memoryService;
    private EmbeddingService embeddingService;
    private ScriptService scriptService;
    private RankService rankService;
    private ToolCallingService toolCallingService;
    private PromptService promptService;
    private DynamicScriptService dynamicScriptService;
    private ApiKeyService apiKeyService;
    private LockService lockService;
    private String llmTemplateConfig;
    private String cotLlmTemplateConfig;
    private Integer cotRetryCount = 0;
    private String cotFallbackLlmTemplateConfig;
    private String grayStrategyConfig;
    private Integer futureTimeout = 60;
    private Boolean sysPromptContainFunctionEnabled;

    public DefaultFrameworkEngineConfiguration() {
        this.languageModelService = new FrameworkLanguageModelService(this);
        this.retrievalService = new FrameworkRetrievalService(this);
        this.retrievalStrategyService = new FrameworkRetrievalStrategyService(this);
        this.memoryService = new FrameworkMemoryService(this);
        this.embeddingService = new FrameworkEmbeddingService(this);
        this.scriptService = new FrameworkScriptService(this);
        this.rankService = new FrameworkRankService(this);
        this.toolCallingService = new FrameworkToolCallingService(this);
        this.promptService = new FrameworkPromptService();
        this.dynamicScriptService = new FrameworkDynamicScriptService(this);
        this.apiKeyService = new FrameworkApiKeyService(this);
        this.lockService = new FrameworkLockService(this);
    }
}
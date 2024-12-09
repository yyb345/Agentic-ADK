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

import com.alibaba.langengine.agentframework.model.AgentEngineConfiguration;
import com.alibaba.langengine.agentframework.model.domain.LoopNodeConfigParam;
import com.alibaba.langengine.agentframework.model.service.*;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;

@Data
public class DefaultAgentEngineConfiguration implements AgentEngineConfiguration {

    private LanguageModelService languageModelService;
    private RetrievalService retrievalService;
    private RetrievalStrategyService retrievalStrategyService;
    private MemoryService memoryService;
    private EmbeddingService embeddingService;
    private ScriptService scriptService;
    private RankService rankService;
    private ToolCallingService toolCallingService;
    private DynamicScriptService dynamicScriptService;
    private PromptService promptService;
    private ApiKeyService apiKeyService;
    private LockService lockService;
    private String llmTemplateConfig;
    private String cotLlmTemplateConfig;
    private Integer cotRetryCount = 0;
    private String cotFallbackLlmTemplateConfig;
    private String grayStrategyConfig;
    private Integer futureTimeout = 60;
    private Boolean sysPromptContainFunctionEnabled;

    private Embeddings embeddings;
    private VectorStore vectorStore;
    private BaseLanguageModel baseLanguageModel;
    private BaseOutputParser baseOutputParser;
    private BaseChatMemory baseChatMemory;

    private LoopNodeConfigParam loopNodeConfigParam;


    public DefaultAgentEngineConfiguration() {
    }
}
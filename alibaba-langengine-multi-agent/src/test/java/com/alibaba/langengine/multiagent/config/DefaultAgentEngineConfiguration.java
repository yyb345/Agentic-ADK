package com.alibaba.langengine.multiagent.config;

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
        setGrayStrategyConfig("{}");
    }
}
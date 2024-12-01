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
package com.alibaba.langengine.agentframework.model;

import com.alibaba.langengine.agentframework.model.service.*;

/**
 * FrameworkEngine configuration
 *
 * @author xiaoxuan.lp
 */
public interface FrameworkEngineConfiguration {

    RetrievalService getRetrievalService();

    void setRetrievalService(RetrievalService retrievalService);

    RetrievalStrategyService getRetrievalStrategyService();

    void setRetrievalStrategyService(RetrievalStrategyService retrievalStrategyService);

    LanguageModelService getLanguageModelService();

    void setLanguageModelService(LanguageModelService languageModelService);

    MemoryService getMemoryService();

    void setMemoryService(MemoryService memoryService);

    RankService getRankService();

    void setRankService(RankService rankService);

    ScriptService getScriptService();

    void setScriptService(ScriptService scriptService);

    ToolCallingService getToolCallingService();

    void setToolCallingService(ToolCallingService toolCallingService);

    PromptService getPromptService();

    void setPromptService(PromptService promptService);

    void setApiKeyService(ApiKeyService apiKeyService);

    ApiKeyService getApiKeyService();

    String getLlmTemplateConfig();

    void setLlmTemplateConfig(String llmTemplateConfig);

    String getCotLlmTemplateConfig();

    void setCotLlmTemplateConfig(String cotLlmTemplateConfig);

    Integer getCotRetryCount();

    void setCotRetryCount(Integer cotRetryCount);

    String getCotFallbackLlmTemplateConfig();

    void setCotFallbackLlmTemplateConfig(String cotFallbackLlmTemplateConfig);

    String getGrayStrategyConfig();

    void setGrayStrategyConfig(String grayStrategyConfig);

    Integer getFutureTimeout();

    void setFutureTimeout(Integer futureTimeout);

    DynamicScriptService getDynamicScriptService();

    void setDynamicScriptService(DynamicScriptService dynamicScriptService);

    Boolean getSysPromptContainFunctionEnabled();

    void setSysPromptContainFunctionEnabled(Boolean sysPromptContainFunctionEnabled);

    void setLockService(LockService lockService);

    LockService getLockService();
}

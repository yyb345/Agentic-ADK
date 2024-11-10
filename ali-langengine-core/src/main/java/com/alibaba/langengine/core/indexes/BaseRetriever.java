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
package com.alibaba.langengine.core.indexes;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.runnables.RunnableStreamCallback;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * BaseRetriever
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseRetriever extends Runnable<RunnableInput, RunnableOutput> {

    private Integer recommendCount = 4;

    /**
     * 回调管理器
     */
    @JsonIgnore
    private BaseCallbackManager callbackManager;

    public BaseCallbackManager getCallbackManager() {
        if(callbackManager == null) {
            callbackManager = LangEngineConfiguration.CALLBACK_MANAGER;
        }
        return callbackManager;
    }

    public void setCallbackManager(BaseCallbackManager callbackManager) {
        if (callbackManager != null && callbackManager.getRunManager() != null) {
            callbackManager.getRunManager().setRunType("retriever");
            callbackManager.getRunManager().setName(this.getClass().getSimpleName());
        }
        this.callbackManager = callbackManager;
    }

    /**
     * 获取与查询相关的文档
     *
     * @param query
     * @return
     */
    public List<Document> getRelevantDocuments(String query) {
        return getRelevantDocuments(query, recommendCount, null, null);
    }

    public List<Document> getRelevantDocuments(String query, int recommendCount) {
        return getRelevantDocuments(query, recommendCount, null, null);
    }

    public List<Document> getRelevantDocuments(String query, int recommendCount, Double maxDistanceValue) {
        return getRelevantDocuments(query, recommendCount, maxDistanceValue, null);
    }

    /**
     * 获取与查询相关的文档
     *
     * @param query
     * @param recommendCount
     * @param maxDistanceValue
     * @param executionContext
     * @return
     */
    public abstract List<Document> getRelevantDocuments(String query, int recommendCount, Double maxDistanceValue, ExecutionContext executionContext);

    @Override
    public RunnableOutput invoke(RunnableInput runnableInput, RunnableConfig config) {
        return invoke(runnableInput, config, null);
    }

    private RunnableOutput invoke(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(runnableInput instanceof RunnableRelevantInput) {
            RunnableRelevantInput runnableRelevantInput = (RunnableRelevantInput) runnableInput;

            List<Document> documents = getRelevantDocuments(runnableRelevantInput.getQuery(), runnableRelevantInput.getRecommendCount());
            RunnableRelevantOutput relevantOutput = new RunnableRelevantOutput();
            relevantOutput.setDocuments(documents);
            if(chunkConsumer != null) {
                chunkConsumer.accept(relevantOutput);
            }
            return relevantOutput;
        }
        return null;
    }

    @Override
    public RunnableOutput stream(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(runnableInput, config, chunkConsumer);
    }

    @Override
    public RunnableOutput streamLog(RunnableInput runnableInput, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if(callbackManager == null) {
            setCallbackManager(new CallbackManager());
            callbackManager.addHandler(new RunnableStreamCallback());
        }
        return invoke(runnableInput, config, chunkConsumer);
    }

    public void onRetrieverStart(BaseRetriever retriever, Map<String, Object> inputs, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }

        if (getCallbackManager() != null) {
            executionContext.setRetriever(retriever);
            executionContext.setRetrieverInput(inputs);
            executionContext.setRetrieverOutput(null);
            getCallbackManager().onRetrieverStart(executionContext);
        }
    }

    public void onRetrieverEnd(BaseRetriever retriever, Map<String, Object> inputs, List<Document> outputs, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }

        if (getCallbackManager() != null) {
            executionContext.setRetriever(retriever);
            executionContext.setRetrieverInput(inputs);
            executionContext.setRetrieverOutput(outputs);
            getCallbackManager().onRetrieverEnd(executionContext);
        }
    }

    public void onRetrieverError(BaseRetriever retriever, Throwable throwable, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }

        if (getCallbackManager() != null) {
            executionContext.setRetriever(retriever);
            executionContext.setThrowable(throwable);
            executionContext.setRetrieverOutput(null);
            getCallbackManager().onRetrieverError(executionContext);
        }
    }
}

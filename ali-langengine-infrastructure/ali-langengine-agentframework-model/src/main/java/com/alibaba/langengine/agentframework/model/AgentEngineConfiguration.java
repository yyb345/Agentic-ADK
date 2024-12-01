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

import com.alibaba.langengine.agentframework.model.domain.LoopNodeConfigParam;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.vectorstore.VectorStore;

/**
 * AgentEngine configuration
 *
 * @author xiaoxuan.lp
 */
public interface AgentEngineConfiguration extends FrameworkEngineConfiguration {

    Embeddings getEmbeddings();

    void setEmbeddings(Embeddings embeddings);

    VectorStore getVectorStore();

    void setVectorStore(VectorStore vectorStore);

    BaseLanguageModel getBaseLanguageModel();

    void setBaseLanguageModel(BaseLanguageModel baseLanguageModel);

    BaseOutputParser getBaseOutputParser();

    void setBaseOutputParser(BaseOutputParser baseOutputParser);

    BaseChatMemory getBaseChatMemory();

    void setBaseChatMemory(BaseChatMemory baseChatMemory);

    LoopNodeConfigParam getLoopNodeConfigParam();

    void setLoopNodeConfigParam(LoopNodeConfigParam loopNodeConfigParam);
}

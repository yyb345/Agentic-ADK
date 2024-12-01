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
package com.alibaba.agentmagic.framework.delegation.constants;

public interface KnowledgeRetrievalConstant {

    /**
     * 知识库列表
     */
    String KNOWLEDGE_LIST_KEY = "knowledgeList";

    /**
     * 知识类型（document，table）
     */
    String KNOWLEDGE_TYPE_KEY = "knowledgeType";

    /**
     * 知识形式（offline，online）
     */
    String KNOWLEDGE_MODE_KEY = "knowledgeMode";
    String KNOWLEDGE_MODE_OFFLINE = "offline";
    String KNOWLEDGE_MODE_ONLINE = "online";
    String OPENSEARCH_INDEX_TABLE_KEY = "opensearchIndexTable";

    /**
     * 知识召回条数
     */
    String KNOWLEDGE_TOPN_KEY = "knowledgeTopN";

    String KNOWLEDGE_SCORE_KEY = "knowledgeScore";

    /**
     * 是否启动retrieval策略
     */
    String RETRIEVAL_STRATEGY_ENABLED_KEY = "retrievalStrategyEnabled";

    /*
     * 知识库类型
     */
    String KNOWLEDGE_TYPE_DOCUMENT = "document";
    String KNOWLEDGE_TYPE_TABLE = "table";

    Integer DEFAULT_KNOWLEDGE_TOPN = 5;

    Double DEFAULT_KNOWLEDGE_SCORE = 0.5;

    String CODE_ENABLE_EXCEPTION_CONFIG = "enableExceptionConfig";
    String CODE_EXCEPTION_CONFIG = "exceptionConfig";
}

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
package com.alibaba.langengine.agentframework.model.domain;

import lombok.Data;
import java.util.List;

/**
 * 知识库检索输入
 *
 * 合并两个版本的字段和注释
 *
 * @author xiaoxuan.lp
 */
@Data
public class KnowledgeRetrievalInput {
    /**
     * 知识库Id
     */
    private String knowledgeId;

    /**
     * 向量嵌入类型，@link{AgentPaasEmbeddingFactory}
     */
    private Integer embeddingType;

    /**
     * 索引表名称，具体是OpenSearch的那一张表
     */
    private String datasourceIndexName;

    /**
     * 知识库类型，对应document或者table
     */
    private String knowledgeType;

    /**
     * 知识库参数列表
     */
    private List<KnowledgeParam> knowledgeParams;

    /**
     * 知识库名称
     */
    private String knowledgeName;

    /**
     * 知识库描述
     */
    private String knowledgeDesc;

    /**
     * 使用那些具体的docId，docId代表的是具体的某个文件
     */
    private List<String> docIds;
}

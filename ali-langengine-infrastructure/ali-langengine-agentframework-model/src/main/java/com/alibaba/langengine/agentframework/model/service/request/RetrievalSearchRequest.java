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
package com.alibaba.langengine.agentframework.model.service.request;

import com.alibaba.langengine.agentframework.model.domain.KnowledgeRetrievalInput;
import lombok.Data;

import java.util.List;

/**
 * RetrievalSearchRequest
 *
 * @author xiaoxuan.lp
 */
@Data
public class RetrievalSearchRequest {

    /**
     * 租户code，默认为default
     */
    private String tenantCode = "default";

    /**
     * agentCode
     */
    private String agentCode;

    /**
     * 检索会话
     */
    private String query;

    /**
     * 支持多query召回，该参数与query参数互斥
     */
    private List<String> queries;

    /**
     * 知识类型
     */
    private String knowledgeType;

    /**
     * 知识入参列表
     */
    private List<KnowledgeRetrievalInput> knowledgeInputs;

    /**
     * 召回topN
     */
    private Integer knowledgeTopN;

    /**
     * 匹配度
     */
    private Double knowledgeScore;

    /**
     * 指定的opensearch索引表
     */
    private String opensearchIndexTable;

    /**
     * 是否仅返回文件详情信息，默认为false
     */
    private Boolean returnFileDetailOnly = false;

    /**
     * 是否启动retrieval策略，默认为false
     */
    private Boolean retrievalStrategyEnabled = false;

    /**
     * 全文召回阈值
     */
    private Long retrievalThreshold;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 用户id
     */
    private String userId;
}

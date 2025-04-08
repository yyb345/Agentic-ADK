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
package com.alibaba.langengine.core.chain.retrievalqa;

import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;

import java.util.List;

import static com.alibaba.langengine.core.config.LangEngineConfiguration.RETRIEVAL_QA_RECOMMEND_COUNT;

/**
 * 默认检索器
 *
 * @author xiaoxuan.lp
 */
@Data
public class RetrievalQA extends BaseRetrievalQA {

    /**
     * TopN 知识库推荐数量
     */
    private int recommend = Integer.valueOf(RETRIEVAL_QA_RECOMMEND_COUNT);

    /**
     * 设置最大距离值
     * 参考：https://help.aliyun.com/apsara/enterprise/v_3_16_2_20220708/hologram/enterprise-ascm-user-guide/vector-prolima.html
     */
    private Double maxDistanceValue;

    private BaseRetriever retriever;

    public List<Document> getDocs(String question) {
        return retriever.getRelevantDocuments(question, recommend, maxDistanceValue);
    }
}

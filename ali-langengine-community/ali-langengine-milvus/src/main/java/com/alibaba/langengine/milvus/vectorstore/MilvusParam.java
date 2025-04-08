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
package com.alibaba.langengine.milvus.vectorstore;

import com.alibaba.fastjson.JSON;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Data;

import java.util.Map;

/**
 * @author: andrea.phl
 * @create: 2023-12-19 10:46
 **/
@Data
public class MilvusParam {

    /**
     * VarChar字段名
     */
    private String fieldNameUniqueId = "content_id";

    /**
     * FloatVector字段名
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * VarChar字段名
     */
    private String fieldNamePageContent = "row_content";

    /**
     * 自定义搜索扩展参数
     */
    private Map<String, Object> searchParams = JSON.parseObject("{\"nprobe\":10, \"offset\":0}");

    /**
     * 初始化参数, 用于创建Collection
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * 是否使用uniqueId作为唯一键, 如果是的话, addDocuments的时候uniqueId不要为空
         */
        private boolean fieldUniqueIdAsPrimaryKey;

        /**
         * pageContent字段VarChar长度
         */
        private int fieldPageContentMaxLength = 8192;

        /**
         * embeddings字段向量维度, 如果设置为0, 则会通过embedding模型查询一条数据, 看维度是多少
         */
        private int fieldEmbeddingsDimension = 1536;

        /**
         * shardsNum
         */
        private int shardsNum = 2;

        /**
         * 构建embeddings索引时传入的IndexType
         */
        private IndexType indexEmbeddingsIndexType = IndexType.IVF_FLAT;

        /**
         * 构建embeddings索引时传入的MetricType
         */
        private MetricType indexEmbeddingsMetricType = MetricType.L2;

        /**
         * 构建embeddings索引时传入的ExtraParam
         */
        private Map<String, Object> indexEmbeddingsExtraParam = JSON.parseObject("{\"nlist\":1024}");

    }

}

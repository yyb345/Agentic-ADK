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
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.milvus.MilvusConfiguration.MILVUS_SERVER_URL;

/**
 * @author: andrea.phl
 * @create: 2023-12-18 10:57
 **/
@Slf4j
@Data
public class Milvus extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 向量库名称
     */
    private final String collection;

    /**
     * 分区名称
     */
    private final String partition;

    private final MilvusService milvusService;

    public Milvus(String collection) {
        this(collection, null);
    }

    public Milvus(String collection, String partition) {
        this(collection, partition, null);
    }

    public Milvus(String collection, String partition, MilvusParam milvusParam) {
        this.collection = collection;
        this.partition = partition;
        // 如果需要指定port请在milvus_server_url属性中设置host:port格式的值，例如：127.0.0.1:19530
        String serverUrl = MILVUS_SERVER_URL;
        milvusService = new MilvusService(serverUrl, collection, partition, milvusParam);
    }

    /**
     * init会在Collection不存在的情况下创建Milvus的Collection，
     * 1. 根据embedding模型结果维度创建embeddings向量字段
     * 2. 创建int64的content_id字段
     * 3. 创建长度8192的row_content字符串字段
     * 4. 对embeddings字段创建索引
     * 如果需要自定义Collection，请按照上面的字段类型规范进行提前创建:
     * 1. 你线下创建Collection(可以修改字段的长度，字段名称，但字段类型不可变），建议以content_id作为主键，这样在文档更新的时候可以覆盖
     * 2. 你同时需要创建Index
     */
    public void init() {
        try {
            milvusService.init(embedding);
        } catch (Exception e) {
            log.error("init milvus failed", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        milvusService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return milvusService.similaritySearch(embeddings, k);
    }

}

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
package com.alibaba.langengine.tair.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.aliyun.tair.tairvector.TairVector;
import com.aliyun.tair.tairvector.factory.VectorBuilderFactory;
import com.aliyun.tair.tairvector.params.DistanceMethod;
import com.aliyun.tair.tairvector.params.IndexAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LangeEngine - Tair向量检索支持
 *
 * https://help.aliyun.com/document_detail/453885.html#section-wu6-ph2-h7j
 * https://kvstore.console.aliyun.com/Redis/instance/cn-hangzhou/r-bp1mg3ubm7n9dbgykx/Normal/VPC/tair.rdb.1g/info
 * https://dms.aliyun.com/?spm=5176.22426891.0.0.38e76375Y32MK0&dbType=redis&instanceSource=RDS&host=r-bp1mg3ubm7n9dbgykx.redis.rds.aliyuncs.com&instanceId=r-bp1mg3ubm7n9dbgykx&regionId=cn-hangzhou&port=6379
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class Tair extends VectorStore {

    /**
     * 向量索引名称
     */
    private String indexName = "default_index";

    /**
     * 向量维度，插入该索引的向量需具有相同的向量维度，取值范围为[1, 32768]。
     */
    private int dims = 1536;

    /**
     * 构建、查询索引的算法
     * FLAT：不单独构建索引，采用暴力搜索的方式执行查询，适合1万条以下的小规模数据集。
     * HNSW：采用HNSW图结构构建整个索引，并通过该算法进行查询，适合大规模的数据集。
     */
    private IndexAlgorithm algorithm = IndexAlgorithm.HNSW;

    /**
     * 计算向量距离函数
     * L2：平方欧氏距离。
     * IP：向量内积。
     * JACCARD：Jaccard距离，且需指定向量数据类型（data_type）为BINARY。
     */
    private DistanceMethod method = DistanceMethod.L2;

    private List<String> indexParams = Arrays.asList("ef_construct", "100", "M", "16");
//    private List<String> indexParamsWithDataType = Arrays.asList("ef_construct", "100", "M", "16", "data_type", "BINARY");
//    private List<String> efParams = Arrays.asList("ef_search", "100");

    private Embeddings embedding;

    private TairVector tairVector;

    /**
     * 创建索引
     *
     * @return
     */
    public boolean createIndexIfNotExist() {
        Map<String, String> index = tairVector.tvsgetindex(indexName);
        if(index != null && index.size() > 0) {
            return false;
        }
        String result = tairVector.tvscreateindex(indexName, dims, algorithm, method, indexParams.toArray(new String[0]));
        log.warn("tvscreateindex result:" + result);
        return true;
    }

    /**
     * 删除索引
     *
     * @return
     */
    public boolean dropIndex() {
        Long result = tairVector.tvsdelindex(indexName);
        log.warn("tvsdelindex result:" + result);
        return result.equals(1L);
    }

    @Override
    public void addDocuments(List<Document> documents) {
        //创建索引
        createIndexIfNotExist();

        if(documents == null || documents.size() == 0) {
            return;
        }
        documents = embedding.embedDocument(documents);
        for (Document document : documents) {
            String uniqueId = !StringUtils.isEmpty(document.getUniqueId()) ? document.getUniqueId() : UUID.randomUUID().toString();
            String embeddingString = JSON.toJSONString(document.getEmbedding());
            List<String> params = new ArrayList<>();
            params.add("content_id");
            params.add(uniqueId);
            params.add("origin_content");
            params.add(document.getPageContent());
            if(document.getMetadata() != null && document.getMetadata().containsKey("name")) {
                params.add("name");
                params.add(document.getMetadata().get("name").toString());
            }
            tairVector.tvshset(indexName, uniqueId, embeddingString, params.toArray(new String[0]));
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.size() == 0 || !embeddingStrings.get(0).startsWith("[")) {
            return new ArrayList<>();
        }
        String embeddingString = embeddingStrings.get(0);
        VectorBuilderFactory.Knn<String> result  = tairVector.tvsknnsearch(indexName, Long.parseLong(String.valueOf(k)), embeddingString);
        Collection<VectorBuilderFactory.KnnItem<String>> knowledgeDOs =  result.getKnnResults();
        if(maxDistanceValue != null) {
            knowledgeDOs = knowledgeDOs.stream().filter(knowledgeDO -> knowledgeDO.getScore() < maxDistanceValue)
                    .collect(Collectors.toList());
        }
        return knowledgeDOs.stream().map(e -> {
            List<String> detail = tairVector.tvshmget(indexName, e.getId(), "content_id", "origin_content", "name");
            Document document = new Document();
            document.setUniqueId(detail.get(0));
            document.setPageContent(detail.get(1));
            document.setScore(e.getScore());
            if(detail.get(2) != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("name", detail.get(2));
                document.setMetadata(metadata);
            }
            return document;
        }).collect(Collectors.toList());
    }
}

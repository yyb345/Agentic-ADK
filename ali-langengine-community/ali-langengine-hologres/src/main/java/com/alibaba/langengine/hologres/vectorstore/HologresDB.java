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
package com.alibaba.langengine.hologres.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.hologres.vectorstore.mapper.HologresKnowledgeMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * hologres向量数据库支持
 *
 * https://hologram.console.aliyun.com/cn-hangzhou/instance/hgpostcn-cn-wwo38m9cq005/info?resourceGroupId=
 * https://holoweb-cn-hangzhou.data.aliyun.com/query
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class HologresDB extends VectorStore {

    private Embeddings embedding;

    private HologresKnowledgeMapper hologresKnowledgeMapper;

    @Override
    public void addDocuments(List<Document> documents) {
        if(documents == null || documents.size() == 0) {
            return;
        }
        documents = embedding.embedDocument(documents);
        for (Document document : documents) {
            KnowledgeDO knowledgeDO = new KnowledgeDO();
            if(!StringUtils.isEmpty(document.getUniqueId())) {
                knowledgeDO.setContentId(Long.parseLong(document.getUniqueId()));
            }
            knowledgeDO.setType(Integer.valueOf(embedding.getModelType()));
            String embeddingString = JSON.toJSONString(document.getEmbedding()).replaceAll("\\[", "{")
                    .replaceAll("\\]", "}");
            knowledgeDO.setContent(embeddingString);
            knowledgeDO.setIdx(document.getIndex());
            knowledgeDO.setRowContent(document.getPageContent());
            if(document.getMetadata() != null) {
                knowledgeDO.setMetadata(JSON.toJSONString(document.getMetadata()));
            }
            hologresKnowledgeMapper.insert(knowledgeDO);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.size() == 0 || !embeddingStrings.get(0).startsWith("[")) {
            return new ArrayList<>();
        }
        String embeddingString = embeddingStrings.get(0);
        embeddingString = embeddingString.replaceAll("\\[", "{")
                .replaceAll("\\]", "}");
        if(type == null) {
            type = Integer.valueOf(embedding.getModelType());
        }
        List<KnowledgeDO> knowledgeDOs = hologresKnowledgeMapper.similaritySearch(embeddingString, type, k);
        if(maxDistanceValue != null) {
            knowledgeDOs = knowledgeDOs.stream().filter(knowledgeDO -> knowledgeDO.getDistance() != null
                            && knowledgeDO.getDistance() < maxDistanceValue)
                    .collect(Collectors.toList());
        }

        return knowledgeDOs.stream().map(e -> {
            Document document = new Document();
            document.setUniqueId(e.getIdx().toString());
            document.setPageContent(filter(e.getRowContent()));
            document.setScore(e.getDistance());
            if(!StringUtils.isEmpty(e.getMetadata())) {
                Map<String, Object> metadata = JSON.parseObject(e.getMetadata(), new TypeReference<Map<String, Object>>(){});
                document.setMetadata(metadata);
            }
            return document;
        }).collect(Collectors.toList());
    }

    private String filter(String value) {
        value = value.replaceAll("<[^>]+>", ""); // 去掉所有HTML标签
        value = StringEscapeUtils.unescapeHtml4(value); // 去掉HTML实体
        return value;
    }
}

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
package com.alibaba.langengine.core.caches;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.outputs.Generation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过相似度来命中问答缓存，类似python的GPTCache(https://github.com/zilliztech/GPTCache)
 *
 * @author xiaoxuan.lp
 */
@Data
public class GPTCache extends BaseCache {

    /**
     * Embedding API
     */
    private Embeddings embedding;

    /**
     * 基础缓存
     */
    private CacheManager cacheManager;

    /**
     * 相似度阀值
     */
    private Double similarityThreshold = 0.8;


    @Override
    public List<Generation> get(String prompt, String llmString) {
        List<Document> documents = cacheManager.getVectorStore().similaritySearch(prompt, 1, similarityThreshold);
        if(documents == null || documents.size() == 0) {
            return null;
        }
        Document document = documents.get(0);
        return cacheManager.getCacheStorage().get(document.getPageContent(), llmString);
    }

    @Override
    public void update(String prompt, String llmString, List<Generation> returnVal) {
        List<Document> documents = new ArrayList<>();
        Document document = new Document();
        document.setPageContent(prompt);
        documents.add(document);
        cacheManager.getVectorStore().addDocuments(documents);
        cacheManager.getCacheStorage().update(prompt, llmString, returnVal);
    }

    @Override
    public void clear() {

    }
}

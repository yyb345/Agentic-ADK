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
package com.alibaba.langengine.demo.cache;

import com.alibaba.langengine.core.caches.CacheManager;
import com.alibaba.langengine.core.caches.GPTCache;
import com.alibaba.langengine.core.caches.InMemoryCache;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

public class GPTCacheTest {

    @Test
    public void test_predict() {
        // success
        Embeddings embedding = new OpenAIEmbeddings();
        InMemoryDB inMemoryDB = new InMemoryDB();
        inMemoryDB.setEmbedding(embedding);

        InMemoryCache cache = new InMemoryCache();

        CacheManager cacheManager = new CacheManager();
        cacheManager.setVectorStore(inMemoryDB);
        cacheManager.setCacheStorage(cache);
        GPTCache gptCache = new GPTCache();
        gptCache.setCacheManager(cacheManager);
        gptCache.setEmbedding(embedding);

        LangEngineConfiguration.CurrentCache = gptCache;

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        long start = System.currentTimeMillis();
        String result = llm.predict("什么是淘宝开放平台？");
        System.out.println(result);
        System.out.println((System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        result = llm.predict("淘宝开放平台的介绍");
        System.out.println(result);
        System.out.println((System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        result = llm.predict("什么是支付宝？");
        System.out.println(result);
        System.out.println((System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        result = llm.predict("支付宝是啥");
        System.out.println(result);
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

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

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import org.junit.jupiter.api.Test;

/**
 * @author: andrea.phl
 * @create: 2023-12-19 11:27
 **/
public class MilvusTest {

    @Test
    public void test() {
        Milvus milvus = new Milvus("knowledge");
        milvus.setEmbedding(new FakeEmbeddings());
        milvus.similaritySearch("hello", 2);
    }

    @Test
    public void testInit() {
        Milvus milvus = new Milvus("knowledge");
        milvus.setEmbedding(new FakeEmbeddings());
//        MilvusService milvusService = milvus.getMilvusService();
//        milvusService.dropCollection();
//        milvus.init();
//        milvus.addTexts(Lists.newArrayList("hello", "ha ha", "hello world"));
//        milvus.addTexts(Lists.newArrayList("hello everyone", "hi", "你好", "好呀"));
        milvus.similaritySearch("hello", 2);

    }
}

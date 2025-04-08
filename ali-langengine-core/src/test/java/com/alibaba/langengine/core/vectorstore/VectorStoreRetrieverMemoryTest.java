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
package com.alibaba.langengine.core.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.memory.impl.VectorStoreRetrieverMemory;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class VectorStoreRetrieverMemoryTest {

    @Test
    public void test_loadMemoryVariables() {
        // success
        InMemoryDB inMemoryDB = new InMemoryDB();
        inMemoryDB.setEmbedding(new FakeEmbeddings());
        VectorStoreRetriever retriever = (VectorStoreRetriever) inMemoryDB.asRetriever();
        retriever.setRecommendCount(1);
        VectorStoreRetrieverMemory memory = new VectorStoreRetrieverMemory();
        memory.setRetriever(retriever);

        Map<String, Object> inputs = new HashMap<>();
        Map<String, Object> outputs = new HashMap<>();

        inputs.put("input", "My favorite food is pizza");
        outputs.put("output", "thats good to know");
        memory.saveContext(inputs, outputs);

        inputs.put("input", "My favorite sport is soccer");
        outputs.put("output", "...");
        memory.saveContext(inputs, outputs);

        inputs.put("input", "I don't the Celtics");
        outputs.put("output", "ok");
        memory.saveContext(inputs, outputs);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", "what sport should i watch?");
        Map<String, Object> response = memory.loadMemoryVariables(variables);

        System.out.println(JSON.toJSONString(response));
    }
}

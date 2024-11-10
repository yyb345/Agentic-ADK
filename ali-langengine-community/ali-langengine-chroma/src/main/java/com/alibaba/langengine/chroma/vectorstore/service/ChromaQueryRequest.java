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
package com.alibaba.langengine.chroma.vectorstore.service;

import lombok.Data;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Data
public class ChromaQueryRequest {

    private final List<List<Double>> queryEmbeddings;
    private final int nResults;
    private final List<String> include = asList("metadatas", "documents", "distances", "embeddings");

    public ChromaQueryRequest(List<Double> queryEmbedding, int nResults) {
        this.queryEmbeddings = singletonList(queryEmbedding);
        this.nResults = nResults;
    }
}

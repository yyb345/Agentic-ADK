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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ChromaEmbeddingsRequest {

    private final List<String> ids;
    private final List<Double> embeddings;
    private final List<String> documents;
    private final List<Map<String, String>> metadatas;

    public ChromaEmbeddingsRequest(Builder builder) {
        this.ids = builder.ids;
        this.embeddings = builder.embeddings;
        this.documents = builder.documents;
        this.metadatas = builder.metadatas;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<String> ids = new ArrayList<>();
        private List<Double> embeddings = new ArrayList<>();
        private List<String> documents = new ArrayList<>();
        private List<Map<String, String>> metadatas = new ArrayList<>();

        public Builder ids(List<String> ids) {
            if (ids != null) {
                this.ids = ids;
            }
            return this;
        }

        public Builder embeddings(List<Double> embeddings) {
            if (embeddings != null) {
                this.embeddings = embeddings;
            }
            return this;
        }

        public Builder documents(List<String> documents) {
            if (documents != null) {
                this.documents = documents;
            }
            return this;
        }

        public Builder metadatas(List<Map<String, String>> metadatas) {
            if (metadatas != null) {
                this.metadatas = metadatas;
            }
            return this;
        }

        public ChromaEmbeddingsRequest build() {
            return new ChromaEmbeddingsRequest(this);
        }
    }
}

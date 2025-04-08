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
package com.alibaba.langengine.core.vectorstore.memory;

import java.util.Objects;

public class EmbeddingMatch {

    private final String embeddingId;
    private final EmbeddingValueEntity embeddingValue;
    private final Double score;

    private final String content;

    private final String name;

    public EmbeddingMatch(String embeddingId, EmbeddingValueEntity embeddingValue, Double score, String content, String name) {
        this.embeddingId = embeddingId;
        this.embeddingValue = embeddingValue;
        this.score = score;
        this.content =content;
        this.name = name;
    }

    public String embeddingId() {
        return embeddingId;
    }

    public Double score() {
        return score;
    }

    public String content() {
        return content;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddingMatch that = (EmbeddingMatch) o;
        return Objects.equals(this.embeddingId, that.embeddingId)
                && Objects.equals(this.embeddingValue, that.embeddingValue)
                && Objects.equals(this.score, that.score)
                && Objects.equals(this.content, that.content)
                && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddingId, embeddingValue, score, content);
    }

    @Override
    public String toString() {
        return "EmbeddingMatch {" +
                " embeddingId = \"" + embeddingId + "\"" +
                ", embeddingValue = " + embeddingValue +
                ", score = " + score +
                ", content = " + content +
                ", name = " + name +
                " }";
    }
}

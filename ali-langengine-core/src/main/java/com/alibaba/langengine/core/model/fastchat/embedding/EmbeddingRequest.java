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
package com.alibaba.langengine.core.model.fastchat.embedding;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Creates an embedding vector representing the input text.
 *
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmbeddingRequest {

    /**
     * The name of the model to use.
     * Required if using the new v1/embeddings endpoint.
     */
    String model;

    /**
     * Input text to get embeddings for, encoded as a string or array of tokens.
     * To get embeddings for multiple inputs in a single request, pass an array of strings or array of token arrays.
     * Each input must not exceed 2048 tokens in length.
     * <p>
     * Unless you are embedding code, we suggest replacing newlines (\n) in your input with a single space,
     * as we have observed inferior results when newlines are present.
     */
    @NonNull
    List<String> input;

    /**
     * A unique identifier representing your end-user, which will help OpenAI to monitor and detect abuse.
     */
    String user;

    /**
     * dict containing the following keys.
     */
    private Map<String, Object> parameters;
}

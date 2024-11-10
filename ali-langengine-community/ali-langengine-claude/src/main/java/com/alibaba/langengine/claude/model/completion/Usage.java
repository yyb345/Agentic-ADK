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
package com.alibaba.langengine.claude.model.completion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Usage {

    /**
     * The number of input tokens which were used.
     */
    @JsonProperty("input_tokens")
    private Integer inputTokens;

    /**
     * The number of input tokens used to create the cache entry.
     */
    @JsonProperty("cache_creation_input_tokens")
    private Integer cacheCreationInputTokens;

    /**
     * The number of input tokens read from the cache.
     */
    @JsonProperty("cache_read_input_tokens")
    private Integer cacheRadInputTokens;

    /**
     * The number of output tokens which were used.
     */
    @JsonProperty("output_tokens")
    private Integer outputTokens;
}

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
package com.alibaba.langengine.moonshot.model.completion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * CompletionChunk
 *
 * @author xiaoxuan.lp
 */
@Data
public class CompletionChunk {

    private String id;

    private String Object;

    private String model;

    private Long created;

    private List<Choice> choices;

    private CompletionResult.Usage usage;

    @Data
    public static class Choice{
        private Long index;
        private CompletionRequest.RoleContent delta;
        @JsonProperty("finish_reason")
        private String finishReason;
    }
}

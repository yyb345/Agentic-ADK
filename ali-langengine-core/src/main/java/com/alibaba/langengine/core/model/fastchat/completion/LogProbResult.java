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
package com.alibaba.langengine.core.model.fastchat.completion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Log probabilities of different token options
 * Returned if {@link com.theokanning.openai.completion.CompletionRequest#logprobs} is greater than zero
 *
 * https://beta.openai.com/docs/api-reference/create-completion
 */
@Data
public class LogProbResult {

    /**
     * The tokens chosen by the completion api
     */
    List<String> tokens;

    /**
     * The log probability of each token in {@link tokens}
     */
    @JsonProperty("token_logprobs")
    List<Double> tokenLogprobs;

    /**
     * A map for each index in the completion result.
     * The map contains the top {@link CompletionRequest#logprobs} tokens and their probabilities
     */
    @JsonProperty("top_logprobs")
    List<Map<String, Double>> topLogprobs;

    /**
     * The character offset from the start of the returned text for each of the chosen tokens.
     */
    List<Integer> textOffset;
}

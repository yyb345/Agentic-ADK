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
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ChatCompletionRequest extends com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest {

    /**
     * Custom text sequences that will cause the model to stop generating.
     * Our models will normally stop when they have naturally completed their turn, which will result in a response stop_reason of "end_turn"
     */
    @JsonProperty("stop_sequences")
    List<String> stop;

    /**
     * System prompt.
     * A system prompt is a way of providing context and instructions to Claude, such as specifying a particular goal or role.
     */
    private String system;

    /**
     * Whether or not to store the output of this chat completion request for use in our model distillation or evals products.
     */
    private String store;
}

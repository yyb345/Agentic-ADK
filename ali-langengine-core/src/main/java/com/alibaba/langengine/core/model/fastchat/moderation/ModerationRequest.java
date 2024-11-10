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
package com.alibaba.langengine.core.model.fastchat.moderation;

import lombok.*;

/**
 * A request for OpenAi to detect if text violates OpenAi's content policy.
 *
 * https://beta.openai.com/docs/api-reference/moderations/create
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModerationRequest {

    /**
     * The input text to classify.
     */
    @NonNull
    String input;

    /**
     * The name of the model to use, defaults to text-moderation-stable.
     */
    String model;
}

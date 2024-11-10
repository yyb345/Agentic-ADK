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
package com.alibaba.langengine.core.model.fastchat.finetune;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * An object representing an event in the lifecycle of a fine-tuning job
 *
 * https://beta.openai.com/docs/api-reference/fine-tunes
 */
@Data
public class FineTuneEvent {
    /**
     * The type of object returned, should be "fine-tune-event".
     */
    String object;

    /**
     * The creation time in epoch seconds.
     */
    @JsonProperty("created_at")
    Long createdAt;

    /**
     * The log level of this message.
     */
    String level;

    /**
     * The event message.
     */
    String message;
}

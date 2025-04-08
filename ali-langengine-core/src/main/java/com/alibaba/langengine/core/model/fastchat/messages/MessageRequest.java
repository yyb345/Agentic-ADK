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
package com.alibaba.langengine.core.model.fastchat.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Creates a Message
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/createMessage
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageRequest {
    /**
     * The role of the entity that is creating the message.
     * Currently only "user" is supported.
     */
    @NonNull
    @Builder.Default
    String role = "user";

    /**
     * The content of the message.
     */
    @NonNull
    String content;

    /**
     * A list of File IDs that the message should use.
     * Defaults to an empty list.
     * There can be a maximum of 10 files attached to a message.
     * Useful for tools like retrieval and code_interpreter that can access and use files.
     */
    @JsonProperty("file_ids")
    List<String> fileIds;

    /**
     * Set of 16 key-value pairs that can be attached to an object.
     * This can be useful for storing additional information about the object in a structured format.
     * Keys can be a maximum of 64 characters long, and values can be a maximum of 512 characters long.
     */
    Map<String, String> metadata;
}

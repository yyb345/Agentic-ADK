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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A list of files attached to a Message
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/file-object
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageFile {
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always thread.message.file.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the message file was created.
     */
    @JsonProperty("created_at")
    int createdAt;

    /**
     * The ID of the message that the File is attached to.
     */
    @JsonProperty("message_id")
    String messageId;
}

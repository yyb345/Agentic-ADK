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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


/**
 * Represents a Message within a thread.
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/object
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Message {
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always thread.message.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the message was created.
     */
    @JsonProperty("created_at")
    int createdAt;

    /**
     * The thread ID that this message belongs to.
     */
    @JsonProperty("thread_id")
    String threadId;

    /**
     * The entity that produced the message. One of user or assistant.
     */
    String role;

    /**
     * The content of the message in an array of text and/or images.
     */
    List<MessageContent> content;

    /**
     * If applicable, the ID of the assistant that authored this message.
     */
    @JsonProperty("assistant_id")
    String assistantId;

    /**
     * If applicable, the ID of the run associated with the authoring of this message.
     */
    @JsonProperty("run_id")
    String runId;

    /**
     * A list of file IDs that the assistant should use.
     * Useful for tools like retrieval and code_interpreter that can access files.
     * A maximum of 10 files can be attached to a message.
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
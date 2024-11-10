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
package com.alibaba.langengine.core.model.fastchat.assistants;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AssistantFile {

    /**
     * The identifier of the Assistant File
     */
    String id;

    /**
     * The object type, which is always assistant.file.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the assistant file was created.
     */
    @JsonProperty("created_at")
    String createdAt;

    /**
     * The assistant ID that the file is attached to
     */
    @JsonProperty("assistant_id")
    String assistantId;
}

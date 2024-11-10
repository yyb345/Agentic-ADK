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
package com.alibaba.langengine.core.model.fastchat.messages.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An annotation for a text Message
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annotation {
    /**
     * The type of annotation, either file_citation or file_path
     */
    String type;

    /**
     * The text in the message content that needs to be replaced
     */
    String text;

    /**
     * File citation details, only present when type == file_citation
     */
    @JsonProperty("file_citation")
    FileCitation fileCitation;

    /**
     * File path details, only present when type == file_path
     */
    @JsonProperty("file_path")
    FilePath filePath;

    @JsonProperty("start_index")
    int startIndex;

    @JsonProperty("end_index")
    int endIndex;
}

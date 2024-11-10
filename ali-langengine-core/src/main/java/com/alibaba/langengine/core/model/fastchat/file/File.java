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
package com.alibaba.langengine.core.model.fastchat.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A file uploaded to OpenAi
 *
 * https://beta.openai.com/docs/api-reference/files
 */
@Data
public class File {

    /**
     * The unique id of this file.
     */
    String id;

    /**
     * The type of object returned, should be "file".
     */
    String object;

    /**
     * File size in bytes.
     */
    Long bytes;

    /**
     * The creation time in epoch seconds.
     */
    @JsonProperty("created_at")
    Long createdAt;

    /**
     * The name of the file.
     */
    String filename;

    /**
     * Description of the file's purpose.
     */
    String purpose;
}

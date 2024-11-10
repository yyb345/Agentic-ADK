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
package com.alibaba.langengine.core.model.fastchat.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A request for OpenAi to create a variation of an image
 * All fields are optional
 *
 * https://beta.openai.com/docs/api-reference/images/create-variation
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateImageVariationRequest {

    /**
     * The number of images to generate. Must be between 1 and 10. Defaults to 1.
     */
    Integer n;

    /**
     * The size of the generated images. Must be one of "256x256", "512x512", or "1024x1024". Defaults to "1024x1024".
     */
    String size;

    /**
     * The format in which the generated images are returned. Must be one of url or b64_json. Defaults to url.
     */
    @JsonProperty("response_format")
    String responseFormat;

    /**
     * A unique identifier representing your end-user, which will help OpenAI to monitor and detect abuse.
     */
    String user;
}

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
package com.alibaba.langengine.core.model.fastchat.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * gpt-4v会使用
 * model：
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChatMessageContent {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE_URL = "image_url";

    /**
     * user message content type
     * 包含：text、image_url
     */
    String type;

    /**
     * text
     *
     * type为text时使用
     */
    String text;

    /**
     * image_url
     *
     * 包含：url，可以用http也可以用base64（data:image/jpeg;base64,{base64_image}）
     */
    @JsonProperty("image_url")
    Map<String, Object> imageUrl;
}

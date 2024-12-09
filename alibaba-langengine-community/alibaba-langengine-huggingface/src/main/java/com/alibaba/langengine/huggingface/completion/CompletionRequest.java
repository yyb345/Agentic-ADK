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
package com.alibaba.langengine.huggingface.completion;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * HuggingFace CompletionRequest
 *
 * @author xiaoxuan.lp
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionRequest extends ChatCompletionRequest {

    /**
     * a string or object to be generated from.
     * required
     */
    private Object inputs;

    /**
     * dict containing the following keys.
     */
    private Map<String, Object> parameters;

    /**
     * a dict containing the following keys.
     */
    private Map<String, Object> options;
}

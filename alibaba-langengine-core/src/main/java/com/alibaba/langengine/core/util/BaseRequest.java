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
package com.alibaba.langengine.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * BaseRequest.
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class BaseRequest extends ChatCompletionRequest {

    private static final long serialVersionUID = -3999733542020328421L;
    @JsonIgnore
    private Map<String, Object> extraAttributes;

    public JSONObject toJsonRequest() {
        JSONObject request = (JSONObject) JSON.toJSON(this);

        if (extraAttributes != null) {
            request.putAll(extraAttributes);
        }
        return request;
    }
}

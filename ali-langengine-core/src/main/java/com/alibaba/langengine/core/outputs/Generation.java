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
package com.alibaba.langengine.core.outputs;

import com.alibaba.langengine.core.messages.BaseMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * A single text generation output.
 *
 * @author xiaoxuan.lp
 */
@Data
public class Generation implements Serializable {

    /**
     * 生成的文本输出
     */
    private String text;

    /**
     * The message output by the chat model.
     */
    private BaseMessage message;

    /**
     * 来自提供商的原始生成信息响应
     */
    private Map<String, Object> generationInfo;
}

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
package com.alibaba.langengine.dashscope.model.completion;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * DashScope CompletionRequest
 *
 * @author xiaoxuan.lp
 */
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CompletionRequest extends ChatCompletionRequest {

    /**
     * 包括prompt字段
     * { "prompt": "就当前的海洋污染的情况，写一份限塑的倡议书提纲，需要有理有据地号召大家克制地使用塑料制品" }
     */
    Map<String, Object> input;

    /**
     * parameters包括top_p，max_length，enable_search
     */
    private Map<String, Object> parameters;

    /**
     * history
     */
    List<String> history;

    /**
     * 接口输入和输出的信息是否通过绿网过滤，默认不调用绿网
     */
    private boolean dataInspection = false;

}

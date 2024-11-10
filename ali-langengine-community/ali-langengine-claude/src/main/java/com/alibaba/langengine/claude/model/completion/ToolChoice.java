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
package com.alibaba.langengine.claude.model.completion;

import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 工具选择
 *
 * @author xiaoxuan.lp
 */
@Data
public class ToolChoice {

    /**
     * auto, any, tool
     */
    private String type;

    /**
     * Available options: auto, any, tool
     *
     * Whether to disable parallel tool use.
     * Defaults to false. If set to true, the model will output at most one tool use.
     */
    @JsonProperty("disable_parallel_tool_use")
    private Boolean disableParallelToolUse = false;

    /**
     * Available options: tool
     * The name of the tool to use.
     */
    private String name;

    /**
     * tools collection
     */
    private List<FunctionProperty> tools;
}

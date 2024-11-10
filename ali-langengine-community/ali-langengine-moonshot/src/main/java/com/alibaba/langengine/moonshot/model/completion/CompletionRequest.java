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
package com.alibaba.langengine.moonshot.model.completion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionRequest {

    /** 必填,目前是 moonshot-v1-8k,moonshot-v1-32k,moonshot-v1-128k 其一 */
    private String model;

    /** 必填，包含迄今为止对话的消息列表 */
    private List<RoleContent>  messages;

    // ---------- 以下为选填内容 -------------

    /** 选填，使用什么采样温度，介于 0 和 1 之间。较高的值（如 0.7）将使输出更加随机，而较低的值（如 0.2）将使其更加集中和确定性 */
    private Double temperature;

    /** 选填，另一种采样方法，即模型考虑概率质量为 top_p 的标记的结果。因此，0.1 意味着只考虑概率质量最高的 10% 的标记。一般情况下，我们建议改变这一点或温度，但不建议 同时改变 */
    @JsonProperty("top_p")
    private Double topP;

    /** 选填，聊天完成时生成的最大 token 数。如果到生成了最大 token 数个结果仍然没有结束，finish reason 会是 "length", 否则会是 "stop"*/
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /** 选填，为每条输入消息生成多少个结果。 默认为 1，不得大于 5。特别的，当 temperature 非常小靠近 0 的时候，我们只能返回 1 个结果*/
    private Integer n;

    /** 选填，存在惩罚，介于-2.0到2.0之间的数字。正值会根据新生成的词汇是否出现在文本中来进行惩罚，增加模型讨论新话题的可能性.默认为 0 */
    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    /** 选填，频率惩罚，介于-2.0到2.0之间的数字。正值会根据新生成的词汇在文本中现有的频率来进行惩罚，减少模型一字不差重复同样话语的可能性.默认为 0 */
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    /** 选填，停止词，当全匹配这个（组）词后会停止输出，这个（组）词本身不会输出。最多不能超过 5 个字符串，每个字符串不得超过 32 字节 */
    private List<String> stop;

    /** 选填，是否流式返回.默认 false, 可选 true */
    private Boolean stream;


    @Data
    public static class RoleContent{
        // 参考Role或自己定制
        private String role;
        private String content;
    }

    public static enum Role{
        // 当前moonshot只支持这几种角色
        system,user,assistant;
    }
}

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
package com.alibaba.langengine.minimax.model.model;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author qiongjin
 * @date 2023/11/16
 */
@Data
public class MiniMaxResult {
    /**
     * 请求发起时间
     */
    private Long created;

    /**
     * 请求指定的模型名称
     */
    private String model;

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 输入命中敏感词
     */
    @JSONField(name = "input_sensitive")
    private Boolean inputSensitive;

    /**
     * 输入命中敏感词类型，当input_sensitive为true时返回
     *
     * 取值为以下其一：1 严重违规；2 色情；3 广告；4 违禁；5 谩骂；6 暴恐；7 其他
     */
    @JSONField(name = "input_sensitive_type")
    private Integer inputSensitiveType;

    /**
     * 输出命中敏感词
     */
    @JSONField(name = "output_sensitive")
    private Boolean outputSensitive;

    /**
     * 输出命中敏感词类型，当output_sensitive为true时返回
     *
     * 取值为以下其一：1 严重违规；2 色情；3 广告；4 违禁；5 谩骂；6 暴恐；7 其他
     */
    @JSONField(name = "output_sensitive_type")
    private Integer outputSensitiveType;

    /**
     * 所有结果
     */
    private List<Choice> choices;

    /**
     * tokens数使用情况，流式场景下仅最后一个数据包含该字段
     */
    private Usage usage;

    /**
     * 本次请求ID，用于问题排查
     */
    private String id;

    /**
     * 请求信息
     */
    @JSONField(name = "base_resp")
    private BaseResp baseResp;

    @Data
    public static class Choice {

        /**
         * 回复结果的具体内容
         */
        private List<MiniMaxMessage> messages;

        /**
         * 排名
         */
        private Long index;

        /**
         * 结束原因，枚举值
         *
         * stop：接口返回了模型生成完整结果
         * length：模型生成结果超过配置的tokens_to_generate长度，内容被截断
         * max_output：输入+模型输出内容超过模型最大能力限制
         */
        @JSONField(name = "finish_reason")
        private String finishReason;
    }

    @Data
    public static class Usage {
        /**
         * 消耗tokens总数，包括输入和输出
         */
        @JSONField(name = "total_tokens")
        private Long totalTokens;
    }

    @Data
    public static class BaseResp {
        /**
         * 状态码
         */
        @JSONField(name = "status_code")
        private Long statusCode;

        /**
         * 错误信息
         */
        @JSONField(name = "status_msg")
        private String statusMsg;
    }
}

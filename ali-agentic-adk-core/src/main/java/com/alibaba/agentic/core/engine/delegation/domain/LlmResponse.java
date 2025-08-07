/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * DESCRIPTION: 通用大语言模型（LLM）响应对象, 可适配 OpenAI、ChatGLM、百度、讯飞、阿里等主流 LLM 厂商的响应格式
 *
 * @author baliang.smy
 * @date 2025/7/16 20:31
 */
@Data
@Accessors(chain = true)
public class LlmResponse {

    /**
     * 本次请求响应的唯一标识
     */
    private String id;

    /**
     * 响应对象类型，例如 "text_completion", "chat.completion"
     */
    private String object;

    /**
     * 响应创建时间戳（秒级，部分平台有）
     */
    private Long created;

    /**
     * 生成结果列表
     */
    private List<Choice> choices;

    /**
     * token 用量统计
     */
    private Usage usage;

    /**
     * 错误信息（有错误时填充）
     */
    private ErrorInfo error;

    /**
     * 单个生成结果
     */
    @Data
    public static class Choice {
        /**
         * 输出内容（completion 或 message.content）
         */
        private String text;

        /**
         * 对话模型下的消息结构（如 OpenAI）
         */
        private Message message;

        /**
         * 生成结束原因，如 "stop"、"length"
         */
        private String finishReason;

        /**
         * 结果索引（有些接口有）
         */
        private Integer index;
    }

    /**
     * 对话消息体（适用于 chat 场景）
     */
    @Data
    public static class Message {
        /**
         * 消息角色（assistant、user、system）
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }

    /**
     * token 用量统计信息
     */
    @Data
    public static class Usage {
        /**
         * 输入 token 数
         */
        private Integer promptTokens;

        /**
         * 输出 token 数
         */
        private Integer completionTokens;

        /**
         * 总 token 数
         */
        private Integer totalTokens;
    }

    /**
     * 错误信息结构体
     */
    @Data
    public static class ErrorInfo {
        /**
         * 错误码
         */
        private String code;

        /**
         * 错误类型
         */
        private String type;

        /**
         * 错误描述信息
         */
        private String message;
    }
}

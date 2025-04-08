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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiongjin
 * @date 2023/11/16
 */
@Data
public class MiniMaxParameters {
    /**
     * 模型名称
     * abab5.5-chat
     */
    private String model;

    /**
     * 是否是流式，默认为非流式的方式
     */
    private Boolean stream;

    /**
     * 最大生成token数
     */
    @JSONField(name = "tokens_to_generate")
    private Long tokensToGenerate;

    private Float temperature;

    @JSONField(name = "top_p")
    private Float topP;

    /**
     * 隐私信息是否打码
     */
    @JSONField(name = "mask_sensitive_info")
    private Boolean maskSensitiveInfo;

    /**
     * 对话内容
     */
    private List<MiniMaxMessage> messages;

    /**
     * 机器人设置
     */
    @JSONField(name = "bot_setting")
    private List<BotSetting> botSetting;

    /**
     * 模型回复要求
     */
    @JSONField(name = "reply_constraints")
    private ReplyConstraints replyConstraints;

    @Data
    public static class BotSetting {
        /**
         * 机器人名称
         */
        @JSONField(name = "bot_name")
        private String botName;

        /**
         * 机器人设定
         */
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyConstraints {
        /**
         * 指定回复角色类型，当前仅支持BOT机器人
         */
        @JSONField(name = "sender_type")
        private String senderType;

        /**
         * 指定回复的机器人名称
         */
        @JSONField(name = "sender_name")
        private String senderName;
    }
}

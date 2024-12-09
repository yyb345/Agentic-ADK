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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MiniMaxMessage {
    /**
     * 发送者的类型，USER/BOT
     */
    @JSONField(name = "sender_type")
    private String senderType;

    /**
     * 发送者的名称
     */
    @JSONField(name = "sender_name")
    private String senderName;

    /**
     * 发送者的消息
     */
    private String text;

    @JSONField(name = "delta")
    private String delta;

    //finish_reason
    @JSONField(name = "finish_reason")
    private String finishReason;
}

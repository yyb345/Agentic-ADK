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
package com.alibaba.langengine.agentframework.model.domain;

import com.alibaba.langengine.agentframework.model.enums.ChatMsgMemTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 会话消息
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChatMessage {

    public static final String TYPE_ANSWER = "answer";
    public static final String TYPE_FUNCTION_CALL = "function_call";
    public static final String TYPE_TOOL_RESPONSE = "tool_response";
    public static final String TYPE_FOLLOW_UP = "follow_up";

    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_HTML = "html";
    public static final String CONTENT_TYPE_MARKDOWN = "markdown";
    public static final String CONTENT_TYPE_JSON = "json";
    public static final String CONTENT_TYPE_COMPONENT_CARD = "card";

    // 消息节点的文本输出
    public static final String CONTENT_TYPE_TEMP_MESSAGE = "temp_message";

    // 老的硬编码卡片，不建议再使用
    @Deprecated
    public static final String CONTENT_TYPE_STATUS_CARD_LOADING = "card.loading";

    /**
     * 发送这条消息的实体。取值：
     * user：代表该条消息内容是用户发送的
     * assistant：代表该条消息内容是 Agent 发送的
     */
    private String role;

    /**
     * 当 role=user 时，用于标识 Bot的消息类型，取值：
     * multi：多模态生成内容
     *
     * 当 role=assistant 时，用于标识 Bot 的消息类型，取值：
     * answer：Agent 最终返回给用户的消息内容。
     * function_call：Agent 对话过程中调用函数 (function call) 的中间结果。
     * tool_response：调用工具 (function call) 后返回的结果。
     * follow_up：如果在 Agent 上配置打开了用户问题建议开关，则会返回推荐问题相关的回复内容。
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息内容的类型，取值。
     * text: 文本类型。
     * markdown: 当 type = answer 时，消息内容格式为 Markdown。即 Bot 的最终回复的内容格式是 Markdown。
     * html: 当 type = answer 时，消息内容格式为 Html。即 Bot 的最终回复的内容格式是 Html。
     * json: 当 type = answer 时，消息内容格式为 Json。即 Bot 的最终回复的内容格式是 Json。
     */
    private String contentType;

    /**
     * 会话附件
     */
    private List<ChatAttachment> chatAttachments;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 回复ID，上一条回复ID（未启用）
     */
    private String replyId;

    /**
     * 分块id，例如function_call和tool_response是同一个sectionId
     */
    private String sectionId;

    /**
     * 发送者ID，一般是工号
     */
    private String senderId;

    /**
     * 记忆类型
     * @see ChatMsgMemTypeEnum
     * 业务保存持久化对话历史的时候需要保存该字段，并在后续请求中把该字段带上
     */
    private Integer memoryType;

    /**
     * 扩展字段
     */
    private ChatExtraInfo extraInfo = new ChatExtraInfo();
}

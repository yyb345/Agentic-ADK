package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhenkui.yzk
 * @date 2025/07/25
 * @descrtion：请求大语言模型（LLM）的参数对象，包含模型选择、生成设置、用户输入等常用参数，适配主流 LLM（如 OpenAI、ChatGLM 等）的请求格式
 */
@Data
@Accessors(chain = true)
public class LlmRequest {
    /**
     * 模型族
     */
    private String model;
    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 生成文本的最大长度（token数量）
     */
    private Integer maxTokens;

    /**
     * 采样温度，控制输出的随机性，值越高结果越多样
     * 通常在 0-2 之间，常用值为 0.7
     */
    private Double temperature;

    /**
     * 用于 nucleus 采样的参数，控制输出多样性，范围 0-1
     */
    private Double topP;

    /**
     * 生成内容时遇到这些字符串自动停止，常用于限定输出范围
     */
    private List<String> stop;

    /**
     * 是否以流式方式返回结果（适用于支持流式响应的 API）
     */
    private Boolean stream;

    /**
     * 用户唯一标识（可选），便于上下文管理或审计
     */
    private String user;

    /**
     * 多轮对话的消息列表，适用于支持 Chat 场景的模型
     */
    private List<Message> messages;

    /**
     * 额外参数，适用于特定平台扩展字段，可为 null。
     */
    private Object extraParams;

    /**
     * 对话消息体（Chat 场景下使用）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        /**
         * 角色名称，如 "system", "user", "assistant"
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}
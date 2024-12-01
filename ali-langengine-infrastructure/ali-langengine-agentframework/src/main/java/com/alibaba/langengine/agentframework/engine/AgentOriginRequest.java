package com.alibaba.langengine.agentframework.engine;

import com.alibaba.langengine.agentframework.model.agent.AgentModel;
import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import lombok.Data;

import java.util.List;
import java.util.function.Consumer;

/**
 * Agent原始请求体
 *
 * @author xiaoxuan.lp
 */
@Data
public class AgentOriginRequest {

    /**
     * agentCode
     * 必填
     */
    private String agentCode;

    /**
     * 应用名称
     * 选填
     */
    private String agentName;

    /**
     * 会话问题
     * 选填
     */
    private String query;

    /**
     * 会话附件
     * 选填
     */
    private List<ChatAttachment> attachments;

    /**
     * 会话id
     * 选填
     */
    private String sessionId;

    /**
     * 会话上下文记录，如果有值，以这个history为准
     * 选填
     */
    private List<ChatMessage> chatHistory;

    /**
     * agent模型（目前包括FlowAgentModel、BuiltInAgentModel）
     * 必填
     */
    private AgentModel agentModel;

    /**
     * requestId
     * 选填
     */
    private String requestId;

    /**
     * 是否组件节点异步化metaq
     */
    private Boolean async = false;

    /**
     * 流式输出
     */
    Consumer<Object> chunkConsumer;
}

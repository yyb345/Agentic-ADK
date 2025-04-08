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
package com.alibaba.langengine.core.callback.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import lombok.Data;

/**
 * @author aihe.ah
 * @time 2023/11/22
 * 功能说明：
 */
@Data
public class CallBackTraceDTO {

    /**
     * 当前的交互发送在那一次的会话中
     */
    private String sessionId;

    /**
     * traceId
     */
    private String traceId;

    /**
     * 上下文中的用户id
     */
    private String userId;

    /**
     * 业务的关键Id，方便排查问题、比如模板Id，助手Id
     */
    private String bizId;

    /**
     * 业务上下文，业务可能想要处理的过程中处理一下自己的上下文数据
     */
    private Map<String, Object> bizContext;

    /**
     * 业务的埋点数据，放在这里面的内容都会打印出来
     */
    private Map<String, Object> utBizParams;

    /**
     * eagleEyeCtx
     */
    private Object eagleEyeCtx;

    /**
     * chain实例ID
     */
    private String chainInstanceId;

    /**
     * 异常
     */
    private Throwable throwable;

    /** ---------- chain start ---------- **/

    /**
     * chain
     */
    private Map<String, Object> chainInfo;

    /**
     * 请求内容
     */
    private Map<String, Object> inputs;

    /**
     * 响应内容
     */
    private Map<String, Object> outputs;

    /** ---------- chain end ---------- **/

    /** ---------- llm start ---------- **/

    /**
     * 执行类型
     */
    private String executionType;

    /**
     * 请求-大模型
     */
    private Map<String, Object> llmInfo;

    /**
     * 请求-prompts
     */
    private List<String> prompts;

    /**
     * 响应-llmResult
     */
    private LLMResult llmResult;

    /** ---------- llm end ---------- **/

    /**
     * child 执行类型
     */
    private String childExecutionType;

    /** ---------- child chain start ---------- **/

    /**
     * child chain
     */
    private Map<String, Object> childChainInfo;

    /**
     * 子请求内容
     */
    private Map<String, Object> childInputs;

    /**
     * 子响应内容
     */
    private Map<String, Object> childOutputs;

    /** ---------- chain end ---------- **/

    /** ---------- tool start ---------- **/

    /**
     * tool
     */
    private Map<String, Object> toolInfo;

    /**
     * toolInput
     */
    private String toolInput;

    /**
     * tool响应
     */
    private ToolExecuteResult toolExecuteResult;

    private AgentAction agentAction;

    private AgentFinish agentFinish;

    /** ---------- retriever start ---------- **/

    /**
     * retriever
     */
    private Map<String, Object> retrieverInfo;

    /**
     * retriever input
     */
    private Map<String, Object> retrieverInput;

    /**
     * retriever output
     */
    private List<Document> retrieverOutput;

    /**
     * 记录时间
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 记录当前所处的阶段
     */
    private String stage;
}

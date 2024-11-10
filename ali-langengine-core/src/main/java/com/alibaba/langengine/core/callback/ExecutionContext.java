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
package com.alibaba.langengine.core.callback;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.runnables.RunnableTraceData;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 执行上下文
 *
 * @author xiaoxuan.lp
 */
@Data
public class ExecutionContext<T> {

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
    private com.alibaba.langengine.core.chain.Chain chain;

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
    private BaseLanguageModel llm;

    /**
     * 请求-prompts
     */
    private List<String> prompts;

    /**
     * 响应-llmResult
     */
    private LLMResult llmResult;

    /** ---------- llm end ---------- **/


    /** ---------- chat model start ---------- **/

    List<List<BaseMessage>> messages;

    List<FunctionDefinition> functions;

    /** ---------- chat model end ---------- **/


    /**
     * child 执行类型
     */
    private String childExecutionType;

    /** ---------- child chain start ---------- **/

    /**
     * child chain
     */
    private com.alibaba.langengine.core.chain.Chain childChain;

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
    private BaseTool tool;

    /**
     * toolInput
     */
    private String toolInput;

    /**
     * tool响应
     */
    private ToolExecuteResult toolExecuteResult;

    /** ---------- tool end ---------- **/


    private AgentAction agentAction;
    private AgentFinish agentFinish;

    /** ---------- retriever start ---------- **/

    /**
     * retriever
     */
    private BaseRetriever retriever;

    /**
     * retriever input
     */
    private Map<String, Object> retrieverInput;

    /**
     * retriever output
     */
    private List<Document> retrieverOutput;


    /** ---------- retriever end ---------- **/


    private Consumer<T> chunkConsumer;

    private RunnableTraceData traceData;

    public boolean isContainChildChain() {
        return getChildExecutionType() != null && getChildExecutionType().startsWith("childChain-");
    }

    public boolean isContainTool() {
        return getChildExecutionType() != null && getChildExecutionType().startsWith("tool-");
    }
}

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
package com.alibaba.langengine.dashscope.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ToolDefinition;
import com.alibaba.langengine.core.outputs.context.LlmResultHolder;
import com.alibaba.langengine.core.util.JsonUtils;
import com.alibaba.langengine.core.util.LLMUtils;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.agent.DashScopeAPIChainUrlOutputParser;
import com.alibaba.langengine.dashscope.model.agent.DashScopePromptConstants;
import com.alibaba.langengine.dashscope.model.agent.DashScopeStructuredChatOutputParser;
import com.alibaba.langengine.dashscope.model.completion.CompletionRequest;
import com.alibaba.langengine.dashscope.model.completion.CompletionResult;
import com.alibaba.langengine.dashscope.model.service.DashScopeService;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * 通义千问Dashscope大模型
 * https://help.aliyun.com/zh/dashscope/developer-reference/api-details
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
@ToString(exclude = "token")
public class DashScopeLLM extends BaseLLM<CompletionRequest> {

    private DashScopeService service;

    /**
     * 生成时使用的随机数种子，用户控制模型生成内容的随机性。seed支持无符号64位整数，默认值为1234。在使用seed时，模型将尽可能生成相同或相似的结果，但目前不保证每次生成的结果完全相同。
     */
    private Integer seed;

    /**
     * 控制在流式输出模式下是否开启增量输出，即后续输出内容是否包含已输出的内容。设置为True时，将开启增量输出模式，后面输出不会包含已经输出的内容，您需要自行拼接整体输出；设置为False则会包含已输出的内容。
     */
    private Boolean incrementalOutput;

    /**
     * 是否启动 web 搜索功能，默认为false。
     */
    private boolean enableSearch = false;

    /**
     * 接口输入和输出的信息是否通过绿网过滤，默认不调用绿网。
     */
    private boolean dataInspection = false;

    /**
     * 其他扩展字段
     */
    private Map<String,Object> parameters;

    /**
     * 是否流式增量
     */
    private boolean sseInc = true;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeLLM() {
        this(DASHSCOPE_API_KEY);
    }

    public DashScopeLLM(String token) {
        setModel(DashScopeModelName.QWEN_TURBO);
        setMaxTokens(256);
        setTopP(0.8d);
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token);
    }

    @Override
    public CompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", chatMessages.get(0).getContent());
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .input(input);

        Map<String, Object> parameters = new HashMap<>();
        if (getTopP() != null) {
            parameters.put("top_p", getTopP());
        }
        if (enableSearch) {
            parameters.put("enable_search", enableSearch);
        }
        if (dataInspection) {
            parameters.put("dataInspection", "enable");
        }

        if (seed != null) {
            parameters.put("seed", seed);
        }

        if (getMaxTokens() != null) {
            parameters.put("max_tokens", getMaxTokens());
        }

        if (incrementalOutput != null) {
            parameters.put("incremental_output", incrementalOutput);
        }

        if(this.parameters != null){
            parameters.putAll(this.parameters);
        }

        builder.parameters(parameters);

        if (extraAttributes!=null&& Objects.nonNull(extraAttributes.get("functions"))) {
            List<ToolDefinition> toolDefinitionList = (List<ToolDefinition>)extraAttributes.get("functions");
            parameters.put("tools", toolDefinitionList);
        }

        return builder.build();
    }

    @Override
    public String runRequest(CompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<CompletionResult> resultAtomicReference = new AtomicReference<>(new CompletionResult());
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));

        CompletionResult completionResult = service.createCompletion(request);
        resultAtomicReference.set(completionResult);
        String answer = completionResult.getOutput().getText();
        if (answer != null) {
            answerContent.get().collect(answer);
        }

        String responseContent = answerContent.get().joining();

        responseContent = LLMUtils.interceptAnswerWithStopsSplit(responseContent, stops);

        log.warn(getModel() + " answer:" + responseContent);

        LlmResultHolder.setResult(JsonUtils.obj2Map(resultAtomicReference.get()));

        return responseContent;
    }

    @Override
    public String runRequestStream(CompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<CompletionResult> resultAtomicReference = new AtomicReference<>(new CompletionResult());
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));

        service.streamCompletion(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    String answer = e.getOutput().getText();
                    log.warn(getModel() + " stream answer:" + answer);
                    if (answer != null) {
                        CompletionResult result = new CompletionResult();
                        result.setOutput(e.getOutput());
                        result.setRequestId(e.getRequestId());
                        result.setUsage(e.getUsage());
                        resultAtomicReference.set(result);
                        answerContent.get().collect(answer);
                        if (consumer != null) {
                            consumer.accept(answer);
                        }
                    }
                });

        String responseContent = answerContent.get().joining();
//        responseContent = responseContent.replace(prompt + "\n\n", "");

        responseContent = LLMUtils.interceptAnswerWithStopsSplit(responseContent, stops);

        log.warn(getModel() + " answer:" + responseContent);

        LlmResultHolder.setResult(JsonUtils.obj2Map(resultAtomicReference.get()));

        return responseContent;
    }

    @Override
    public String getStructuredChatAgentPrefixPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.PREFIX_CH : DashScopePromptConstants.PREFIX);
    }

    @Override
    public String getStructuredChatAgentInstructionsPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.FORMAT_INSTRUCTIONS_CH : DashScopePromptConstants.FORMAT_INSTRUCTIONS);
    }

    @Override
    public String getStructuredChatAgentSuffixPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.SUFFIX_CH : DashScopePromptConstants.SUFFIX);
    }

    @Override
    public String getToolDescriptionPrompt(BaseMemory memory, boolean isCH) {
        return DashScopePromptConstants.TOOL_DESC;
    }

    @Override
    public AgentOutputParser getStructuredChatOutputParser() {
        return new DashScopeStructuredChatOutputParser();
    }

    @Override
    public AgentOutputParser getAPIChainUrlOutputParser() {
        return new DashScopeAPIChainUrlOutputParser();
    }

    public void setToken(String token) {
        service.setToken(token);
    }

    @Override
    public String getTraceInfo() {
        JSONObject traceInfo = new JSONObject();
        traceInfo.put("model", getModel());
        traceInfo.put("maxLength", getMaxTokens());
        traceInfo.put("topP", getTopP());
        traceInfo.put("enableSearch", enableSearch);
        traceInfo.put("dataInspection", dataInspection);
        traceInfo.put("stream", isStream());
        return traceInfo.toJSONString();
    }

    @Override
    public String getLlmFamilyName() {
        return getModel();
    }
}

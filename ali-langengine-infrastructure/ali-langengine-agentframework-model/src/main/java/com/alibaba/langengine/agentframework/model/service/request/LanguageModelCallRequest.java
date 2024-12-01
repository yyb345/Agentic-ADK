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
package com.alibaba.langengine.agentframework.model.service.request;

import com.alibaba.langengine.agentframework.model.domain.ChatAttachment;
import com.alibaba.langengine.agentframework.model.domain.ChatMessage;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Data
public class LanguageModelCallRequest {

    private String agentCode;

    private String nodeId;

    private String query;

    private List<ChatAttachment> chatAttachments;

    private String prompt;

    private Double temperature;

    private String modelId;

    private Boolean traceToken;

    private String sessionId;

    private String userId;

    private String requestId;

    /**
     * builtin或者llmtemplate
     */
    private String llmType;

    private String llmTemplateCode;

    private List<ChatMessage> chatHistory;

    private Boolean hasHistory = true;

    private Object streamReference;

    private String responseFilter;
    
    private Boolean debug;

    private String env;

    private String projectName;

    private Boolean botChat;

    Consumer<Object> chunkConsumer;

    private Map<String, Object> request;

    private Map<String, Object> response;

    private Boolean apiKeyCall;

    private String apiKey;

    private Boolean async;

//    private Boolean batch;
//
//    private Boolean offline;

    private Map<String, Object> invokeContext;

    private String processInstanceId;
    /**
     * 开启节点的自定义异常配置
     */
    private Boolean enableExceptionConfig;
    /**
     * 自定义异常配置内容
     */
    private JSONObject exceptionConfig;
}

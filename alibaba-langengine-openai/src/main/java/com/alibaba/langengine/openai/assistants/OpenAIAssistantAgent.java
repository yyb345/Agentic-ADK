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
package com.alibaba.langengine.openai.assistants;

import com.alibaba.langengine.core.model.fastchat.ListSearchParameters;
import com.alibaba.langengine.core.model.fastchat.OpenAiResponse;
import com.alibaba.langengine.core.model.fastchat.assistants.*;
import com.alibaba.langengine.core.model.fastchat.file.File;
import com.alibaba.langengine.core.model.fastchat.messages.Message;
import com.alibaba.langengine.core.model.fastchat.messages.MessageFile;
import com.alibaba.langengine.core.model.fastchat.messages.MessageRequest;
import com.alibaba.langengine.core.model.fastchat.messages.ModifyMessageRequest;
import com.alibaba.langengine.core.model.fastchat.runs.*;
import com.alibaba.langengine.core.model.fastchat.service.DeleteResult;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.alibaba.langengine.core.model.fastchat.threads.Thread;
import com.alibaba.langengine.core.model.fastchat.threads.ThreadRequest;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Map;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * OpenAI Assistant Agent
 *
 * https://platform.openai.com/docs/assistants/overview
 * https://platform.openai.com/docs/api-reference/assistants/object
 *
 * @author xiaoxuan.lp
 */
@Data
public class OpenAIAssistantAgent {

    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    /**
     * 国内代理openai server url
     */
    private static final String DEFAULT_PROXY_BASE_URL = "https://api.openai-proxy.com/";

    public OpenAIAssistantAgent() {
        this(null);
    }

    public OpenAIAssistantAgent(String apiKey) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)), true, apiKey != null ? apiKey : token);
    }

    public Assistant createAssistant(AssistantRequest request) {
        return service.createAssistant(request);
    }

    public Assistant retrieveAssistant(String assistantId) {
        return service.retrieveAssistant(assistantId);
    }

    public Assistant modifyAssistant(String assistantId, ModifyAssistantRequest request) {
        return service.modifyAssistant(assistantId, request);
    }

    public DeleteResult deleteAssistant(String assistantId) {
        return service.deleteAssistant(assistantId);
    }

    public OpenAiResponse<Assistant> listAssistants(ListSearchParameters params) {
        return service.listAssistants(params);
    }

    public File uploadFile(String filepath) {
        return service.uploadFile("assistants", filepath);
    }

    public DeleteResult deleteFile(String fileId) {
        return service.deleteFile(fileId);
    }

    public AssistantFile createAssistantFile(String assistantId, AssistantFileRequest fileRequest) {
        return service.createAssistantFile(assistantId, fileRequest);
    }

    public AssistantFile retrieveAssistantFile(String assistantId, String fileId) {
        return service.retrieveAssistantFile(assistantId, fileId);
    }

    public DeleteResult deleteAssistantFile(String assistantId, String fileId) {
        return service.deleteAssistantFile(assistantId, fileId);
    }

    public OpenAiResponse<Assistant> listAssistantFiles(String assistantId, ListSearchParameters params) {
        return service.listAssistantFiles(assistantId, params);
    }

    public Thread createThread(ThreadRequest request) {
        return service.createThread(request);
    }

    public Thread retrieveThread(String threadId) {
        return service.retrieveThread(threadId);
    }

    public Thread modifyThread(String threadId, ThreadRequest request) {
        return service.modifyThread(threadId, request);
    }

    public DeleteResult deleteThread(String threadId) {
        return service.deleteThread(threadId);
    }

    public Message createMessage(String threadId, MessageRequest request) {
        return service.createMessage(threadId, request);
    }

    public Message retrieveMessage(String threadId, String messageId) {
        return service.retrieveMessage(threadId, messageId);
    }

    public Message modifyMessage(String threadId, String messageId, ModifyMessageRequest request) {
        return service.modifyMessage(threadId, messageId, request);
    }

    public OpenAiResponse<Message> listMessages(String threadId) {
        return service.listMessages(threadId);
    }

    public OpenAiResponse<Message> listMessages(String threadId, ListSearchParameters params) {
        return service.listMessages(threadId, params);
    }

    public MessageFile retrieveMessageFile(String threadId, String messageId, String fileId) {
        return service.retrieveMessageFile(threadId, messageId, fileId);
    }

    public OpenAiResponse<MessageFile> listMessageFiles(String threadId, String messageId) {
        return service.listMessageFiles(threadId, messageId);
    }

    public OpenAiResponse<MessageFile> listMessageFiles(String threadId, String messageId, ListSearchParameters params) {
        return service.listMessageFiles(threadId, messageId, params);
    }

    public Run createRun(String threadId, RunCreateRequest runCreateRequest) {
        return service.createRun(threadId, runCreateRequest);
    }

    public Run retrieveRun(String threadId, String runId) {
        return service.retrieveRun(threadId, runId);
    }

    public Run modifyRun(String threadId, String runId, Map<String, String> metadata) {
        return service.modifyRun(threadId, runId, metadata);
    }

    public OpenAiResponse<Run> listRuns(String threadId, ListSearchParameters listSearchParameters) {
        return service.listRuns(threadId, listSearchParameters);
    }

    public Run submitToolOutputs(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        return service.submitToolOutputs(threadId, runId, submitToolOutputsRequest);
    }

    public Run cancelRun(String threadId, String runId) {
        return service.cancelRun(threadId, runId);
    }

    public Run createThreadAndRun(CreateThreadAndRunRequest createThreadAndRunRequest) {
        return service.createThreadAndRun(createThreadAndRunRequest);
    }

    public RunStep retrieveRunStep(String threadId, String runId, String stepId) {
        return service.retrieveRunStep(threadId, runId, stepId);
    }

    public OpenAiResponse<RunStep> listRunSteps(String threadId, String runId, ListSearchParameters listSearchParameters) {
        return service.listRunSteps(threadId, runId, listSearchParameters);
    }
}

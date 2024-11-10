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
package com.alibaba.langengine.core.model.fastchat.service;

import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.model.fastchat.ListSearchParameters;
import com.alibaba.langengine.core.model.fastchat.OpenAiResponse;
import com.alibaba.langengine.core.model.fastchat.audio.Text2SpeechRequest;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionChunk;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionResult;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionChunk;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionResult;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingRequest;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingResult;
import com.alibaba.langengine.core.model.fastchat.file.File;
import com.alibaba.langengine.core.model.fastchat.finetune.FineTuneEvent;
import com.alibaba.langengine.core.model.fastchat.finetune.FineTuneRequest;
import com.alibaba.langengine.core.model.fastchat.finetune.FineTuneResult;
import com.alibaba.langengine.core.model.fastchat.messages.Message;
import com.alibaba.langengine.core.model.fastchat.messages.MessageFile;
import com.alibaba.langengine.core.model.fastchat.messages.MessageRequest;
import com.alibaba.langengine.core.model.fastchat.messages.ModifyMessageRequest;
import com.alibaba.langengine.core.model.fastchat.moderation.ModerationRequest;
import com.alibaba.langengine.core.model.fastchat.moderation.ModerationResult;
import com.alibaba.langengine.core.model.fastchat.threads.Thread;
import com.alibaba.langengine.core.model.fastchat.threads.ThreadRequest;
import com.alibaba.langengine.core.model.fastchat.assistants.Assistant;
import com.alibaba.langengine.core.model.fastchat.assistants.AssistantFile;
import com.alibaba.langengine.core.model.fastchat.assistants.AssistantFileRequest;
import com.alibaba.langengine.core.model.fastchat.assistants.AssistantRequest;
import com.alibaba.langengine.core.model.fastchat.assistants.ModifyAssistantRequest;
import com.alibaba.langengine.core.model.fastchat.image.CreateImageEditRequest;
import com.alibaba.langengine.core.model.fastchat.image.CreateImageRequest;
import com.alibaba.langengine.core.model.fastchat.image.CreateImageVariationRequest;
import com.alibaba.langengine.core.model.fastchat.image.ImageResult;
import com.alibaba.langengine.core.model.fastchat.model.Model;
import com.alibaba.langengine.core.model.fastchat.runs.CreateThreadAndRunRequest;
import com.alibaba.langengine.core.model.fastchat.runs.Run;
import com.alibaba.langengine.core.model.fastchat.runs.RunCreateRequest;
import com.alibaba.langengine.core.model.fastchat.runs.RunStep;
import com.alibaba.langengine.core.model.fastchat.runs.SubmitToolOutputsRequest;
import com.alibaba.langengine.core.util.JacksonUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * FastChat服务
 *
 * @author xiaoxuan.lp
 */
@Data
public class FastChatService extends RetrofitInitService<FastChatApi> {

    public FastChatService() {
        super();
    }

    public FastChatService(Duration timeout) {
        super(timeout);
    }

    public FastChatService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    public FastChatService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<FastChatApi> getServiceApiClass() {
        return FastChatApi.class;
    }

    public List<Model> listModels() {
        return execute(getApi().listModels()).data;
    }

    public Model getModel(String modelId) {
        return execute(getApi().getModel(modelId));
    }

    public CompletionResult createCompletion(CompletionRequest request) {
        try {
            callerClass.set(getClass());
            CompletionResult result = execute(getApi().createCompletion(request));
            return result;
        } finally {
            callerClass.remove();
        }

    }

    public Flowable<CompletionChunk> streamCompletion(CompletionRequest request) {
        try {
            callerClass.set(getClass());
            request.setStream(true);
            Flowable<CompletionChunk> stream = stream(getApi().createCompletionStream(request), CompletionChunk.class);
            return stream;
        } finally {
            callerClass.remove();
        }
    }

    public ChatCompletionResult createChatCompletion(ChatCompletionRequest request) {
        try {
            callerClass.set(getClass());
            ChatCompletionResult execute = execute(getApi().createChatCompletion(request));
            return execute;
        } finally {
            callerClass.remove();
        }
    }

    public Flowable<ChatCompletionChunk> streamChatCompletion(ChatCompletionRequest request) {
        try {
            callerClass.set(getClass());
            request.setStream(true);
            Flowable<ChatCompletionChunk> stream = stream(getApi().createChatCompletionStream(request),
                ChatCompletionChunk.class);
            return stream;

        } finally {
            callerClass.remove();
        }

    }

    public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
        try {
            callerClass.set(getClass());
            EmbeddingResult execute = execute(getApi().createEmbeddings(request));
            return execute;
        } finally {
            callerClass.remove();
        }
    }

    public List<File> listFiles() {
        return execute(getApi().listFiles()).data;
    }

    public File uploadFile(String purpose, String filepath) {
        java.io.File file = new java.io.File(filepath);
        RequestBody purposeBody = RequestBody.create(okhttp3.MultipartBody.FORM, purpose);
        RequestBody fileBody = RequestBody.create(MediaType.parse("text"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filepath, fileBody);

        return execute(getApi().uploadFile(purposeBody, body));
    }

    public DeleteResult deleteFile(String fileId) {
        return execute(getApi().deleteFile(fileId));
    }

    public File retrieveFile(String fileId) {
        return execute(getApi().retrieveFile(fileId));
    }

    public FineTuneResult createFineTune(FineTuneRequest request) {
        return execute(getApi().createFineTune(request));
    }

    public CompletionResult createFineTuneCompletion(CompletionRequest request) {
        return execute(getApi().createFineTuneCompletion(request));
    }

    public List<FineTuneResult> listFineTunes() {
        return execute(getApi().listFineTunes()).data;
    }

    public FineTuneResult retrieveFineTune(String fineTuneId) {
        return execute(getApi().retrieveFineTune(fineTuneId));
    }

    public FineTuneResult cancelFineTune(String fineTuneId) {
        return execute(getApi().cancelFineTune(fineTuneId));
    }

    public List<FineTuneEvent> listFineTuneEvents(String fineTuneId) {
        return execute(getApi().listFineTuneEvents(fineTuneId)).data;
    }

    public DeleteResult deleteFineTune(String fineTuneId) {
        return execute(getApi().deleteFineTune(fineTuneId));
    }

    public ImageResult createImage(CreateImageRequest request) {
        return execute(getApi().createImage(request));
    }

    public ResponseBody createAudio(Text2SpeechRequest request) {
        ResponseBody execute = execute(getApi().createAudioSpeech(request));
        return execute;
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, String imagePath, String maskPath) {
        java.io.File image = new java.io.File(imagePath);
        java.io.File mask = null;
        if (maskPath != null) {
            mask = new java.io.File(maskPath);
        }
        return createImageEdit(request, image, mask);
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, java.io.File image, java.io.File mask) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
            .setType(MediaType.get("multipart/form-data"))
            .addFormDataPart("prompt", request.getPrompt())
            .addFormDataPart("size", request.getSize())
            .addFormDataPart("response_format", request.getResponseFormat())
            .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        if (mask != null) {
            RequestBody maskBody = RequestBody.create(MediaType.parse("image"), mask);
            builder.addFormDataPart("mask", "mask", maskBody);
        }

        return execute(getApi().createImageEdit(builder.build()));
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, String imagePath) {
        java.io.File image = new java.io.File(imagePath);
        return createImageVariation(request, image);
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, java.io.File image) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
            .setType(MediaType.get("multipart/form-data"))
            .addFormDataPart("size", request.getSize())
            .addFormDataPart("response_format", request.getResponseFormat())
            .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        return execute(getApi().createImageVariation(builder.build()));
    }

    public ModerationResult createModeration(ModerationRequest request) {
        return execute(getApi().createModeration(request));
    }

    public Assistant createAssistant(AssistantRequest request) {
        return execute(getApi().createAssistant(request));
    }

    public Assistant retrieveAssistant(String assistantId) {
        return execute(getApi().retrieveAssistant(assistantId));
    }

    public Assistant modifyAssistant(String assistantId, ModifyAssistantRequest request) {
        return execute(getApi().modifyAssistant(assistantId, request));
    }

    public DeleteResult deleteAssistant(String assistantId) {
        return execute(getApi().deleteAssistant(assistantId));
    }

    public OpenAiResponse<Assistant> listAssistants(ListSearchParameters params) {
        Map<String, Object> queryParameters = JacksonUtils.getServiceMapper(FastChatService.class).convertValue(params,
            new TypeReference<Map<String, Object>>() {
            });
        return execute(getApi().listAssistants(queryParameters));
    }

    public AssistantFile createAssistantFile(String assistantId, AssistantFileRequest fileRequest) {
        return execute(getApi().createAssistantFile(assistantId, fileRequest));
    }

    public AssistantFile retrieveAssistantFile(String assistantId, String fileId) {
        return execute(getApi().retrieveAssistantFile(assistantId, fileId));
    }

    public DeleteResult deleteAssistantFile(String assistantId, String fileId) {
        return execute(getApi().deleteAssistantFile(assistantId, fileId));
    }

    public OpenAiResponse<Assistant> listAssistantFiles(String assistantId, ListSearchParameters params) {
        Map<String, Object> queryParameters = JacksonUtils.getServiceMapper(FastChatService.class).convertValue(params,
            new TypeReference<Map<String, Object>>() {});
        return execute(getApi().listAssistantFiles(assistantId, queryParameters));
    }

    public Thread createThread(ThreadRequest request) {
        return execute(getApi().createThread(request));
    }

    public Thread retrieveThread(String threadId) {
        return execute(getApi().retrieveThread(threadId));
    }

    public Thread modifyThread(String threadId, ThreadRequest request) {
        return execute(getApi().modifyThread(threadId, request));
    }

    public DeleteResult deleteThread(String threadId) {
        return execute(getApi().deleteThread(threadId));
    }

    public Message createMessage(String threadId, MessageRequest request) {
        return execute(getApi().createMessage(threadId, request));
    }

    public Message retrieveMessage(String threadId, String messageId) {
        return execute(getApi().retrieveMessage(threadId, messageId));
    }

    public Message modifyMessage(String threadId, String messageId, ModifyMessageRequest request) {
        return execute(getApi().modifyMessage(threadId, messageId, request));
    }

    public OpenAiResponse<Message> listMessages(String threadId) {
        return execute(getApi().listMessages(threadId));
    }

    public OpenAiResponse<Message> listMessages(String threadId, ListSearchParameters params) {
        Map<String, Object> queryParameters = JacksonUtils.getServiceMapper(FastChatService.class).convertValue(params,
            new TypeReference<Map<String, Object>>() {
            });
        return execute(getApi().listMessages(threadId, queryParameters));
    }

    public MessageFile retrieveMessageFile(String threadId, String messageId, String fileId) {
        return execute(getApi().retrieveMessageFile(threadId, messageId, fileId));
    }

    public OpenAiResponse<MessageFile> listMessageFiles(String threadId, String messageId) {
        return execute(getApi().listMessageFiles(threadId, messageId));
    }

    public OpenAiResponse<MessageFile> listMessageFiles(String threadId, String messageId,
        ListSearchParameters params) {
        Map<String, Object> queryParameters = JacksonUtils.getServiceMapper(FastChatService.class).convertValue(params,
            new TypeReference<Map<String, Object>>() {
            });
        return execute(getApi().listMessageFiles(threadId, messageId, queryParameters));
    }

    public Run createRun(String threadId, RunCreateRequest runCreateRequest) {
        return execute(getApi().createRun(threadId, runCreateRequest));
    }

    public Run retrieveRun(String threadId, String runId) {
        return execute(getApi().retrieveRun(threadId, runId));
    }

    public Run modifyRun(String threadId, String runId, Map<String, String> metadata) {
        return execute(getApi().modifyRun(threadId, runId, metadata));
    }

    public OpenAiResponse<Run> listRuns(String threadId, ListSearchParameters listSearchParameters) {
        Map<String, String> search = new HashMap<>();
        if (listSearchParameters != null) {
            ObjectMapper mapper = JacksonUtils.defaultObjectMapper();
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(getApi().listRuns(threadId, search));
    }

    public Run submitToolOutputs(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        return execute(getApi().submitToolOutputs(threadId, runId, submitToolOutputsRequest));
    }

    public Run cancelRun(String threadId, String runId) {
        return execute(getApi().cancelRun(threadId, runId));
    }

    public Run createThreadAndRun(CreateThreadAndRunRequest createThreadAndRunRequest) {
        return execute(getApi().createThreadAndRun(createThreadAndRunRequest));
    }

    public RunStep retrieveRunStep(String threadId, String runId, String stepId) {
        return execute(getApi().retrieveRunStep(threadId, runId, stepId));
    }

    public OpenAiResponse<RunStep> listRunSteps(String threadId, String runId,
        ListSearchParameters listSearchParameters) {
        Map<String, String> search = new HashMap<>();
        if (listSearchParameters != null) {
            ObjectMapper mapper = JacksonUtils.defaultObjectMapper();
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(getApi().listRunSteps(threadId, runId, search));
    }
}

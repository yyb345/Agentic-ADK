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

import com.alibaba.fastjson.JSON;
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
import com.alibaba.langengine.core.model.fastchat.threads.Thread;
import com.alibaba.langengine.core.model.fastchat.threads.ThreadRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenAIAssistantAgentTest {

    @Test
    public void test_createAssistant() {
        createAssistant();
    }

    @Test
    public void test_retrieveAssistant() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        Assistant assistant =  agent.retrieveAssistant("asst_c7R4DD3sXOu4VYY4O8kVeWc6");
        System.out.println("assistant:" + JSON.toJSONString(assistant));
    }

    @Test
    public void test_modifyAssistant() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ModifyAssistantRequest request = new ModifyAssistantRequest();
        request.setName("Math Tutor 2");
        Assistant assistant = agent.modifyAssistant("asst_c7R4DD3sXOu4VYY4O8kVeWc6", request);
        System.out.println("assistant:" + JSON.toJSONString(assistant));
    }

    @Test
    public void test_deleteAssistant() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        DeleteResult deleteResult = agent.deleteAssistant("asst_c7R4DD3sXOu4VYY4O8kVeWc6");
        System.out.println("result:" + JSON.toJSONString(deleteResult));
    }

    @Test
    public void test_listAssistants() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ListSearchParameters.ListSearchParametersBuilder builder = ListSearchParameters.builder()
                .limit(10);
        ListSearchParameters params = builder.build();
        OpenAiResponse<Assistant> openAiResponse =  agent.listAssistants(params);
        System.out.println("result:" + JSON.toJSONString(openAiResponse));
    }

    @Test
    public void test_uploadFile() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        File file = agent.uploadFile("/Users/xiaoxuan.lp/Documents/sft_data/train_demo.json");
        System.out.println("result:" + JSON.toJSONString(file));
    }

    @Test
    public void deleteFile() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        DeleteResult result = agent.deleteFile("file-zbwoAgjBfQtunYb7YuQuWVwO");
        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void test_createAssistantFile() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        AssistantFileRequest fileRequest = new AssistantFileRequest();
        fileRequest.setFileId("file-zbwoAgjBfQtunYb7YuQuWVwO");
        AssistantFile assistantFile = agent.createAssistantFile("asst_2n2rqIzZpdKZ1i0secVwZ2BG", fileRequest);
        System.out.println("result:" + JSON.toJSONString(assistantFile));
    }

    @Test
    public void test_retrieveAssistantFile() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        AssistantFile assistantFile = agent.retrieveAssistantFile("asst_2n2rqIzZpdKZ1i0secVwZ2BG", "file-zbwoAgjBfQtunYb7YuQuWVwO");
        System.out.println("result:" + JSON.toJSONString(assistantFile));
    }

    @Test
    public void test_deleteAssistantFile() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        DeleteResult deleteResult = agent.deleteAssistantFile("asst_2n2rqIzZpdKZ1i0secVwZ2BG", "file-zbwoAgjBfQtunYb7YuQuWVwO");
        System.out.println("result:" + JSON.toJSONString(deleteResult));
    }

    @Test
    public void test_listAssistantFiles() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ListSearchParameters.ListSearchParametersBuilder builder = ListSearchParameters.builder()
                .limit(10);
        ListSearchParameters params = builder.build();
        OpenAiResponse<Assistant> openAiResponse = agent.listAssistantFiles("asst_2n2rqIzZpdKZ1i0secVwZ2BG", params);
        System.out.println("result:" + JSON.toJSONString(openAiResponse));
    }

    @Test
    public void test_createThread() {
        createThread();
    }

    @Test
    public void test_retrieveThread() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        Thread thread = agent.retrieveThread("thread_xU6YjMatWP0Js1ztr05Sa6Kb");
        System.out.println("result:" + JSON.toJSONString(thread));
    }

    @Test
    public void test_modifyThread() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ThreadRequest request = new ThreadRequest();
        Thread thread = agent.modifyThread("thread_xU6YjMatWP0Js1ztr05Sa6Kb", request);
        System.out.println("result:" + JSON.toJSONString(thread));
    }

    @Test
    public void test_deleteThread() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        DeleteResult result = agent.deleteThread("thread_xU6YjMatWP0Js1ztr05Sa6Kb");
        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void test_createMessage() {
        createMessage();
    }

    @Test
    public void test_retrieveMessage() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        Message message = agent.retrieveMessage("thread_5H3UvlQtWL1Sq3fRuqYzvZcx", "msg_QSGkYHYiM2rgg3GFhi3n4POn");
        System.out.println("result:" + JSON.toJSONString(message));
    }

    @Test
    public void test_modifyMessage() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ModifyMessageRequest request = new ModifyMessageRequest();
        request.setMetadata(new HashMap<String, String>() {{
            put("modified", "true");
            put("user", "abc123");
        }});
        Message message = agent.modifyMessage("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "msg_C4SkcwGcPvD8RjtoYKKCGjVV", request);
        System.out.println("result:" + JSON.toJSONString(message));
    }

    @Test
    public void test_listMessages() {
        listMessages();
    }

    @Test
    public void test_retrieveMessageFile() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        MessageFile messageFile = agent.retrieveMessageFile("thread_RtIUOIhndfO3eMk6wM3a5ftJ",
                "msg_nnuSVVGQwvHN5d8tYCfdqJkq",
                "file-EtKYkd10natcFNhmiP6M7IGo");
        System.out.println("result:" + JSON.toJSONString(messageFile));
    }

    @Test
    public void test_listMessageFiles() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        OpenAiResponse<MessageFile> openAiResponse = agent.listMessageFiles("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "msg_nnuSVVGQwvHN5d8tYCfdqJkq");
        System.out.println("result:" + JSON.toJSONString(openAiResponse));
    }

    @Test
    public void test_createRun() {
        createRun();
    }

    @Test
    public void test_retrieveRun() {
        retrieveRun();
    }

    @Test
    public void test_modifyRun() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        Run run = agent.modifyRun("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "run_eMJjP0aaYsT891SaZa6JI1T4", new HashMap<>());
        System.out.println("result:" + JSON.toJSONString(run));
    }

    @Test
    public void test_listRuns() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ListSearchParameters.ListSearchParametersBuilder builder = ListSearchParameters.builder()
                .limit(10);
        ListSearchParameters params = builder.build();
        OpenAiResponse<Run> openAiResponse = agent.listRuns("thread_RtIUOIhndfO3eMk6wM3a5ftJ", params);
        System.out.println("result:" + JSON.toJSONString(openAiResponse));
    }

    @Test
    public void test_submitToolOutputs() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        SubmitToolOutputsRequest submitToolOutputsRequest = new SubmitToolOutputsRequest();
        List<SubmitToolOutputRequestItem> toolOutputs = new ArrayList<>();
        SubmitToolOutputRequestItem toolOutput = new SubmitToolOutputRequestItem();
        toolOutput.setToolCallId("call_abc123");
        toolOutput.setOutput("28C");
        toolOutputs.add(toolOutput);
        submitToolOutputsRequest.setToolOutputs(toolOutputs);
        Run run = agent.submitToolOutputs("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "run_eMJjP0aaYsT891SaZa6JI1T4", submitToolOutputsRequest);
        System.out.println("result:" + JSON.toJSONString(run));
    }

    @Test
    public void test_cancelRun() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        Run run = agent.cancelRun("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "run_eMJjP0aaYsT891SaZa6JI1T4");
        System.out.println("result:" + JSON.toJSONString(run));
    }

    @Test
    public void test_createThreadAndRun() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        CreateThreadAndRunRequest createThreadAndRunRequest = new CreateThreadAndRunRequest();
        createThreadAndRunRequest.setAssistantId("asst_6yXvwi46BUud705y7SS18v9Q");
        Run run = agent.createThreadAndRun(createThreadAndRunRequest);
        System.out.println("result:" + JSON.toJSONString(run));
    }

    @Test
    public void test_retrieveRunStep() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        RunStep runStep = agent.retrieveRunStep("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "run_eMJjP0aaYsT891SaZa6JI1T4", "");
        System.out.println("result:" + JSON.toJSONString(runStep));
    }

    @Test
    public void test_listRunSteps() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ListSearchParameters.ListSearchParametersBuilder builder = ListSearchParameters.builder()
                .limit(10);
        ListSearchParameters params = builder.build();
        OpenAiResponse<RunStep> openAiResponse = agent.listRunSteps("thread_RtIUOIhndfO3eMk6wM3a5ftJ", "run_eMJjP0aaYsT891SaZa6JI1T4", params);
        System.out.println("result:" + JSON.toJSONString(openAiResponse));
    }

    @Test
    public void test_execute() {
        Assistant assistant = createAssistant();
        String assistantId = assistant.getId();

        Thread thread = createThread();
        String threadId = thread.getId();

        Message message = createMessage(threadId);
//        String messageId = message.getId();

        Run run = createRun(assistantId, threadId);
        String runId = run.getId();

        while (!"completed".equals(run.getStatus())) {
            run = retrieveRun(threadId, runId);
            System.out.println("curnrent run status:" + run.getStatus());
        }

        listMessages(threadId);
    }

    private Assistant createAssistant() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        AssistantRequest request = new AssistantRequest();
        request.setModel("gpt-4");
        request.setName("Math Tutor");
        request.setInstructions("You are a personal math tutor. When asked a question, write and run Python code to answer the question.");

        List<Tool> tools = new ArrayList<>();
        Tool tool = new Tool();
        tool.setType(AssistantToolsEnum.CODE_INTERPRETER);
        tools.add(tool);
        request.setTools(tools);
        Assistant assistant = agent.createAssistant(request);
        System.out.println("createAssistant result:" + JSON.toJSONString(assistant));
        return assistant;
    }

    private Thread createThread() {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ThreadRequest request = new ThreadRequest();
        Thread thread = agent.createThread(request);
        System.out.println("createThread result:" + JSON.toJSONString(thread));
        return thread;
    }

    private Message createMessage() {
        return createMessage(null);
    }

    private Message createMessage(String threadId) {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        MessageRequest request = new MessageRequest();
        request.setRole("user");
        request.setContent("I need to solve the equation `3x + 11 = 14`. Can you help me?");
//        request.setContent("[\n" +
//                "    {\n" +
//                "      \"type\": \"text\",\n" +
//                "      \"text\": {\n" +
//                "        \"value\": \"How does AI work? Explain it in simple terms.\",\n" +
//                "        \"annotations\": []\n" +
//                "      }\n" +
//                "    }\n" +
//                "  ]");
//        request.setFileIds(Arrays.asList(new String[] { "file-EtKYkd10natcFNhmiP6M7IGo" }));
        Message message = agent.createMessage(threadId != null ? threadId : "thread_5H3UvlQtWL1Sq3fRuqYzvZcx", request);
        System.out.println("createMessage result:" + JSON.toJSONString(message));
        return message;
    }

    private Run createRun() {
        return createRun(null, null);
    }
    private Run createRun(String assistantId, String threadId) {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        RunCreateRequest runCreateRequest = new RunCreateRequest();
        runCreateRequest.setAssistantId(assistantId != null ? assistantId : "asst_rner8xlt6OeFKl50R6wB7zzQ");
        Run run = agent.createRun(threadId != null ? threadId : "thread_5H3UvlQtWL1Sq3fRuqYzvZcx", runCreateRequest);
        System.out.println("createRun result:" + JSON.toJSONString(run));
        return run;
    }

    private Run retrieveRun() {
        return retrieveRun(null, null);
    }

    private Run retrieveRun(String threadId, String runId) {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        Run run = agent.retrieveRun(threadId != null ? threadId : "thread_5H3UvlQtWL1Sq3fRuqYzvZcx", runId != null ? runId : "run_Ji4w9Ly5w7JtNn8hFFcTQKlG");
        System.out.println("retrieveRun result:" + JSON.toJSONString(run));
        return run;
    }

    private OpenAiResponse<Message> listMessages() {
        return listMessages(null);
    }

    private OpenAiResponse<Message> listMessages(String threadId) {
        OpenAIAssistantAgent agent = new OpenAIAssistantAgent();
        ListSearchParameters.ListSearchParametersBuilder builder = ListSearchParameters.builder()
                .limit(10);
        ListSearchParameters params = builder.build();
        OpenAiResponse<Message> openAiResponse = agent.listMessages(threadId != null ? threadId : "thread_5H3UvlQtWL1Sq3fRuqYzvZcx", params);
        System.out.println("listMessages result:" + JSON.toJSONString(openAiResponse));
        return openAiResponse;
    }
}

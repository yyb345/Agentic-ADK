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

import com.alibaba.langengine.core.model.fastchat.OpenAiResponse;
import com.alibaba.langengine.core.model.fastchat.assistants.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatFunction;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatFunctionCall;
import com.alibaba.langengine.core.model.fastchat.messages.Message;
import com.alibaba.langengine.core.model.fastchat.messages.MessageRequest;
import com.alibaba.langengine.core.model.fastchat.runs.*;
import com.alibaba.langengine.core.model.fastchat.service.ChatCompletionRequestMixIn;
import com.alibaba.langengine.core.model.fastchat.service.ChatFunctionCallMixIn;
import com.alibaba.langengine.core.model.fastchat.service.ChatFunctionMixIn;
import com.alibaba.langengine.core.model.fastchat.threads.Thread;
import com.alibaba.langengine.core.model.fastchat.threads.ThreadRequest;
import com.alibaba.langengine.core.tokenizers.TikTokensUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AssistantFunctionTest {

    @Test
    public void createRetrieveRun() throws JsonProcessingException {
        OpenAIAssistantAgent service = new OpenAIAssistantAgent();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.addMixIn(ChatFunction.class, ChatFunctionMixIn.class);
        mapper.addMixIn(ChatCompletionRequest.class, ChatCompletionRequestMixIn.class);
        mapper.addMixIn(ChatFunctionCall.class, ChatFunctionCallMixIn.class);
        
        String funcDef = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"location\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The city and state, e.g. San Francisco, CA\"\n" +
                "    },\n" +
                "    \"unit\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"celsius\", \"fahrenheit\"]\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"location\"]\n" +
                "}";
        Map<String, Object> funcParameters = mapper.readValue(funcDef, new TypeReference<Map<String, Object>>() {});
        AssistantFunction function = AssistantFunction.builder()
                .name("weather_reporter")
                .description("Get the current weather of a location")
                .parameters(funcParameters)
                .build();

        List<Tool> toolList = new ArrayList<>();
        Tool funcTool = new Tool(AssistantToolsEnum.FUNCTION, function);
        toolList.add(funcTool);
        
        
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model(TikTokensUtil.ModelEnum.GPT_4_1106_preview.getName())
                .name("MATH_TUTOR")
                .instructions("You are a personal Math Tutor.")
                .tools(toolList)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);

        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);

        MessageRequest messageRequest = MessageRequest.builder()
                .content("What's the weather of Xiamen?")
                .build();

        Message message = service.createMessage(thread.getId(), messageRequest);

        RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                .assistantId(assistant.getId())
                .build();

        Run run = service.createRun(thread.getId(), runCreateRequest);
        assertNotNull(run);

        Run retrievedRun = service.retrieveRun(thread.getId(), run.getId());
        while (!(retrievedRun.getStatus().equals("completed")) 
                && !(retrievedRun.getStatus().equals("failed")) 
                && !(retrievedRun.getStatus().equals("requires_action"))){
            retrievedRun = service.retrieveRun(thread.getId(), run.getId());
        }
        if (retrievedRun.getStatus().equals("requires_action")) {
            RequiredAction requiredAction = retrievedRun.getRequiredAction();
            System.out.println("requiredAction");
            System.out.println(mapper.writeValueAsString(requiredAction));
            List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
            ToolCall toolCall = toolCalls.get(0);
            String toolCallId = toolCall.getId();

            SubmitToolOutputRequestItem toolOutputRequestItem = SubmitToolOutputRequestItem.builder()
                    .toolCallId(toolCallId)
                    .output("sunny")
                    .build();
            List<SubmitToolOutputRequestItem> toolOutputRequestItems = new ArrayList<>();
            toolOutputRequestItems.add(toolOutputRequestItem);
            SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.builder()
                    .toolOutputs(toolOutputRequestItems)
                    .build();
            retrievedRun = service.submitToolOutputs(retrievedRun.getThreadId(), retrievedRun.getId(), submitToolOutputsRequest);

            while (!(retrievedRun.getStatus().equals("completed"))
                    && !(retrievedRun.getStatus().equals("failed"))
                    && !(retrievedRun.getStatus().equals("requires_action"))){
                retrievedRun = service.retrieveRun(thread.getId(), run.getId());
            }

            OpenAiResponse<Message> response = service.listMessages(thread.getId());

            List<Message> messages = response.getData();

            System.out.println(mapper.writeValueAsString(messages));
            
        }
    }
}

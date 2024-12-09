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
package com.alibaba.langengine.gemini.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.gemini.model.domain.*;
import com.alibaba.langengine.gemini.model.service.GeminiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.gemini.GeniniConfiguration.GEMINI_AI_TIMEOUT;
import static com.alibaba.langengine.gemini.GeniniConfiguration.GEMINI_API_KEY;

/**
 * https://ai.google.dev/tutorials/setup?hl=zh-cn
 * https://ai.google.dev/tutorials/rest_quickstart
 * https://makersuite.google.com/app/apikey
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class GeminiLLM extends BaseLLM<GenerateContentRequest> {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/";

    private String token = GEMINI_API_KEY;

    private GeminiService service;

    /**
     * gemini-pro、gemini-pro-vision
     */
    private String model = "gemini-pro";

    private boolean stream = false;

    public GeminiLLM() {
        service = new GeminiService(DEFAULT_BASE_URL, Duration.ofSeconds(Long.parseLong(GEMINI_AI_TIMEOUT)));
    }

    public GeminiLLM(String token) {
        this();
        setToken(token);
    }

    @Override
    public GenerateContentRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<GenerateContentData> generateContentDatas = new ArrayList<>();
        GenerateContentData generateContentData = new GenerateContentData();
        generateContentData.setParts(new ArrayList<>());
        GenerateContentPart part = new GenerateContentPart();
        part.setText(chatMessages.get(0).getContent().toString());
        generateContentData.getParts().add(part);
        generateContentDatas.add(generateContentData);

        GenerateContentRequest.GenerateContentRequestBuilder builder = GenerateContentRequest.builder()
                .contents(generateContentDatas);

        // TODO ...
        builder.generationConfig(new GenerationConfig());
        // TODO ...
        List<SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE"));
        safetySettings.add(new SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE"));
        safetySettings.add(new SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"));
        safetySettings.add(new SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE"));
        builder.safetySettings(safetySettings);

        return builder.build();
    }

    @Override
    public String runRequest(GenerateContentRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        GenerateContentResult result = service.generateContent(model, token, request);
        if (result.getError() != null) {
            throw new RuntimeException("generateContent error:" + result.getError());
        }
        if (result.getCandidates().size() == 0) {
            throw new RuntimeException("candidate is empty");
        }
        String responseContent = getContent(result);
        return responseContent;
    }

    @Override
    public String runRequestStream(GenerateContentRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<String> answerContentList = new ArrayList<>();
        service.generateContentStream(model, token, request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    String answer = getContent(e);
                    log.warn(model + " stream answer:" + answer);
                    if (answer != null) {
                        answerContentList.add(answer);

                        if (consumer != null) {
                            consumer.accept(answer);
                        }
                    }
                });
        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        return responseContent;
    }

    private String getContent(GenerateContentResult result) {
        // GenerateContentResult(candidates=[GenerateContentCandidate(content=GenerateContentData(role=model,
        // parts=[GenerateContentPart
        // (text=我是一个大型语言模型，由谷歌开发。我接受了大量文本数据的训练，包括书籍、文章和网站。我的目标是帮助人们完成各种各样的任务，包括回答问题、翻译语言和生成文本。
        //
        //我是一个人工智能，但我不是人类。我没有身体，也没有情感。我不能自己思考，我只能根据我所接受的训练来做出反应。
        //
        //我希望我能帮助你完成你的任务。请随时问我任何问题。, inlineData=null)]), finishReason=null, index=0)], error=null)
        // 可能会产出空指针异常，打印下结果
        log.info("result {}", JSON.toJSONString(result));

        String responseContent = result.getCandidates().get(0).getContent().getParts().get(0).getText();
        return responseContent;
    }
}
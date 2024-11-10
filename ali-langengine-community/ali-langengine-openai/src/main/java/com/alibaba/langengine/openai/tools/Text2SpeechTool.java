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
package com.alibaba.langengine.openai.tools;

import com.alibaba.langengine.core.model.fastchat.audio.Text2SpeechRequest;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Function;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * @author aihe.ah
 * @time 2023/11/14
 * 功能说明：
 */
@Slf4j
@Data
public class Text2SpeechTool extends DefaultTool {

    /**
     * 使用那个模型进行语音生成
     * 包括：tts-1、tts-1-hd
     */
    private String model = "tts-1";

    /**
     * 默认的语音
     */
    private String voice = "alloy";

    /**
     * 接收语音的输入流，可以保存到某些地方
     */
    private Function<byte[], Object> function;

    @JsonIgnore
    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    private void init() {
        setName("Text2SpeechTool");
        setDescription(
            "This is a tool that can turn text into lifelike spoken audio. The input should be a text you want to "
                + "generate.");
    }

    public Text2SpeechTool() {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)), true, token);
        init();
    }

    public Text2SpeechTool(String apiKey, Integer timeout) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(timeout), true, apiKey);
        init();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        ResponseBody responseBody = null;
        try {

            Text2SpeechRequest text2SpeechRequest = Text2SpeechRequest.builder().voice(voice).model(model).input(
                toolInput).build();

            responseBody = service.createAudio(text2SpeechRequest);
            byte[] bytes = responseBody.bytes();
            if (function == null) {
                Path path = Paths.get("tmp.mp3");
                Files.write(path, bytes);
                return new ToolExecuteResult(path.toAbsolutePath().toString(), true);
            } else {
                Object apply = function.apply(bytes);
                return new ToolExecuteResult(String.valueOf(apply), true);
            }

        } catch (Exception ex) {
            log.error("error", ex);
            return new ToolExecuteResult("Answer: 当前系统有异常，请稍后再试." + System.currentTimeMillis(), true);
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

}

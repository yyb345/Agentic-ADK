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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.fastchat.moderation.ModerationRequest;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * 给定输入文本，如果模型将其分类为违反 OpenAI 的内容策略，则输出
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ModerationTool extends DefaultTool {

    /**
     * 模型名称
     * 包括：text-moderation-latest、text-moderation-stable
     */
    private String model = "text-moderation-latest";

    @JsonIgnore
    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    private void init() {
        setName("ModerationTool");
        //这是一个进行内容安全检测的工具。输入应该是一个文本的描述。
        setDescription("This is a tool for content security detection. The input should be a textual description.");
    }

    public ModerationTool() {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)), true, token);
        init();
    }

    public ModerationTool(String apiKey, Integer timeout) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(timeout), true, apiKey);
        init();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        try {
            String questionText = toolInput;
            ModerationRequest.ModerationRequestBuilder builder = ModerationRequest.builder()
                    .input(questionText);
            if(model != null) {
                builder.model(model);
            }
            ModerationRequest moderationRequest = builder.build();
            List<String> answerContentList = new ArrayList<>();
            service.createModeration(moderationRequest).getResults().forEach(e -> {
                log.warn("openai moderation answer:" + JSON.toJSONString(e));
                answerContentList.add(JSON.toJSONString(e));
            });

            String responseContent = answerContentList.stream().collect(Collectors.joining("\n"));
            return new ToolExecuteResult(responseContent, true);
        } catch (Exception ex) {
            log.error("error", ex);
            return new ToolExecuteResult("Answer: 当前系统有异常，请稍后再试." + System.currentTimeMillis(), true);
        }
    }
}

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

import com.alibaba.langengine.core.model.fastchat.image.CreateImageVariationRequest;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.FileTools;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * openai 图像变体工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class CreateImageVariationTool extends DefaultTool {

    private Integer imageN = 1;

    private String imageSize = "1024x1024"; //256x256, 512x512, or 1024x1024，Defaults to 1024x1024

    private String responeFormat = "url"; //url or b64_json

    private String user;

    @JsonIgnore
    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    private void init() {
        setName("CreateImageVariationTool");
        //这是一个可以把图像变成另外一个相似图像的工具。输入应该是一个图像的URL链接。
        setDescription("This is a tool that can turn an image into another similar image. The input should be a URL link of an image.");
    }

    public CreateImageVariationTool() {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)), true, token);
        init();
    }

    public CreateImageVariationTool(String apiKey, Integer timeout) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(timeout), true, apiKey);
        init();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        File image;
        try {
            toolInput = toolInput.replaceAll("<", "").replaceAll(">", "");
            CreateImageVariationRequest.CreateImageVariationRequestBuilder builder = CreateImageVariationRequest.builder()
                    .n(imageN)
                    .size(imageSize)
                    .responseFormat(responeFormat);
            if(user != null) {
                builder.user(user);
            }
            image = FileTools.getFileFromUrl(toolInput);
            CreateImageVariationRequest createImageVariationRequest = builder.build();
            List<String> answerContentList = new ArrayList<>();
            service.createImageVariation(createImageVariationRequest, image).getData().forEach(e -> {
                log.warn("openai imageVariation answer:" + e.getUrl() + "," + e.getB64Json());
                answerContentList.add(e.getUrl());
            });
            String imageContent = "";
            for (String answerContent : answerContentList) {
                imageContent += "![](" + answerContent + ") \r\n ";
            }
            return new ToolExecuteResult(imageContent, true);
        } catch (Exception ex) {
            log.error("error", ex);
            return new ToolExecuteResult("Answer: 当前系统有异常，请稍后再试." + System.currentTimeMillis(), true);
        }
    }
}

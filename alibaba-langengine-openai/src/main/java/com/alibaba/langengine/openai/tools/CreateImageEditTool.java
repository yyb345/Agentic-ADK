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
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.model.fastchat.image.CreateImageEditRequest;
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
import java.util.Map;
import java.util.TreeMap;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * openai图片编辑工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class CreateImageEditTool extends DefaultTool {

    private Integer imageN = 1;

    private String imageSize = "1024x1024"; //256x256, 512x512, or 1024x1024，Defaults to 1024x1024

    private String responeFormat = "url"; //url or b64_json

    private String user;

    @JsonIgnore
    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    private void init() {
        setName("CreateImageEditTool");
        //这是一个根据新文本提示创建现有图像的编辑工具。输入应该是一个json字符串，里面包括prompt，originalImageUrl，maskImageUrl字段。
        setDescription("This is an editing tool that creates existing images based on new text cues. The input should be a json string, which includes prompt, originalImageUrl, maskImageUrl fields.");

        Map<String, Object> args = getArgs();
        Map<String, Object> promptMap = new TreeMap<>();
        promptMap.put("title", "prompt");
        promptMap.put("type", "string");
        args.put("prompt", promptMap);

        Map<String, Object> originalImageUrlMap = new TreeMap<>();
        originalImageUrlMap.put("title", "original image url");
        originalImageUrlMap.put("type", "string");
        args.put("originalImageUrl", originalImageUrlMap);

        Map<String, Object> maskImageUrlMap = new TreeMap<>();
        maskImageUrlMap.put("title", "mask image url");
        maskImageUrlMap.put("type", "string");
        args.put("maskImageUrl", maskImageUrlMap);
    }

    public CreateImageEditTool() {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)), true, token);
        init();
    }

    public CreateImageEditTool(String apiKey, Integer timeout) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(timeout), true, apiKey);
        init();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        File image = null;
        File mask = null;
        try {
            toolInput = toolInput.replaceAll("json string ", "");
            Map<String, String> toolInputMap = JSON.parseObject(toolInput, new TypeReference<TreeMap>(){});
            CreateImageEditRequest.CreateImageEditRequestBuilder builder = CreateImageEditRequest.builder()
                    .prompt(toolInputMap.get("prompt"))
                    .n(imageN)
                    .size(imageSize)
                    .responseFormat(responeFormat);
            if(user != null) {
                builder.user(user);
            }
            CreateImageEditRequest createImageEditRequest = builder.build();
            List<String> answerContentList = new ArrayList<>();
            image = FileTools.getFileFromUrl(toolInputMap.get("originalImageUrl"));
            if(toolInputMap.containsKey("maskImageUrl")) {
                mask = FileTools.getFileFromUrl(toolInputMap.get("maskImageUrl"));
            }
            service.createImageEdit(createImageEditRequest, image, mask).getData().forEach(e -> {
                log.warn("chatgpt imageedit answer:" + e.getUrl() + "," + e.getB64Json());
                answerContentList.add(e.getUrl());
            });
            String imageContent = "";
            for (String answerContent: answerContentList) {
                imageContent += "![](" + answerContent + ") \r\n ";
            }
            return new ToolExecuteResult(imageContent, true);
        } catch (Exception ex) {
            log.error("error", ex);
            return new ToolExecuteResult("Answer: 当前系统有异常，请稍后再试." + System.currentTimeMillis(), true);
        }
    }
}

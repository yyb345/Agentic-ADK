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
package com.alibaba.langengine.dashscope.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.dashscope.model.image.*;
import com.alibaba.langengine.dashscope.model.service.DashScopeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * @author chenshuaixin
 * @date 2024/05/17
 */
@Slf4j
@Data
public class DashScopeImage extends BaseLLM<DashImageRequest> {
    /**
     * 通义万象文生图服务
     * https://help.aliyun.com/zh/dashscope/developer-reference/api-details-9?spm=5176.28197632.0.0.5f307e06uXa77Z&disableWebsiteRedirect=true#8f79b5d0f8ker
     *
     * @author chenshuaixin
     */

    private DashScopeService service;

    private String token = DASHSCOPE_API_KEY;

    /**
     * 指定需要的调用的模型
     * 支持 wanx-v1
     */
    private String model = "wanx-v1";

    private String negative_prompt;

    private String ref_img;

    private String style;

    private String size;

    private Integer n;

    private Integer seed;

    private Float ref_strength;

    private String ref_mode;


    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    @Override
    public String getLlmModelName() {
        return model;
    }

    public DashScopeImage() {
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token);
    }

    public DashScopeImage(String token) {
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        this.token = token;
        this.service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true,
                token);
    }

    @Override
    public DashImageRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        DashImageRequest dashImageRequest = new DashImageRequest();
        dashImageRequest.setModel(model);
        DashImageInputRequest input = new DashImageInputRequest();
        input.setPrompt(chatMessages.get(0).getContent().toString());
        if (negative_prompt != null) {
            input.setNegative_prompt(negative_prompt);
        }
        if (ref_img != null) {
            input.setRef_img(ref_img);
        }
        DashImageParametersRequest parameters = new DashImageParametersRequest();
        if (style != null) {
            parameters.setStyle(style);
        }
        if (size != null) {
            parameters.setSize(size);
        }
        if (n != null){
            parameters.setN(n);
        }
        if (seed != null){
            parameters.setSeed(seed);
        }
        if (ref_strength != null){
            parameters.setRef_strength(ref_strength);
        }
        if (ref_mode != null){
            parameters.setRef_mode(ref_mode);
        }
        dashImageRequest.setInput(input);
        dashImageRequest.setParameters(parameters);

        return dashImageRequest;
    }

    @Override
    public String runRequest(DashImageRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        DashImageResult createImageResult = service.createTextToImage(request);
        return JSON.toJSONString(createImageResult);
    }

    @Override
    public String runRequestStream(DashImageRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        DashImageResult createImageResult = service.createTextToImage(request);
        return JSON.toJSONString(createImageResult);
    }

    public String queryImage(String taskId){
        DashImageQueryResult dashImageQueryResult =service.queryImage(taskId);
        return JSON.toJSONString(dashImageQueryResult);
    }


}

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
package com.alibaba.langengine.msearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.msearch.completion.CompletionAttachment;
import com.alibaba.langengine.msearch.completion.CompletionRequest;
import com.alibaba.langengine.msearch.completion.CompletionResult;
import com.alibaba.langengine.msearch.completion.PluginSchema;
import com.alibaba.langengine.msearch.service.MSearchService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.msearch.MSearchConfiguration.MSEARCH_API_KEY;
import static com.alibaba.langengine.msearch.MSearchConfiguration.MSEARCH_API_TIMEOUT;

/**
 * 企业智能搜索
 * 企业智能搜索产品，集成了达摩院强大的算法模型，包含文本理解、图像理解、文本生成大模型等，并内置丰富的常用搜索场景，帮助您快速构建自己的搜索服务。
 * https://msearch.console.aliyun.com/
 * https://help.aliyun.com/document_detail/2539827.html?spm=a2c4g.2539798.0.0.27d14d85dN83WF
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class MSearchLLM extends BaseLLM<ChatCompletionRequest> {

    private String serverUrl = "https://fastai-x-gateway.aliyuncs.com/";

//    private static final String DEFAULT_PLUGIN_SCHEMAS = "{\n" +
//            "            \"search_plugin\":{\n" +
//            "                \"search_enhance\":{  \n" +
//            "                    \"vector_recall_ratio\":0.7, \n" +
//            "                    \"acc_sorting\": 200,    \n" +
//            "                    \"search_size\": 3,    \n" +
//            "                    \"custom_sorting\":\"{\\\"default\\\": {\\\"features\\\": [{\\\"name\\\": \\\"vector_index\\\", \\\"weights\\\": 0.5, \\\"threshold\\\": 0, \\\"norm\\\": true}, {\\\"name\\\": \\\"static_value\\\", \\\"field\\\": \\\"_rc_t_score\\\", \\\"weights\\\": 0.04, \\\"threshold\\\": 0, \\\"norm\\\": false}, {\\\"name\\\": \\\"query_match_ratio\\\", \\\"field\\\": \\\"question\\\", \\\"weights\\\": 0.5, \\\"threshold\\\": 0.0, \\\"norm\\\": false}, {\\\"name\\\": \\\"query_match_ratio\\\", \\\"field\\\": \\\"sim_question\\\", \\\"weights\\\": 0.5, \\\"threshold\\\": 0.0, \\\"norm\\\": false}, {\\\"name\\\": \\\"char_edit_similarity\\\", \\\"field\\\": \\\"question\\\", \\\"weights\\\": 0.6, \\\"threshold\\\": 0.0, \\\"norm\\\": false}, {\\\"name\\\": \\\"char_lcs_match_ratio\\\", \\\"field\\\": \\\"question\\\", \\\"weights\\\": 0.25, \\\"threshold\\\": 0.0, \\\"norm\\\": false}, {\\\"name\\\": \\\"overlap_coefficient\\\", \\\"weights\\\": 0.1, \\\"field\\\": \\\"answer\\\", \\\"threshold\\\": 0, \\\"norm\\\": false}], \\\"aggregate_algo\\\": \\\"weight_avg\\\"}}\",\n" +
//            "                    \"filters\": \"\"\n" +
//            "                }   \n" +
//            "            }   \n" +
//            "        }";
    private MSearchService service;

    /**
     * token，必填
     */
    private String token = MSEARCH_API_KEY;

    private String sessionId = "";

    private PluginSchema pluginSchemas = new PluginSchema();

    private boolean stream = false;

    private boolean greenNet = false;

    private String tenantId = "msearch";

    /**
     * 服务id，必填
     */
    private String appId;

    private String roundId = "";

    private Double temperature = 1.0d;

    private Double topP = 0.7d;

    private boolean allowDirectAnswer = true;

    private Integer numDoc = 3;

    private String tpQueryRewrite = "N";

    private boolean debug = false;

    public MSearchLLM(String appId) {
        this.appId = appId;
        service = new MSearchService(serverUrl, Duration.ofSeconds(Long.parseLong(MSEARCH_API_TIMEOUT)), true, token);
    }

    public MSearchLLM(String appId, String token) {
        this.appId = appId;
        this.token = token;
        service = new MSearchService(serverUrl, Duration.ofSeconds(Long.parseLong(MSEARCH_API_TIMEOUT)), true, token);
    }

    @Override
    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> input = new HashMap<>();
        input.put("prompts", prompt);
        input.put("session_id", sessionId);
        if(pluginSchemas != null) {
            input.put("plugin_schemas", pluginSchemas);
        }
        input.put("max_tokens", getMaxTokens());
        input.put("stream", stream);
        input.put("green_net", greenNet);
        input.put("tenant_id", tenantId);
        input.put("app_id", appId);
        input.put("round_id", roundId);
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .input(input);

        Map<String, Object> parameters = new HashMap<>();
        if(topP != null) {
            parameters.put("top_p", topP);
        }
        if(temperature != null) {
            parameters.put("temperature", temperature);
        }
        parameters.put("allow_direct_answer", allowDirectAnswer);
        parameters.put("num_doc", numDoc);
        parameters.put("tp_query_rewrite", tpQueryRewrite);
        builder.parameters(parameters);

        CompletionRequest completionRequest = builder.build();
        List<String> answerContentList = new ArrayList<>();
        final String[] responseContentArr = new String[1];
        responseContentArr[0] = "";
        CompletionAttachment attachment = null;
        if(stream) {
            service.streamCompletion(completionRequest)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(e -> {
                        if(!StringUtils.isEmpty(e.getContent())) {
                            String answer = e.getContent();
                            log.warn("msearch stream answer:" + answer);
                            if (answer != null) {
                                responseContentArr[0] = answer;
                                if (consumer != null) {
                                    consumer.accept(answer);
                                }
                            }
                        }
                    });
        } else {
            CompletionResult completionResult = service.createCompletion(completionRequest);
            if(completionResult.getData() == null) {
                throw new RuntimeException("completion error.code:" + completionResult.getCode() + ",msg:" + completionResult.getMsg() + ",requestId:" + completionResult.getRequestId());
            }
            String answer = completionResult.getData().getContent();
            log.warn("msearch answer:" + answer);
            if (answer != null) {
                answerContentList.add(answer);
            }
            attachment = completionResult.getData().getAttachment();
        }
        String responseContent = responseContentArr[0];
        if(!stream) {
            responseContent = answerContentList.stream().collect(Collectors.joining(""));
        }
        responseContent = responseContent.replace(prompt + "\n\n", "");

        log.warn("msearch final answer:" + responseContent);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("content", responseContent);
        responseMap.put("attachment", JSON.toJSONString(attachment));

        return JSON.toJSONString(responseMap);
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }
}

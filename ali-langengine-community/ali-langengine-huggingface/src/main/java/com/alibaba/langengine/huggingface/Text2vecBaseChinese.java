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
package com.alibaba.langengine.huggingface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.huggingface.completion.CompletionRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * Calculate the semantic similarity between one text and a list of other sentences by comparing their embeddings.
 * 通过比较它们的嵌入来计算一个文本和一系列其他句子之间的语义相似度。
 *
 * https://huggingface.co/shibing624/text2vec-base-chinese
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class Text2vecBaseChinese extends HuggingfaceLLM {

    /**
     * 模型名称
     * required
     */
    private String model = "shibing624/text2vec-base-chinese";

    /**
     * (Default: true). Boolean. There is a cache layer on the inference API to speedup requests we have already seen. Most models can use those results as is as models are deterministic (meaning the results will be the same anyway). However if you use a non deterministic model, you can set this parameter to prevent the caching mechanism from being used resulting in a real new query.
     */
    private boolean useCache = true;

    /**
     * (Default: false) Boolean. If the model is not ready, wait for it instead of receiving 503. It limits the number of requests required to get your inference done. It is advised to only set this flag to true after receiving a 503 error as it will limit hanging in your application to known places.
     */
    private boolean waitForModel = false;

    /**
     * 是否流模式
     */
    private boolean stream = false;

    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> inputs = JSON.parseObject(prompt, new TypeReference<HashMap>(){});
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .inputs(inputs);

        Map<String, Object> options = new HashMap<>();
        options.put("use_cache", useCache);
        options.put("wait_for_model", waitForModel);
        builder.options(options);

        CompletionRequest completionRequest = builder.build();
        List<String> answerContentList = new ArrayList<>();
        List<Object> response = getService().createBasicListCompletion(model, completionRequest);
        answerContentList.add(JSON.toJSONString(response));
        String responseContent = answerContentList.stream().collect(Collectors.joining("\n"));
        return responseContent;
    }
}

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
 * GPT2 LLM
 * Use to continue text from a prompt. This is a very generic task.
 * 用于从提示继续文本。 这是一个非常通用的任务。
 *
 * https://huggingface.co/gpt2
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class GPT2 extends HuggingfaceLLM {

    /**
     * 模型名称
     * required
     */
    private String model = "gpt2"; // gpt2-large, gpt2-medium, gpt2-xl

    /**
     * (Default: None). Integer to define the top tokens considered within the sample operation to create new text.
     */
    private Integer topK;

    /**
     * (Default: None). Float to define the tokens that are within the sample operation of text generation. Add tokens in the sample for more probable to least probable until the sum of the probabilities is greater than top_p.
     */
    private Double topP;

    /**
     * (Default: 1.0). Float (0.0-100.0). The temperature of the sampling operation. 1 means regular sampling, 0 means always take the highest score, 100.0 is getting closer to uniform probability.
     */
    private Double temperature = 1.0d;

    /**
     * (Default: None). Float (0.0-100.0). The more a token is used within generation the more it is penalized to not be picked in successive generation passes.
     */
    private Double repetitionPenalty;

    /**
     * (Default: None). Int (0-250). The amount of new tokens to be generated, this does not include the input length it is a estimate of the size of generated text you want. Each new tokens slows down the request, so look for balance between response times and length of text generated.
     */
    private Integer maxNewTokens;

    /**
     * (Default: None). Float (0-120.0). The amount of time in seconds that the query should take maximum. Network can cause some overhead so it will be a soft limit. Use that in combination with max_new_tokens for best results.
     */
    private Integer maxTime;

    /**
     * (Default: True). Bool. If set to False, the return results will not contain the original query making it easier for prompting.
     */
    private boolean returnFullText = true;

    /**
     * (Default: 1). Integer. The number of proposition you want to be returned.
     */
    private Integer numReturnSequences = 1;

    /**
     * (Optional: True). Bool. Whether or not to use sampling, use greedy decoding otherwise.
     */
    private boolean doSample = true;

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
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .inputs(prompt);

        Map<String, Object> parameters = new HashMap<>();
        if(topK != null) {
            parameters.put("top_k", topK);
        }
        if(topP != null) {
            parameters.put("top_p", topP);
        }
        parameters.put("temperature", temperature);
        if(repetitionPenalty != null) {
            parameters.put("repetition_penalty", repetitionPenalty);
        }
        if(maxNewTokens != null) {
            parameters.put("max_new_tokens", maxNewTokens);
        }
        if(maxTime != null) {
            parameters.put("max_time", maxTime);
        }
        parameters.put("return_full_text", returnFullText);
        if(numReturnSequences != null) {
            parameters.put("num_return_sequences", numReturnSequences);
        }
        parameters.put("do_sample", doSample);
        builder.parameters(parameters);

        Map<String, Object> options = new HashMap<>();
        options.put("use_cache", useCache);
        options.put("wait_for_model", waitForModel);
        builder.options(options);

        CompletionRequest completionRequest = builder.build();
        List<String> answerContentList = new ArrayList<>();
        List<Map<String, Object>> response = getService().createListCompletion(model, completionRequest);
        for (Map<String, Object> responseMap: response) {
            if(responseMap.containsKey("generated_text")) {
                answerContentList.add(responseMap.get("generated_text").toString());
            }
        }
        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        responseContent = responseContent.replace(prompt + "\n\n", "");
        return responseContent;
    }
}

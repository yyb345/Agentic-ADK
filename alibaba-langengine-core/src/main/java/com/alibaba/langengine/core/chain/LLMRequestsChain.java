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
package com.alibaba.langengine.core.chain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.langengine.core.callback.ExecutionContext;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 请求 URL 然后使用 LLM 解析结果的链。
 *
 * @author aihe.ah
 * @time 2023/9/20
 * 功能说明：
 */
@Data
public class LLMRequestsChain extends Chain {

    private static final Map<String, String> DEFAULT_HEADERS;

    static {
        DEFAULT_HEADERS = new HashMap<>();
        DEFAULT_HEADERS.put("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 "
                + "Safari/537.36");
    }

    /**
     * 默认的请求结果存储Key
     */
    public static final String REQUESTS_KEY = "requests_result";

    /**
     * RequestChain从Url中取对应的地址
     */
    public static final String INPUT_KEY = "url";
    private LLMChain llmChain;
    private int textLength = 8000;

    private String outputKey = "output";

    public LLMRequestsChain() throws Exception {
        validateEnvironment();
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext,
        Consumer<String> consumer, Map<String, Object> extraAttributes) {
        try {
            String url = (String)inputs.get(INPUT_KEY);

            Document doc = Jsoup.connect(url).headers(DEFAULT_HEADERS).get();
            String text = doc.body().text();
            if (StringUtils.isEmpty(text)) {
                throw new RuntimeException("requests result is empty");
            }

            if (text.length() > textLength) {
                text = text.substring(0, textLength);
            }

            Map<String, Object> otherKeys = new HashMap<>(inputs);
            otherKeys.remove(INPUT_KEY);
            otherKeys.put(REQUESTS_KEY, text);

            // 把拿到的结果重新进行llm处理
            Object result = llmChain.predict(otherKeys, executionContext, extraAttributes);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(outputKey, result);

            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException("requests error:", e);
        }

    }

    public void validateEnvironment() throws Exception {
        try {
            Class.forName("org.jsoup.Jsoup");
        } catch (ClassNotFoundException e) {
            throw new Exception("Could not import jsoup java package. Please add it as a dependency.");
        }
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(INPUT_KEY);
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(outputKey);
    }

    public String getChainType() {
        return "llm_requests_chain";
    }

}

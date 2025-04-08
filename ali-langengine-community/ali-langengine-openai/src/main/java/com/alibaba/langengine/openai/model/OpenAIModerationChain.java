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
package com.alibaba.langengine.openai.model;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.model.fastchat.moderation.Moderation;
import com.alibaba.langengine.core.model.fastchat.moderation.ModerationRequest;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.alibaba.langengine.openai.OpenAIConfiguration.*;

/**
 * Pass input through a moderation endpoint.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class OpenAIModerationChain extends Chain {

    private String inputKey = "input";

    private String outputKey = "text";

    private String modelName = "text-moderation-stable"; // text-moderation-stable, text-moderation-latest

    private FastChatService service;

    private String token = OPENAI_API_KEY;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    /**
     * 国内代理openai server url
     */
    private static final String DEFAULT_PROXY_BASE_URL = "https://api.openai-proxy.com/";

    public OpenAIModerationChain() {
        this(null);
    }

    public OpenAIModerationChain(Proxy proxy) {
        String serverUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(OPENAI_AI_TIMEOUT)), true, token, proxy);
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String input = inputs.get(inputKey).toString();

        ModerationRequest.ModerationRequestBuilder builder = ModerationRequest.builder()
                .input(input);
        if(!StringUtils.isEmpty(modelName)) {
            builder.model(modelName);
        }
        ModerationRequest moderationRequest = builder.build();
        List<Moderation> moderations = service.createModeration(moderationRequest).getResults();
        String responseContent = moderate(input, moderations);
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(outputKey, responseContent);
        return outputs;
    }

    private String moderate(String input, List<Moderation> moderations) {
        if(CollectionUtils.isEmpty(moderations)) {
            return input;
        }
        if (moderations.get(0).isFlagged()) {
            return "Text was found that violates OpenAI's content policy.";
        }
        return input;
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[]{ inputKey });
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(new String[] { outputKey });
    }
}

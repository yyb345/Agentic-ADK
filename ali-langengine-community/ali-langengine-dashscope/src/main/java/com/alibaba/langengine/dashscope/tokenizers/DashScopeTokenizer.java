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
package com.alibaba.langengine.dashscope.tokenizers;

import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tokenizers.Tokenization;
import com.alibaba.dashscope.tokenizers.TokenizationResult;
import com.alibaba.langengine.core.jtokkit.api.EncodingType;
import com.alibaba.langengine.core.tokenizers.Tokenizer;
import lombok.Data;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.DASHSCOPE_API_KEY;
import static com.alibaba.langengine.dashscope.DashScopeModelName.QWEN_PLUS;
import static com.alibaba.langengine.dashscope.Utils.getOrDefault;

@Data
public class DashScopeTokenizer extends Tokenizer {

    private String apiKey = DASHSCOPE_API_KEY;
    private String modelName;
    private Tokenization tokenizer;

    public DashScopeTokenizer(String modelName) {
        this.modelName = getOrDefault(modelName, QWEN_PLUS);
        this.tokenizer = new Tokenization();
    }

    public DashScopeTokenizer(String apiKey, String modelName) {
        this.apiKey = apiKey;
        this.modelName = getOrDefault(modelName, QWEN_PLUS);
        this.tokenizer = new Tokenization();
    }

    @Override
    public int getTokenCount(String text) {
        try {
            QwenParam param = QwenParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .prompt(text)
                    .build();

            TokenizationResult result = tokenizer.call(param);
            return result.getUsage().getInputTokens();
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EncodingType getEncoding() {
        return null;
    }

//    @Override
//    public int estimateTokenCountInMessage(ChatMessage message) {
//        return estimateTokenCountInMessages(Collections.singleton(message));
//    }
//
//    @Override
//    public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
//        try {
//            QwenParam param = QwenParam.builder()
//                    .apiKey(apiKey)
//                    .model(modelName)
//                    .messages(toQwenMessages(messages))
//                    .build();
//
//            TokenizationResult result = tokenizer.call(param);
//            return result.getUsage().getInputTokens();
//        } catch (NoApiKeyException | InputRequiredException e) {
//            throw new RuntimeException(e);
//        }
//    }
}

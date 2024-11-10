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
package com.alibaba.langengine.core.chain.llmguard;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.core.chain.llmguard.input.InputScannerFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sinh Le
 */
public class LLMGuardPromptChainTest {


    @Test
    public void testFactory() {
        ChatOpenAI openAI = new ChatOpenAI();
        openAI.setModel(OpenAIModelConstants.GPT_4_TURBO);
        LLMGuardPromptChain chain = LLMGuardPromptChain.init(
                tokenLimit(1000),
                banSubstring("game", "play"),
                banTopics(openAI, 0.75, "gamming",  "violence", "Lack of Moral"),
                allowTopics(openAI, 0.6, "education")
        );
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "What is the most significant event in American history?");
        Map<String, Object> result = chain.run(inputs);
        System.out.println(result);
    }

    @Test
    public void testValidateTokenLimit() {
        assertThrows(IllegalArgumentException.class, () -> {
            LLMGuardPromptChain chain = LLMGuardPromptChain.init(tokenLimit(1));
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", "it will throw error");
            Map<String, Object> result = chain.run(inputs);
        });
    }

    @Test
    public void testValidTokenLimit() {
        LLMGuardPromptChain chain = LLMGuardPromptChain.init(tokenLimit(100));

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "it will not throw error");
        Map<String, Object> result = chain.run(inputs);
    }

    @Test
    public void testBanSustring() {
        LLMGuardPromptChain chain = LLMGuardPromptChain.init(banSubstring("test", "abc"));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "it will not throw error");
        Map<String, Object> result = chain.run(inputs);

        assertThatThrownBy(() -> {
            inputs.put("input", "it will not throw error by add test");
            chain.run(inputs);
        });
    }

    @Test
    public void testBanSustringContainAlls() {
        LLMGuardPromptChain chain = LLMGuardPromptChain.init(banSubstring(false, true, true, "test", "abc"));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "it will not throw error");
        Map<String, Object> result = chain.run(inputs);

        inputs.put("input", "it will not throw error by add test");
        chain.run(inputs);

        assertThatThrownBy(() -> {
            inputs.put("input", "it will not throw error by add test and abc");
            chain.run(inputs);
        });
    }


    @Test
    public void testBanTopics() {
        ChatOpenAI openAI = new ChatOpenAI();
        openAI.setModel(OpenAIModelConstants.GPT_4_TURBO);
        LLMGuardPromptChain chain = LLMGuardPromptChain.init(banTopics(openAI, 0.75, "movie", "violence", "Lack of Moral"));

        assertThatThrownBy(() -> {
            String prompt = "Critics praised the film for its innovative storytelling and compelling character development";
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", prompt);
            chain.run(inputs);
        });

        assertThatThrownBy(() -> {
            String prompt = "Despite its harsh realities, the film's depiction of violence serves as a poignant commentary on the consequences of conflict and the human cost of war";
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", prompt);
            chain.run(inputs);
        });

        String prompt = "What is Alibaba?";
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", prompt);
        chain.run(inputs);

    }

    @Test
    public void testAllowTopics() {
        ChatOpenAI openAI = new ChatOpenAI();
        openAI.setModel(OpenAIModelConstants.GPT_4_TURBO);
        LLMGuardPromptChain chain = LLMGuardPromptChain.init(allowTopics(openAI, 0.75, "movie"));

        String prompt = "Critics praised the film for its innovative storytelling and compelling character development";
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", prompt);
        chain.run(inputs);

        assertThatThrownBy(() -> {
            String prompt2 = "Alibaba is biggest company in China";
            inputs.put("input", prompt2);
            chain.run(inputs);
        });
    }
}

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
package com.alibaba.langengine.core.chain.llmguard.input;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.tokenizers.Tokenizer;

import java.util.Arrays;

/**
 * @author Sinh Le
 */
public class InputScannerFactory {
    public static InputScanner tokenLimit(int max) {
        return new TokenLimit(max);
    }

    public static InputScanner tokenLimit(int max, Tokenizer tokenizer) {
        return new TokenLimit(max, tokenizer);
    }

    public static InputScanner banSubstring(boolean isCaseSensitive, boolean machTypeWord, boolean containAll, String... substring) {
        return new BanSubstrings(Arrays.asList(substring), isCaseSensitive, machTypeWord, containAll);
    }

    public static InputScanner banSubstring(String... substring) {
        return new BanSubstrings(Arrays.asList(substring), true, true, false);
    }


    public static InputScanner banTopics(BaseLanguageModel llm, double threshold, String... topics) {
        return new BanTopics(llm, threshold, topics);
    }

    public static InputScanner banTopics(BaseLanguageModel llm, PromptTemplate promptTemplate, double threshold, String... topics) {
        return new BanTopics(llm, promptTemplate, threshold, Arrays.asList(topics));
    }

    public static InputScanner allowTopics(BaseLanguageModel llm, double threshold, String... topics) {
        return new AllowTopics(llm, threshold, topics);
    }

    public static InputScanner allowTopics(BaseLanguageModel llm, PromptTemplate promptTemplate, double threshold, String... topics) {
        return new AllowTopics(llm, promptTemplate, threshold, Arrays.asList(topics));
    }

}

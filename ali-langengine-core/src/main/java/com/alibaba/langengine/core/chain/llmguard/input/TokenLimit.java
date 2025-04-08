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


import com.alibaba.langengine.core.chain.llmguard.ScannerResult;
import com.alibaba.langengine.core.tokenizers.GPT35AboveTokenizer;
import com.alibaba.langengine.core.tokenizers.Tokenizer;

import java.util.Map;

/**
 * @author Sinh Le
 */
public class TokenLimit implements InputScanner {
    private int limit = 1000;

    Tokenizer tokenizer;

    public TokenLimit(int limit) {
        this.limit = limit;
        this.tokenizer = new GPT35AboveTokenizer();
    }

    public TokenLimit(int limit, Tokenizer tokenizer) {
        this.limit = limit;
        this.tokenizer = tokenizer;
    }

    @Override
    public ScannerResult scan(String text, Map<String, Object> extraAttributes) {
        if (text.trim().isEmpty()) {
            return new ScannerResult(text, true, 0.0);
        }
        int tokenCounter = tokenizer.getTokenCount(text);
        if (tokenCounter < limit) {
            return new ScannerResult(text, true, 0.0);
        }
        return new ScannerResult(text, false, 1.0);
    }

    @Override
    public String getName() {
        return "TokenLimit";
    }
}

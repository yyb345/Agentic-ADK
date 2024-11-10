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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.llmguard.input.InputScanner;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Sinh Le
 */
@Data
public class LLMGuardPromptChain extends Chain {

    private String inputKey = "input";
    private String outputKey = "sanitized_input";
    private List<String> ignoreErrors;
    private boolean raiseError;
    private List<InputScanner> scanners;

    public LLMGuardPromptChain(List<String> ignoreErrors, boolean raiseError, List<InputScanner> scanners) {
        this.ignoreErrors = ignoreErrors;
        this.raiseError = raiseError;
        this.scanners = scanners;
    }

    public static LLMGuardPromptChain init(InputScanner... scanner) {
        return new LLMGuardPromptChain(Collections.emptyList(), true, Arrays.asList(scanner));
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String prompt = (String)inputs.get(inputKey) ;
        for (InputScanner scanner: scanners) {
            ScannerResult result = scanner.scan(prompt, extraAttributes);
            validateResult(scanner, result);
        }
        Map<String, Object> output = new HashMap<>(1);
        output.put(outputKey, prompt);
        return output;
    }

    private void validateResult(InputScanner scanner, ScannerResult result) {
        if (result.isValid()) {
            return;
        }
        if (ignoreErrors.contains(scanner.getName())) {
            return;
        }
        if (raiseError) {
            throw new IllegalArgumentException("This prompt was determined as invalid based on configured policies with risk score " + result.getConfidence());
        }
    }

    @Override
    public List<String> getInputKeys() {
        return Collections.singletonList(inputKey);
    }

    @Override
    public List<String> getOutputKeys() {
        return Collections.singletonList(outputKey);
    }
}

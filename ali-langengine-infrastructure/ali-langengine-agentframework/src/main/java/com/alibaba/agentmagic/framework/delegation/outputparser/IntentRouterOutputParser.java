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
package com.alibaba.agentmagic.framework.delegation.outputparser;

import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class IntentRouterOutputParser extends BaseOutputParser<Map<String, Object>> {

    private String defaultDestination = "DEFAULT";
    private String nextInputsInnerKey = "input";

    private static final List<String> EXPECTED_KEYS = Arrays.asList(new String[]{ "destination", "next_inputs" });

    @Override
    public Map<String, Object> parse(String text) {
        try {
            Map<String, Object> parsed = PromptConverter.parseAndCheckJsonMarkdown(text, EXPECTED_KEYS);
            if(!(parsed.get("destination") instanceof String)) {
                throw new RuntimeException("Expected 'destination' to be a string.");
            }
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(nextInputsInnerKey, parsed.get("next_inputs"));
            parsed.put("next_inputs", inputs);
            if(((String) parsed.get("destination")).trim().toLowerCase().equals(defaultDestination.toLowerCase())) {
                parsed.remove("destination");
            } else {
                parsed.put("destination", ((String) parsed.get("destination")).trim());
                // TODO 增加全局变量模式自动识别的解析

            }
            return parsed;
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Parsing text\n%s\n raised following error:\n%s", text, e.getMessage()));
        }
    }
}

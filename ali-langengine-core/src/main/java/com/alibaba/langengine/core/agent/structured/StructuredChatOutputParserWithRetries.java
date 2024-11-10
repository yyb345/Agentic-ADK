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
package com.alibaba.langengine.core.agent.structured;

import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;

/**
 * StructuredChatOutputParserWithRetries
 *
 * @author xiaoxuan.lp
 */
@Data
public class StructuredChatOutputParserWithRetries extends AgentOutputParser {

    private AgentOutputParser baseParser = new StructuredChatOutputParser();

    @Override
    public String getFormatInstructions() {
        return PromptConstants.FORMAT_INSTRUCTIONS;
    }

    @Override
    public String getParserType() {
        return "structured_chat_with_retries";
    }

    @Override
    public Object parse(String text) {
        // TODO ...
        return null;
    }
}

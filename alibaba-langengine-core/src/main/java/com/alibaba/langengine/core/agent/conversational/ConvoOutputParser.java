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
package com.alibaba.langengine.core.agent.conversational;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Output parser for the conversational agent.
 *
 * @author xiaoxuan.lp
 */
@Data
public class ConvoOutputParser extends AgentOutputParser {

    /**
     * Prefix to use before AI output.
     */
    private String aiPrefix = "AI";

    @Override
    public Object parse(String text) {
        if (text.contains(aiPrefix + ":")) {
            String[] parts = text.split(aiPrefix + ":");
            String output = parts[parts.length - 1].trim();
            return new AgentFinish(Collections.singletonMap("output", output), text);
        }
        Pattern pattern = Pattern.compile("Action: (.*?)[\\n]*Action Input: (.*)");
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            throw new RuntimeException("Could not parse LLM output: `" + text + "`");
        }
        String action = matcher.group(1);
        String actionInput = matcher.group(2);

        AgentAction agentAction = new AgentAction();
        agentAction.setTool(action.trim());
        agentAction.setToolInput(actionInput.trim());
        agentAction.setLog(text);
        return agentAction;
    }

    @Override
    public String getFormatInstructions() {
        return PromptConstants.FORMAT_INSTRUCTIONS;
    }

    @Override
    public String getParserType() {
        return "conversational";
    }
}

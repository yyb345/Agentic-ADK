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
package com.alibaba.langengine.core.agent.reactdoc;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Output parser for the ReAct agent.
 *
 * @author xiaoxuan.lp
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReActOutputParser extends AgentOutputParser {
    private final String finishActionName;

    public ReActOutputParser(boolean isCH) {
        this.finishActionName = isCH ? "完成" : "Finish";
    }

    @Override
    public Object parse(String text) {
        String actionPrefix = "Action: ";
        String[] lines = text.trim().split("\n");
//        String lastLine = lines[lines.length - 1];
        boolean exists = false;
        String lastLine = null;
        for (String line : lines) {
            if (line.startsWith(actionPrefix)) {
                exists = true;
                lastLine = line;
                break;
            }
        }
        if (!exists) {
            throw new RuntimeException("Could not parse LLM Output: " + text);
        }
        String actionBlock = lastLine;
        String actionStr = actionBlock.substring(actionPrefix.length());
        Matcher matcher = Pattern.compile("(.+?)\\[(.+?)\\]").matcher(actionStr);
        if (!matcher.find()) {
            throw new RuntimeException("Could not parse action directive: " + actionStr);
        }
        String action = matcher.group(1);
        String actionInput = matcher.group(2);
        if (finishActionName.equals(action)) {
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(Collections.singletonMap("output", actionInput));
            agentFinish.setLog(text);
            return agentFinish;
        } else {
            AgentAction agentAction = new AgentAction();
            agentAction.setTool(action);
            agentAction.setToolInput(actionInput);
            agentAction.setLog(text);
            return agentAction;
        }
    }

    @Override
    public String getParserType() {
        return "react";
    }
}

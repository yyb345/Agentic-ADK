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
package com.alibaba.langengine.core.outputparser;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.AgentOutputParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QwenStructuredChatOutputParser extends AgentOutputParser<AgentNextStep> {

    private static final String FINAL_ANSWER_ACTION = "Final Answer";

    @Override
    public String getFormatInstructions() {
        return null;
    }

    @Override
    public String getParserType() {
        return "structured_chat";
    }

    @Override
    public AgentNextStep parse(String text) {
        String regex = "Thought:(.*?)Action:(.*?)Action\\s*Input:(.*?)$";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return parseAgentActionOrFinish(matcher, text);
        } else {
            regex = "Thought:(.*?)Final\\s*Answer:(.*?)$";
            pattern = Pattern.compile(regex, Pattern.DOTALL);
            matcher = pattern.matcher(text);
            if(matcher.find()) {
                return parseAgentFinish(matcher, text);
            } else {
                regex = ".*(Final\\s*Answer):(.*?)$";
                pattern = Pattern.compile(regex, Pattern.DOTALL);
                matcher = pattern.matcher(text);
                if(matcher.find()) {
                    return parseAgentFinish(matcher, text);
                }else{
                    return getAgentFinish( text);
                }
            }
        }
    }

    private AgentNextStep parseAgentActionOrFinish(Matcher matcher, String text) {
//        String thought = matcher.group(1).trim().replaceAll("\n", "");
        String action = matcher.group(2).trim().replaceAll("\n", "");
        String actionInput = matcher.group(3).trim();

        if (FINAL_ANSWER_ACTION.equals(action)) {
            String finalAnswer = actionInput;
            return getAgentFinish(finalAnswer);
        }
        AgentAction agentAction = new AgentAction();
        agentAction.setTool(action);
        agentAction.setToolInput(actionInput);
        agentAction.setLog(text);
        return agentAction;
    }

    private AgentNextStep parseAgentFinish(Matcher matcher, String text) {
        String finalAnswer = matcher.group(2).trim();
        return getAgentFinish(finalAnswer);
    }
}
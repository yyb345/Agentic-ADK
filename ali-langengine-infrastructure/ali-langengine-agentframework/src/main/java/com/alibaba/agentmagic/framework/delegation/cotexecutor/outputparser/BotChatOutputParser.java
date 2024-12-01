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
package com.alibaba.agentmagic.framework.delegation.cotexecutor.outputparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BotChatOutputParser extends AgentOutputParser<AgentNextStep> {

    @Override
    public String getFormatInstructions() {
        return null;
    }

    @Override
    public String getParserType() {
        return "bot_chat";
    }

    @Override
    public AgentNextStep parse(String text) {
        log.info("BotChatOutputParser parse:" + text);

        //{"finishReason":"function_call","index":0,"message":{"content":"。","functionCall":{"arguments":"{\"location\": \"杭州, China\"}","name":"get_current_weather"}}}

        Pattern pattern = Pattern.compile("Final Answer:\\s*((?:[^\\\\]|\\\\.)*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String answer = matcher.group(1);
            AgentFinish agentFinish = getAgentFinish(answer);
            log.info("AgentFinish:" + JSON.toJSONString(agentFinish));
            return agentFinish;
        }

//        Pattern pattern = Pattern.compile("\"Mode\": \"(.*?)\"");
//        Matcher matcher = pattern.matcher(text);
//
//        if (matcher.find()) {
//            String mode = matcher.group(1);
//            if(mode.equals("Function")) {
//                AgentAction agentAction = new AgentAction();
//
//                Pattern functionPattern = Pattern.compile("\"Related_function\": \"(.*?)\"");
//                Matcher functionMatcher = functionPattern.matcher(text);
//                if (functionMatcher.find()) {
//                    String function = functionMatcher.group(1);
//                    agentAction.setTool(function);
//                }
//
//                Pattern argumentPattern = Pattern.compile("\"Content\": \"<f> (.*?)\"");
//                Matcher argumentMatcher = argumentPattern.matcher(text);
//                if (argumentMatcher.find()) {
//                    String argument = argumentMatcher.group(1);
//                    agentAction.setToolInput(argument);
//                }
//                agentAction.setLog(text);
//                log.info("AgentAction:" + JSON.toJSONString(agentAction));
//                return agentAction;
//            } if(mode.equals("Message")) {
//                Pattern messagePattern = Pattern.compile("\"Content\":\\s*\"((?:[^\"\\\\]|\\\\.)*)");
//                Matcher messageMatcher = messagePattern.matcher(text);
//                if (messageMatcher.find()) {
//                    String message = messageMatcher.group(1);
//                    return getAgentFinish(message);
//                }
//            }
//        }
//
        AgentFinish agentFinish = getAgentFinish(text);
        log.info("AgentFinish:" + JSON.toJSONString(agentFinish));
        return agentFinish;
    }
}
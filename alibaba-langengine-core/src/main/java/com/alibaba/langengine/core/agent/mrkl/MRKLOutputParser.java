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
package com.alibaba.langengine.core.agent.mrkl;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentFinish;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.langengine.core.agent.mrkl.PromptConstants.FORMAT_INSTRUCTIONS;

/**
 * MRKL OutputParser
 *
 * @author xiaoxuan.lp
 */
@Data
public class MRKLOutputParser extends AgentOutputParser {

    private static final String FINAL_ANSWER_ACTION = "Final Answer:";

    public String getFormatInstructions() {
        return FORMAT_INSTRUCTIONS;
    }

    @Override
    public String getParserType() {
        return "mrkl";
    }

    @Override
    public Object parse(String text) {
        if(text.indexOf(FINAL_ANSWER_ACTION) >= 0) {
            Map<String, Object> returnValues = new HashMap<>();
            returnValues.put("output", text.substring(text.indexOf(FINAL_ANSWER_ACTION) + FINAL_ANSWER_ACTION.length()));
            AgentFinish agentFinish = new AgentFinish();
            agentFinish.setReturnValues(returnValues);
            agentFinish.setLog(text);
            return agentFinish;
        }
        String regex = "Action\\s*\\d*\\s*:[\\s]*(.*?)[\\s]*Action\\s*\\d*\\s*Input\\s*\\d*\\s*:[\\s]*(.*)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()) {
            String action = matcher.group(1);
            String actionInput = matcher.group(2);
            AgentAction agentAction = new AgentAction();
            agentAction.setTool(action);
            agentAction.setToolInput(actionInput);
            agentAction.setLog(text);
            return agentAction;
        }
        throw new RuntimeException(String.format("Could not parse LLM output: %s", text));
    }
}

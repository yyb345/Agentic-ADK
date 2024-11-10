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
package com.alibaba.langengine.core.agent.semantickernel.planning;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentNextStep;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.tool.BaseTool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class StepwisePlannerOutputParser extends AgentOutputParser<AgentNextStep> {

    private static final Pattern S_ACTION_REGEX = Pattern.compile("\\[ACTION\\][^{}]*(\\{.*\\})", Pattern.DOTALL);
    private static final Pattern S_FINAL_REGEX = Pattern.compile("\\[FINAL[_\\s\\-]ANSWER\\](.*)", Pattern.DOTALL);

    private Map<String, BaseTool> toolMap;

    @Override
    public AgentNextStep parse(String text) {
        //[ACTION]
        //{
        //  "action": "WriterSkill.Brainstorm",
        //  "action_variables": {
        //    "input": "Valentine's day date ideas"
        //  }
        //}
        log.warn("StepwisePlanner AgentOutputParser parse:" + text);
        // Extract final answer

        if(text.indexOf("[THOUGHT]") >= 0) {
            text = text.substring(text.indexOf("[THOUGHT]"));
        }
        Matcher funishMatcher = S_FINAL_REGEX.matcher(text);
        if(funishMatcher.find()) {
            String finishAnswer = funishMatcher.group(1).trim();
            return getAgentFinish(finishAnswer);
        }

        Matcher actionMatcher = S_ACTION_REGEX.matcher(text);
        if (actionMatcher.find()) {
            String actionJson = actionMatcher.group(1).trim();
            JSONObject generatedPlan = JSON.parseObject(actionJson);
            String skFunction = generatedPlan.getString("action");

            AgentAction agentAction = new AgentAction();
            agentAction.setTool(skFunction);
            agentAction.setToolInput(JSON.toJSONString(generatedPlan.getJSONObject("action_variables")));
            agentAction.setLog(text);
            return agentAction;
        } else {
            return getAgentFinish(text);
        }
    }
}

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
package com.alibaba.langengine.dashscope.model.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentOutputParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashScopeStructuredChatOutputParser extends AgentOutputParser {

    private static final String FINAL_ANSWER_ACTION = "Final Answer";

    private static final String FINAL_ANSWER_ACTION_CH = "最终答案";

    @Override
    public String getFormatInstructions() {
        return null;
    }

    @Override
    public String getParserType() {
        return "structured_chat";
    }

    @Override
    public Object parse(String text) {
        String regex = "Thought:(.*?)Action:(.*?)Action\\s*Input:(.*?)$";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return parseAgentActionOrFinish(matcher, text);
        } else {
            regex = "Action:(.*?)Action\\s*Input:(.*?)$";
            pattern = Pattern.compile(regex, Pattern.DOTALL);
            matcher = pattern.matcher(text);
            if(matcher.find()) {
                return parseSecondAgentActionOrFinish(matcher, text);
            }

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

    private Object parseAgentActionOrFinish(Matcher matcher, String text) {
//        String thought = matcher.group(1).trim().replaceAll("\n", "");
        String action = matcher.group(2).trim().replaceAll("\n", "");
        String actionInput = matcher.group(3).trim();

        if (FINAL_ANSWER_ACTION.equals(action) || FINAL_ANSWER_ACTION_CH.equals(action)) {
            return getAgentFinish(actionInput);
        }

        // 如果有Observation
        int index = actionInput.indexOf("Observation:");
        if (index > 0) {
            actionInput = actionInput.substring(0, index);
        }

        AgentAction agentAction = new AgentAction();
        agentAction.setTool(action);
        agentAction.setToolInput(actionInput);
        agentAction.setLog(text);
        return agentAction;
    }

    private Object parseSecondAgentActionOrFinish(Matcher matcher, String text) {
        String action = matcher.group(1).trim().replaceAll("\n", "");
        String actionInput = matcher.group(2).trim();

        if (FINAL_ANSWER_ACTION.equals(action) || FINAL_ANSWER_ACTION_CH.equals(action)) {
            return getAgentFinish(actionInput);
        }

        // 如果有Observation
        int index = actionInput.indexOf("Observation:");
        if (index > 0) {
            actionInput = actionInput.substring(0, index);
        }

        AgentAction agentAction = new AgentAction();
        agentAction.setTool(action);
        agentAction.setToolInput(actionInput);
        agentAction.setLog(text);
        return agentAction;
    }

    private Object parseAgentFinish(Matcher matcher, String text) {
        String finalAnswer = matcher.group(2).trim();
        return getAgentFinish(finalAnswer);
    }

    public static void main(String[] args){
        String text = "Thought: 工具查询到了具体的调用详情，需要根据Observation中的信息回答问题。\n" +
                "Action: FinalAnswer\n" +
                "Answer: 调用详情为：请求id为16lxqklu2vlaj的调用，API为taobao.trade.fullinfo.get，API名称为1111111，调用状态为isp.time-out。";

        DashScopeStructuredChatOutputParser outputParser = new DashScopeStructuredChatOutputParser();
        Object result = outputParser.parse(text);
        System.out.println(JSON.toJSONString(result));
    }
}
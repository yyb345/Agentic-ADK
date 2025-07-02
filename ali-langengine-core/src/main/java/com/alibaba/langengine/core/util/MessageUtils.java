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
package com.alibaba.langengine.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author aihe.ah
 * @time 2023/9/13 11:08
 * 功能说明：
 */
@Slf4j
public class MessageUtils {

    /**
     * Convert a message to a dict.
     *
     * @param message
     * @return
     */
    public static Map<String, Object> convertMessageToDict(BaseMessage message) {
        // Assuming BaseMessage, ChatMessage, HumanMessage, AIMessage, SystemMessage, and FunctionMessage are classes
        // you have defined in Java

        Map<String, Object> messageDict = new HashMap<>(4);

        if (message instanceof ChatMessage) {
            messageDict.put("role", ((ChatMessage)message).getRole());
            messageDict.put("content", ((ChatMessage)message).getContent());
        } else if (message instanceof HumanMessage) {
            messageDict.put("role", "user");
            messageDict.put("content", ((HumanMessage)message).getContent());
        } else if (message instanceof AIMessage) {
            messageDict.put("role", "assistant");
            messageDict.put("content", ((AIMessage)message).getContent());
            if (((AIMessage)message).getAdditionalKwargs().containsKey("function_call")) {
                messageDict.put("function_call", ((AIMessage)message).getAdditionalKwargs().get("function_call"));
                if ("".equals(messageDict.get("content"))) {
                    messageDict.put("content", null);
                }
            }
        } else if (message instanceof SystemMessage) {
            messageDict.put("role", "system");
            messageDict.put("content", ((SystemMessage)message).getContent());
        } else if (message instanceof FunctionMessage) {
            messageDict.put("role", "function");
            messageDict.put("content", ((FunctionMessage)message).getContent());
            messageDict.put("name", ((FunctionMessage)message).getName());
        } else {
            throw new IllegalArgumentException("Got unknown type " + message.getClass().getSimpleName());
        }

        if (message.getAdditionalKwargs().containsKey("name")) {
            messageDict.put("name", message.getAdditionalKwargs().get("name"));
        }

        return messageDict;
    }

    /**
     * 格式化AgentAction,默认为FunctionMessage
     * @param agentAction
     * @return
     */
    public static List<BaseMessage> formatActionMessage(AgentAction agentAction) {
        List<BaseMessage> intermediateStep = new ArrayList<>();
        // 结构示例：
        // [{
        //  "content": null,
        //	"functionCall": {
        //		"name": "add",
        //		"arguments": "{\"number1\":333,\"number2\":444}"
        //	},
        //	"role": "assistant"
        //}, {
        //	"content": "777",
        //	"name": "add",
        //	"role": "function"
        //}]

        log.info("agentAction is {}", agentAction);

//        boolean toolCallFormat = !StringUtils.isEmpty(agentAction.getPrevId());
        boolean toolCallFormat = !CollectionUtils.isEmpty(agentAction.getActions());

        if(!toolCallFormat) {
            AIMessage aiMessage = new AIMessage();
            Map<String, Object> functionCall = new HashMap<>();
            functionCall.put("name", agentAction.getTool());
            functionCall.put("arguments", agentAction.getToolInput());
            Map<String, Object> additional = new HashMap<>();
            additional.put("function_call", functionCall);
            aiMessage.setAdditionalKwargs(additional);
            if (!StringUtils.isEmpty(agentAction.getLog())) {
                aiMessage.setContent(agentAction.getLog());
            } else {
                aiMessage.setContent("");
            }
            intermediateStep.add(aiMessage);

            FunctionMessage functionMessage = new FunctionMessage();
            functionMessage.setName(agentAction.getTool());
            functionMessage.setContent(agentAction.getObservation());
            intermediateStep.add(functionMessage);
        } else {
            List<AgentAction> childActions =  agentAction.getActions();

            AIMessage aiMessage = new AIMessage();
            List<Map<String, Object>> toolCalls = new ArrayList<>();

            for (AgentAction childAction : childActions) {
                Map<String, Object> toolCall = new HashMap<>();
                Map<String, Object> functionCall = new HashMap<>();
                functionCall.put("name", childAction.getTool());
                functionCall.put("arguments", childAction.getToolInput());
                toolCall.put("function", functionCall);
                toolCall.put("id", childAction.getPrevId());
                toolCall.put("type", "function");
                toolCalls.add(toolCall);
            }

            Map<String, Object> additional = new HashMap<>();
            additional.put("tool_calls", toolCalls);
            aiMessage.setAdditionalKwargs(additional);

            if (agentAction.getLog() != null) {
                aiMessage.setContent(agentAction.getLog());
            } else {
                aiMessage.setContent("");
            }
            intermediateStep.add(aiMessage);

            for (AgentAction childAction : childActions) {
                ToolMessage toolMessage = new ToolMessage();
//                toolMessage.setName(agentAction.getTool());
                toolMessage.setTool_call_id(childAction.getPrevId());
                toolMessage.setContent(childAction.getObservation());
                intermediateStep.add(toolMessage);
            }
        }
        return intermediateStep;
    }
}

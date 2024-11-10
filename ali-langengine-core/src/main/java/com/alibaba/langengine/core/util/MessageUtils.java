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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.ChatMessage;
import com.alibaba.langengine.core.messages.FunctionMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;

/**
 * @author aihe.ah
 * @time 2023/9/13 11:08
 * 功能说明：
 */
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
}

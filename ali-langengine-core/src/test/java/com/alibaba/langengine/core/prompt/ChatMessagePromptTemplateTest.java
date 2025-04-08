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
package com.alibaba.langengine.core.prompt;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.impl.ChatMessagePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ChatMessagePromptTemplateTest {

    @Test
    public void test_format() {
        // success
        String template = "May the {subject} be with you";
        ChatMessagePromptTemplate chatMessagePromptTemplate = new ChatMessagePromptTemplate();
        chatMessagePromptTemplate.setPrompt(new PromptTemplate(template));
        chatMessagePromptTemplate.setRole("Jedi");

        Map<String, Object> args = new HashMap<>();
        args.put("subject", "force");
        BaseMessage baseMessage = chatMessagePromptTemplate.format(args);
        System.out.println(JSON.toJSONString(baseMessage));
    }
}

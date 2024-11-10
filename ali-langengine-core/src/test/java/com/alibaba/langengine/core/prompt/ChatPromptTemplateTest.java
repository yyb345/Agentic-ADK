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
import com.alibaba.langengine.core.prompt.impl.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatPromptTemplateTest {

    @Test
    public void test_format() {
        // success
        List<Object> messages = new ArrayList<>();

        SystemMessagePromptTemplate systemMessagePromptTemplate = new SystemMessagePromptTemplate();
        systemMessagePromptTemplate.setPrompt(new PromptTemplate("You are a helpful AI bot. Your name is {name}."));
        messages.add(systemMessagePromptTemplate);

        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        humanMessagePromptTemplate.setPrompt(new PromptTemplate("Hello, how are you doing?"));
        messages.add(humanMessagePromptTemplate);

        AIMessagePromptTemplate aiMessagePromptTemplate = new AIMessagePromptTemplate();
        aiMessagePromptTemplate.setPrompt(new PromptTemplate("I'm doing well, thanks!"));
        messages.add(aiMessagePromptTemplate);

        HumanMessagePromptTemplate humanMessagePromptTemplate2 = new HumanMessagePromptTemplate();
        humanMessagePromptTemplate2.setPrompt(new PromptTemplate("{user_input}"));
        messages.add(humanMessagePromptTemplate2);

        ChatPromptTemplate chatPromptTemplate = ChatPromptTemplate.fromMessages(messages);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Bob");
        args.put("user_input", "What is your name?");
        List<BaseMessage> baseMessages = chatPromptTemplate.formatMessages(args);
        System.out.println(JSON.toJSONString(baseMessages));
    }
}

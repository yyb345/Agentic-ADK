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
package com.alibaba.langengine.core.runnables;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import org.junit.jupiter.api.Test;

public class RunnableStreamTest {

    @Test
    public void test_run() {
        // success
//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("你是谁？");
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        RunnableInterface chain = Runnable.sequence(prompt, model);

        Object output = chain.stream(input, chunk -> {
            if(chunk instanceof BaseMessage) {
                System.out.println(((BaseMessage) chunk).getContent());
            } else {
                System.out.println(chunk);
            }
        });
    }
}

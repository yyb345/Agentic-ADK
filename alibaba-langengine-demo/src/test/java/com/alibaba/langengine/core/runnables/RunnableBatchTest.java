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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.outputparser.StrOutputParser;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.prompt.impl.HumanMessagePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.prompt.impl.SystemMessagePromptTemplate;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RunnableBatchTest {

    @Test
    public void test_RunnableWithFallbacks_batch() {
        // success
        List<Object> messages = new ArrayList<>();
        SystemMessagePromptTemplate systemMessagePromptTemplate = new SystemMessagePromptTemplate();
        systemMessagePromptTemplate.setPrompt(new PromptTemplate("You're a nice assistant who always includes a compliment in your response"));
        messages.add(systemMessagePromptTemplate);
        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        humanMessagePromptTemplate.setPrompt(new PromptTemplate("Why did the {animal} cross the road"));
        messages.add(humanMessagePromptTemplate);

        ChatPromptTemplate chatPrompt = ChatPromptTemplate.fromMessages(messages);

        BaseOutputParser outputParser = new StrOutputParser();

        //badModel
        ChatModelOpenAI badModel = new ChatModelOpenAI();
        badModel.setModel("gpt-fake");

        //badChain
        RunnableInterface badChain = Runnable.sequence(chatPrompt, badModel, outputParser);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("Instructions: You should always include a compliment in your response.\n" +
                "\n" +
                "Question: Why did the {animal} cross the road?");

        //goodModel
        ChatModelOpenAI goodModel = new ChatModelOpenAI();

        //goodChain
        RunnableInterface goodChain = Runnable.sequence(prompt, goodModel);

        //goodChain is a fallback with badChain.
        RunnableInterface chain = badChain.withFallbacks(goodChain);

        RunnableHashMap input1 = new RunnableHashMap() {{
            put("animal", "turtle");
        }};

        RunnableHashMap input2 = new RunnableHashMap() {{
            put("animal", "cat");
        }};

        List<RunnableHashMap> inputs = new ArrayList<>();
        inputs.add(input1);
        inputs.add(input2);
        List<Object> outputs = chain.batch(inputs);

        System.out.println("outputs:" + JSON.toJSONString(outputs));
    }

    @Test
    public void test_RunnableWithFallbacks_batchAsync() throws ExecutionException, InterruptedException {
        // success
        List<Object> messages = new ArrayList<>();
        SystemMessagePromptTemplate systemMessagePromptTemplate = new SystemMessagePromptTemplate();
        systemMessagePromptTemplate.setPrompt(new PromptTemplate("You're a nice assistant who always includes a compliment in your response"));
        messages.add(systemMessagePromptTemplate);
        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        humanMessagePromptTemplate.setPrompt(new PromptTemplate("Why did the {animal} cross the road"));
        messages.add(humanMessagePromptTemplate);

        ChatPromptTemplate chatPrompt = ChatPromptTemplate.fromMessages(messages);

        BaseOutputParser outputParser = new StrOutputParser();

        //badModel
        ChatModelOpenAI badModel = new ChatModelOpenAI();
        badModel.setModel("gpt-fake");

        //badChain
        RunnableInterface badChain = Runnable.sequence(chatPrompt, badModel, outputParser);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("Instructions: You should always include a compliment in your response.\n" +
                "\n" +
                "Question: Why did the {animal} cross the road?");

        //goodModel
        ChatModelOpenAI goodModel = new ChatModelOpenAI();

        //goodChain
        RunnableInterface goodChain = Runnable.sequence(prompt, goodModel);

        //goodChain is a fallback with badChain.
        RunnableInterface chain = badChain.withFallbacks(goodChain);

        RunnableHashMap input1 = new RunnableHashMap() {{
            put("animal", "turtle");
        }};

        RunnableHashMap input2 = new RunnableHashMap() {{
            put("animal", "cat");
        }};

        List<RunnableHashMap> inputs = new ArrayList<>();
        inputs.add(input1);
        inputs.add(input2);
        CompletableFuture<List<Object>> future = chain.batchAsync(inputs);

        System.out.println("Waiting for batchAsync response.");

        List<Object> outputs = future.get();

        System.out.println("outputs:" + JSON.toJSONString(outputs));
    }
}

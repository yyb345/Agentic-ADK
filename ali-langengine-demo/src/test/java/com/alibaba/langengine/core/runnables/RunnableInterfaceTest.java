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
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RunnableInterfaceTest extends BaseTest {

    @Test
    public void test_invoke() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        FakeAI model = new FakeAI();

        RunnableInterface chain = Runnable.sequence(prompt, model);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_stream() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setStream(true);

        RunnableInterface chain = Runnable.sequence(prompt, model);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        chain.stream(input, chunk -> System.out.println(((BaseMessage) chunk).getContent()));
    }

    @Test
    public void test_batch() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();

        RunnableInterface chain = Runnable.sequence(prompt, model);

        RunnableHashMap input1 = new RunnableHashMap() {{
            put("topic", "bears");
        }};

        RunnableHashMap input2 = new RunnableHashMap() {{
            put("topic", "cats");
        }};

        List<RunnableHashMap> inputs = new ArrayList<>();
        inputs.add(input1);
        inputs.add(input2);
        List<Object> outputs = chain.batch(inputs);
        System.out.println(JSON.toJSONString(outputs));
    }

    @Test
    public void test_invokeAsync() throws ExecutionException, InterruptedException {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();

        RunnableInterface chain = Runnable.sequence(prompt, model);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        CompletableFuture<Object> runnableOutput = chain.invokeAsync(input);
        Object output = runnableOutput.get();
        System.out.println(JSON.toJSONString(output));
    }

    @Test
    public void test_streamAsync() throws ExecutionException, InterruptedException {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setStream(true);

        RunnableInterface chain = Runnable.sequence(prompt, model);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        CompletableFuture<Object> future = chain.streamAsync(input, chunk -> chunkHandler(chunk));
        Object output = future.get();
        System.out.println(JSON.toJSONString(output));
    }

    @Test
    public void test_streamLogAsync() throws ExecutionException, InterruptedException {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setStream(true);

        RunnableInterface chain = Runnable.sequence(prompt, model);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        CompletableFuture<Object> future = chain.streamLogAsync(input, chunk -> chunkHandler(chunk));
        Object output = future.get();
        System.out.println(JSON.toJSONString(output));
    }

    @Test
    public void test_batchAsync() throws ExecutionException, InterruptedException {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();

        RunnableInterface chain = Runnable.sequence(prompt, model);

        RunnableHashMap input1 = new RunnableHashMap() {{
            put("topic", "bears");
        }};

        RunnableHashMap input2 = new RunnableHashMap() {{
            put("topic", "cats");
        }};

        List<RunnableHashMap> inputs = new ArrayList<>();
        inputs.add(input1);
        inputs.add(input2);
        CompletableFuture<List<Object>> future = chain.batchAsync(inputs);
        Object output = future.get();
        System.out.println(JSON.toJSONString(output));
    }
}

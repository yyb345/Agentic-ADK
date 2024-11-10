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
package com.alibaba.langengine.openai.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ChatOpenAITest {

    @Test
    public void test_predict() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        System.out.println("response:" + llm.predict("你是谁？"));
    }

    @Test
    public void test_predict_streamTrue() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setSseInc(true);
        llm.predict("你是谁？", Arrays.asList(new String[]{"Human:", "AI:"}), e -> {
            System.out.println(e);
        });
    }

    @Test
    public void test_predictAsync() throws ExecutionException, InterruptedException {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        long start = System.currentTimeMillis();
        CompletableFuture<String> future1 = llm.predictAsync("你是谁？");
        CompletableFuture<String> future2 = llm.predictAsync("你好？");
        CompletableFuture.allOf(future1, future2).get();
        future1.thenAccept(response -> System.out.println("response:" + response));
        future2.thenAccept(response -> System.out.println("response:" + response));
//        System.out.println(future1.get());
//        System.out.println(future2.get());
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predictAsync_streamTrue() throws ExecutionException, InterruptedException {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setStream(true);
        long start = System.currentTimeMillis();
        CompletableFuture<String> future1 = llm.predictAsync("你是谁？");
        CompletableFuture<String> future2 = llm.predictAsync("你好？");
        CompletableFuture.allOf(future1, future2).get();
        future1.thenAccept(response -> System.out.println("response:" + response));
        future2.thenAccept(response -> System.out.println("response:" + response));
//        System.out.println(future1.get());
//        System.out.println(future2.get());
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

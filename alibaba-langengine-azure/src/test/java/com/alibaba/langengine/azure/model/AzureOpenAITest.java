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
package com.alibaba.langengine.azure.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author: andrea.phl
 * @create: 2024-01-26 10:51
 **/
public class AzureOpenAITest {

    private String apiBase = "*";
    private String apiKey = "*";
    private String deploymentName = "gpt-35-turbo";
    private String apiVersion = "2023-06-01-preview";

    @Test
    public void test_predict_streamTrue() {
        AzureOpenAI llm = new AzureOpenAI(apiBase, deploymentName, apiVersion, apiKey);
        llm.setStream(true);
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict("你是谁？", Arrays.asList("Human:", "AI:")));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predictAsync() throws ExecutionException, InterruptedException {
        AzureOpenAI llm = new AzureOpenAI(apiBase, deploymentName, apiVersion, apiKey);
        long start = System.currentTimeMillis();
        CompletableFuture<String> future1 = llm.predictAsync("你是谁？");
        CompletableFuture<String> future2 = llm.predictAsync("你好？");
        CompletableFuture.allOf(future1, future2).get();
        future1.thenAccept(response -> System.out.println("response:" + response));
        future2.thenAccept(response -> System.out.println("response:" + response));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }


    @Test
    public void test_predictAsync_streamTrue() throws ExecutionException, InterruptedException {
        AzureOpenAI llm = new AzureOpenAI(apiBase, deploymentName, apiVersion, apiKey);
        llm.setStream(true);
        long start = System.currentTimeMillis();
        CompletableFuture<String> future1 = llm.predictAsync("你是谁？");
        CompletableFuture<String> future2 = llm.predictAsync("你好？");
        CompletableFuture.allOf(future1, future2).get();
        future1.thenAccept(response -> System.out.println("response:" + response));
        future2.thenAccept(response -> System.out.println("response:" + response));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

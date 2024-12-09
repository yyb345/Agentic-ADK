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
package com.alibaba.langengine.agentframework.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ResourceUtils {

//    public static final DelayQueue<DelayTask<InstanceSignal>> NODE_RETRY_QUEUE = new DelayQueue<>();

    /**
     * 本地重试线程池
     */
    public static final ThreadPoolExecutor RETRY_POOL = new ThreadPoolExecutor(100, 100,
            10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("EVENT-RETRY-THREAD-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 方法异步回调线程池
     */
    public static final ThreadPoolExecutor API_CALLBACK_POOL = new ThreadPoolExecutor(30, 30,
            10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("API-CALLBACK-THREAD-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());

//    public static void main(String[] args) throws InterruptedException {
//        for (int i = 1; i <= 10; i++) {
//            NODE_RETRY_QUEUE.add(new DelayTask<>(null,i * 1000L));
//        }
//        while (NODE_RETRY_QUEUE.size() > 0) {
//            DelayTask<InstanceSignal> task = NODE_RETRY_QUEUE.take();
//            System.out.println(task.getExpire());
//        }
//    }

}

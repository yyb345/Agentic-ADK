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
package com.alibaba.langengine.minimax.model;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * @author aihe.ah
 * @time 2023/12/18
 * 功能说明：
 */
public class SharedClient {
    // 创建一个共享的OkHttpClient实例
    public static OkHttpClient sharedClient = new OkHttpClient.Builder()
        .connectionPool(new ConnectionPool(5, 1, TimeUnit.MINUTES))
        .build();
}

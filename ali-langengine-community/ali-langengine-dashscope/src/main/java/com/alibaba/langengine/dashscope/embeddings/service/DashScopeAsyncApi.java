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
package com.alibaba.langengine.dashscope.embeddings.service;

import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncRequest;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncResult;
import io.reactivex.Single;
import retrofit2.http.*;

import java.util.Map;

public interface DashScopeAsyncApi {

    @POST("/api/v1/services/embeddings/text-embedding/text-embedding")
    Single<EmbeddingAsyncResult> createEmbeddingsAsync(@Body EmbeddingAsyncRequest request,
                                                       @HeaderMap Map<String, String> headers);

    @GET("/api/v1/tasks/{task_id}")
    Single<EmbeddingAsyncResult> getEmbeddingTask(@Path(value = "task_id") String taskId);
}
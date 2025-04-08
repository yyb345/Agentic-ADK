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
package com.alibaba.langengine.dashscope.model.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.dashscope.model.completion.CompletionRequest;
import com.alibaba.langengine.dashscope.model.completion.CompletionResult;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingRequest;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingResult;
import com.alibaba.langengine.dashscope.model.image.DashImageQueryResult;
import com.alibaba.langengine.dashscope.model.image.DashImageResult;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

/**
 * DashScope API
 *
 * @author xiaoxuan.lp
 */
public interface DashScopeApi {

    @POST("/api/v1/services/aigc/text-generation/generation")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request,
                                              @HeaderMap Map<String, String> headers);

    @Streaming
    @POST("/api/v1/services/aigc/text-generation/generation")
    Call<ResponseBody> createCompletionStream(@Body CompletionRequest request,
                                              @HeaderMap Map<String, String> headers);

    @POST("/api/v1/services/embeddings/text-embedding/text-embedding")
    Single<EmbeddingResult> createEmbeddings(@Body EmbeddingRequest request);

    @POST("/api/v1/services/aigc/multimodal-generation/generation")
    Single<CompletionResult> createMultimodalGeneration(@Body CompletionRequest request,
                                                        @HeaderMap Map<String, String> headers);

    @Streaming
    @POST("/api/v1/services/aigc/multimodal-generation/generation")
    Call<ResponseBody> createMultimodalGenerationStream(@Body CompletionRequest request,
                                                        @HeaderMap Map<String, String> headers);
    @POST("/api/v1/services/aigc/text2image/image-synthesis")
    Single<DashImageResult> createTextToImage(@Body JSONObject request,
                                              @HeaderMap Map<String, String> headers);

    @GET("/api/v1/tasks/{task_id}")
    Single<DashImageQueryResult> queryImageFile(@Path("task_id") String task_id);
}

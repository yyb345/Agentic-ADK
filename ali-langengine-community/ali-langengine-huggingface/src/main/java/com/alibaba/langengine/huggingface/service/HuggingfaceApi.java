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
package com.alibaba.langengine.huggingface.service;

import com.alibaba.langengine.huggingface.completion.CompletionRequest;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;
import java.util.Map;

/**
 * HuggingFace API
 *
 * @author xiaoxuan.lp
 */
public interface HuggingfaceApi {

    /**
     * createCompletion
     *
     * @param modelId
     * @param request
     * @return
     */
    @POST("/models/{model_id}")
    Single<Object> createCompletion(@Path("model_id") String modelId, @Body CompletionRequest request);

    /**
     * createListCompletion
     *
     * @param modelId
     * @param request
     * @return
     */
    @POST("/models/{model_id}")
    Single<List<Map<String, Object>>> createMapListCompletion(@Path("model_id") String modelId, @Body CompletionRequest request);

    /**
     * createBasicListCompletion
     *
     * @param modelId
     * @param request
     * @return
     */
    @POST("/models/{model_id}")
    Single<List<Object>> createBasicListCompletion(@Path("model_id") String modelId, @Body CompletionRequest request);
}
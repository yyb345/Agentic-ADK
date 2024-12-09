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
package com.alibaba.langengine.vertexai.model.service;

import com.alibaba.langengine.vertexai.model.completion.CompletionRequest;
import com.alibaba.langengine.vertexai.model.completion.CompletionResult;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VertexAIApi {

    /**
     * createCompletion
     *
     * @param modelId
     * @param request
     * @return
     */
    @POST("/v1/projects/{project_id}/locations/us-central1/publishers/google/models/{model_id}:predict")
    Single<CompletionResult> createCompletion(@Path("project_id") String projectId,
                                              @Path("model_id") String modelId,
                                              @Body CompletionRequest request);
}

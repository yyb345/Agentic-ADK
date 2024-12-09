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
package com.alibaba.langengine.azure.embeddings.service;

import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingRequest;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingResult;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author: andrea.phl
 * @create: 2024-01-29 14:39
 **/
public interface AzureOpenAIEmbeddingsApi {

    @POST("/{deployment-path}/embeddings")
    Single<EmbeddingResult> createChatCompletion(@Path(value = "deployment-path", encoded = true) String deploymentPath, @Query("api-version") String apiVersion, @Body EmbeddingRequest request);


}

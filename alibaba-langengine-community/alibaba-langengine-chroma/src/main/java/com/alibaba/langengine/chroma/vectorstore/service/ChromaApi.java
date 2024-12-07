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
package com.alibaba.langengine.chroma.vectorstore.service;

import io.reactivex.Single;
import retrofit2.http.*;

/**
 * Chroma API
 *
 * @author xiaoxuan.lp
 */
public interface ChromaApi {

    @GET("/api/v1/collections/{collection_name}")
    @Headers({"Content-Type: application/json"})
    Single<ChromaCollection> collection(@Path("collection_name") String collectionName);

    @POST("/api/v1/collections")
    @Headers({"Content-Type: application/json"})
    Single<ChromaCollection> createCollection(@Body CreateCollectionRequest createCollectionRequest);

    @POST("/api/v1/collections/{collection_id}/add")
    @Headers({"Content-Type: application/json"})
    Single<Boolean> addEmbeddings(@Path("collection_id") String collectionId, @Body ChromaEmbeddingsRequest embedding);

    @POST("/api/v1/collections/{collection_id}/query")
    @Headers({"Content-Type: application/json"})
    Single<ChromaQueryResponse> queryCollection(@Path("collection_id") String collectionId, @Body ChromaQueryRequest queryRequest);
}

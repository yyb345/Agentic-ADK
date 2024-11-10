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
package com.alibaba.langengine.moonshot.model.service;

import com.alibaba.langengine.moonshot.model.completion.CompletionRequest;
import com.alibaba.langengine.moonshot.model.completion.CompletionResult;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface MoonshotApi {

    @Streaming
    @POST("/{version}/chat/completions")
    Call<ResponseBody> createCompletionStream(@Path(value = "version", encoded = true) String apiVersion, @Body CompletionRequest request);

    @POST("/{version}/chat/completions")
    Single<CompletionResult> createCompletion(@Path(value = "version", encoded = true) String apiVersion, @Body CompletionRequest request);
}

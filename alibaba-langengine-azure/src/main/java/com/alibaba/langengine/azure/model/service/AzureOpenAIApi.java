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
package com.alibaba.langengine.azure.model.service;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionResult;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * @author: andrea.phl
 * @create: 2024-01-29 14:39
 **/
public interface AzureOpenAIApi {

    @POST("/{deployment-path}/chat/completions")
    Single<ChatCompletionResult> createChatCompletion(@Path(value = "deployment-path", encoded = true) String deploymentPath, @Query("api-version") String apiVersion, @Body ChatCompletionRequest request);

    @Streaming
    @POST("/{deployment-path}/chat/completions")
    Call<ResponseBody> streamChatCompletion(@Path(value = "deployment-path", encoded = true) String deploymentPath, @Query("api-version") String apiVersion, @Body ChatCompletionRequest request);

}

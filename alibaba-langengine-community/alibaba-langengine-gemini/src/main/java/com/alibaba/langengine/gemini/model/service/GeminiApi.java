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
package com.alibaba.langengine.gemini.model.service;

import com.alibaba.langengine.gemini.model.domain.GenerateContentRequest;
import com.alibaba.langengine.gemini.model.domain.GenerateContentResult;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Gemini Api Proxy
 *
 * @author xiaoxuan.lp
 */
public interface GeminiApi {

    @POST("/v1/models/{model_name}:generateContent")
    Single<GenerateContentResult> generateContent(@Path("model_name") String modelName,
                                                  @Query("key") String key,
                                                  @Body GenerateContentRequest request);

    @Streaming
    @POST("/v1/models/{model_name}:streamGenerateContent")
    Call<ResponseBody> generateContentStream(@Path("model_name") String modelName,
                                             @Query("key") String key,
                                             @Body GenerateContentRequest request);
}

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
package com.alibaba.langengine.msearch.service;

import com.alibaba.langengine.msearch.completion.CompletionRequest;
import com.alibaba.langengine.msearch.completion.CompletionResult;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

import java.util.Map;

/**
 *  MSearch Api
 *  https://help.aliyun.com/document_detail/2539827.html?spm=5176.28356950.help.dexternal.35c25eaaf4g0Vv#IokX3
 *
 * @author xiaoxuan.lp
 */
public interface MSearchApi {

    @POST("/msearch/api/chat")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request,
                                              @HeaderMap Map<String, String> headers);

    @Streaming
    @POST("/msearch/api/chat")
    Call<ResponseBody> createCompletionStream(@Body CompletionRequest request);
}

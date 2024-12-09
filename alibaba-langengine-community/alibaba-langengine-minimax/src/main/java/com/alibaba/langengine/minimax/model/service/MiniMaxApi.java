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
package com.alibaba.langengine.minimax.model.service;

import com.alibaba.langengine.minimax.model.model.*;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * @author aihe.ah
 * @time 2023/12/18
 * 功能说明：
 */
public interface MiniMaxApi {

    @Streaming
    @POST("/v1/text/chatcompletion_pro")
    Call<ResponseBody> chatCompletionProStream(
        @Query("GroupId") String groupId, @Body MiniMaxParameters parameters
    );

    @POST("/v1/text/chatcompletion_pro")
    Single<MiniMaxResult> chatCompletionPro(
        @Query("GroupId") String groupId, @Body MiniMaxParameters parameters
    );

    /**
     * https://api.minimax.chat/document/guides/chat-model/chat/api?id=6569c88e48bc7b684b3037a9
     * 专门用于对话的模型：https://api.minimax.chat/v1/text/chatcompletion
     */
    @POST("/v1/text/chatcompletion")
    Single<Minimax55Result> chatCompletion(
        @Query("GroupId") String groupId, @Body Minimax55Parameters parameters
    );

    /**
     * 流式返回结果
     */
    @POST("/v1/text/chatcompletion")
    @Streaming
    Flowable<ResponseBody> chatCompletionStream(
        @Query("GroupId") String groupId, @Body Minimax55Parameters parameters
    );

    /**
     * text_to_speech
     * 语音生成：https://api.minimax.chat/document/guides/T2A-model/tts/api?id=6569c8be48bc7b684b3037df
     * 如果生成成功，返回的是音频流
     * 如果生成失败，返回的是字符串错误信息
     */
    @POST("/v1/text_to_speech")
    Single<ResponseBody> textToSpeech(@Query("GroupId") String groupId, @Body MinimaxText2SpeechParams parameters);

    /**
     * 流式语音生成
     * https://api.minimax.chat/document/guides/T2A-model/stream?id=65701c77024fd5d1dffbb8fe
     * /v1/tts/stream
     */
    @POST("/v1/tts/stream")
    @Streaming
    Call<ResponseBody> textToSpeechStream(@Query("GroupId") String groupId, @Body MinimaxText2SpeechParams parameters);

}

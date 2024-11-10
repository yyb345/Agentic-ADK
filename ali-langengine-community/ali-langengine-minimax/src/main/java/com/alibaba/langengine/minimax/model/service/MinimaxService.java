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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.minimax.model.FastJsonConverterFactory;
import com.alibaba.langengine.minimax.model.SharedClient;
import com.alibaba.langengine.minimax.model.model.MiniMaxParameters;
import com.alibaba.langengine.minimax.model.model.MiniMaxResult;
import com.alibaba.langengine.minimax.model.model.Minimax55Parameters;
import com.alibaba.langengine.minimax.model.model.Minimax55Result;
import com.alibaba.langengine.minimax.model.model.MinimaxAudioStreamResult;
import com.alibaba.langengine.minimax.model.model.MinimaxText2SpeechParams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Minimax服务
 *
 * @author aihe.ah
 */
@Data
@Slf4j
public class MinimaxService extends RetrofitInitService<MiniMaxApi> {

    private static Pattern pattern = Pattern.compile("^data: \\{.*\\}$");

    public MinimaxService() {
        super();
    }

    public MinimaxService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    public MinimaxService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<MiniMaxApi> getServiceApiClass() {
        return MiniMaxApi.class;
    }

    /**
     * 阻塞式调用
     *
     * @param request
     * @return
     */
    public MiniMaxResult createCompletion(String groupId, MiniMaxParameters request) {
        request.setStream(false);
        return execute(getApi().chatCompletionPro(groupId, request));
    }

    public Flowable<MiniMaxResult> streamCompletion(String groupId, MiniMaxParameters request) {
        request.setStream(true);
        return stream(getApi().chatCompletionProStream(groupId, request), MiniMaxResult.class);
    }

    /**
     * 对话式模型的调用
     *
     * @param groupId
     * @param params
     * @return
     */
    public Minimax55Result createChatCompletion(String groupId, Minimax55Parameters params) {
        params.setStream(false);
        return execute(getApi().chatCompletion(groupId, params));
    }

    /**
     * 对话式模型流式调用
     *
     * @param groupId
     * @param params
     * @return
     */
    public Flowable<Minimax55Result> streamChatCompletion(String groupId, Minimax55Parameters params) {
        params.setStream(true);

        // 返回的Flowable<ResponseBody>经过flatMapPublisher转换为Flowable<MiniMaxResult>
        return getApi().chatCompletionStream(groupId, params)
            .flatMap(responseBody -> {
                // 将ResponseBody转换为Flowable<MiniMaxResult>
                return convertResponseBodyToFlowableOfMiniMaxResult(responseBody);
            });
    }

    private Flowable<Minimax55Result> convertResponseBodyToFlowableOfMiniMaxResult(ResponseBody responseBody) {
        return Flowable.create(emitter -> {
            try {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                String line;
                //Gson gson = new Gson(); // 使用Gson来处理JSON字符串

                while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
                    try {
                        // 假设每一行都是一个完整的JSON对象
                        String jsonData = line;
                        if (line.contains("data: ")) {
                            jsonData = line.substring("data: ".length());
                        }

                        Minimax55Result result = JSON.parseObject(jsonData, Minimax55Result.class);
                        emitter.onNext(result);
                    } catch (Exception e) {
                        emitter.onError(e); // 如果JSON解析失败，则发送错误
                    }
                }
                emitter.onComplete(); // 完成流的发送
            } finally {
                responseBody.close(); // 确保ResponseBody被关闭
            }
        }, BackpressureStrategy.BUFFER); // 使用BUFFER策略来应对背压
    }

    private Flowable<String> parseSseEvent(String sseLine) {
        // 这里的解析逻辑取决于SSE事件的具体格式，以下是一个基本的例子
        if (sseLine.contains("data: ")) {
            //System.out.println("Received SSE event: " + sseLine);
            String jsonData = sseLine.substring("data: ".length());
            return Flowable.just(jsonData);
        }
        return Flowable.empty();
    }

    public ResponseBody createAudio(String groupId, MinimaxText2SpeechParams params) {
        ResponseBody responseBody = execute(getApi().textToSpeech(groupId, params));
        return responseBody;
    }

    public Flowable<MinimaxAudioStreamResult> streamAudio(String groupId, MinimaxText2SpeechParams params) {
        MiniMaxApi miniMaxApi = getApi();
        Flowable<MinimaxAudioStreamResult> resultFlowable = stream(miniMaxApi.textToSpeechStream(groupId, params),
            MinimaxAudioStreamResult.class);
        return resultFlowable;
    }

    @Override
    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = SharedClient.sharedClient.newBuilder()
            .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        // contribute by dapeng.fdp
        if (proxy != null) {
            builder.proxy(proxy);
        }
        builder.addInterceptor(new MiniMaxAuthenticationInterceptor(getToken()));

        return builder.build();
    }

    @Override
    public Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder().baseUrl(getBaseUrl()).client(client)
            //.addConverterFactory(JacksonConverterFactory.create(mapper))
            .addConverterFactory(FastJsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
    }
}

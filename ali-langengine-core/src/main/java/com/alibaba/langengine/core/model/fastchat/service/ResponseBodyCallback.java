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
package com.alibaba.langengine.core.model.fastchat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.FlowableEmitter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.alibaba.langengine.core.util.JacksonUtils;

/**
 * 响应回调
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class ResponseBodyCallback implements Callback<ResponseBody> {
    private static final ObjectMapper mapper = JacksonUtils.defaultObjectMapper();

    private FlowableEmitter<SSE> emitter;
    private boolean emitDone;

    public ResponseBodyCallback(FlowableEmitter<SSE> emitter, boolean emitDone) {
        this.emitter = emitter;
        this.emitDone = emitDone;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        log.info("ResponseBodyCallback onResponse");
        BufferedReader reader = null;

        try {
            if (!response.isSuccessful()) {
                HttpException e = new HttpException(response);
                ResponseBody errorBody = response.errorBody();

                if (errorBody == null) {
                    throw e;
                } else {
                    FastChatError error = mapper.readValue(
                            errorBody.string(),
                            FastChatError.class
                    );
                    throw new FastChatHttpException(error, e, e.code());
                }
            }

            InputStream in = response.body().byteStream();
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            SSE sse = null;

            while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
//                log.info("stream line is {}", line);
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    sse = new SSE(data);
                } else if (line.equals("") && sse != null) {
                    if (sse.isDone()) {
                        if (emitDone) {
                            emitter.onNext(sse);
                        }
                        break;
                    }

                    emitter.onNext(sse);
                    sse = null;
                }
                else {
                    if (line.startsWith("id:")
                            || line.startsWith("event:")
                            || line.startsWith("retry:")
                            || line.startsWith(":HTTP_STATUS/200")) {
                        continue;
                    }
                    throw new SSEFormatException("Invalid sse format! " + line);
                }
            }

            log.info("stream emitter onComplete");
            emitter.onComplete();

        } catch (Throwable t) {
            log.info("stream emitter onFailure");
            onFailure(call, t);
        } finally {
            log.info("stream emitter finally start");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
            log.info("stream emitter finally end");
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        emitter.onError(t);
    }
}

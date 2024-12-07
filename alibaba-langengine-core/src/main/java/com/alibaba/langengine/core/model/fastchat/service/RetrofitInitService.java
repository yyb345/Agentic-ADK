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

import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.Proxy;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 大模型基础服务调用框架
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class RetrofitInitService<T> {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(100);

    /**
     * 用于存储调用者的类
     */
    protected static final ThreadLocal<Class<?>> callerClass = new ThreadLocal<>();

    /**
     * 大模型基础地址
     */
    private String baseUrl;

    /**
     * 默认超时时间
     */
    private Duration defaultTimeout;

    /**
     * 每个service内置的mapper
     */
    private ObjectMapper mapper;

    @JsonIgnore
    private T api;

    @JsonIgnore
    private ExecutorService executorService;

    private Proxy proxy;

    @JsonIgnore
    private OkHttpClient client;

    /**
     * 授权token
     */
    private String token;

    /**
     * 是否需要授权
     */
    private boolean authentication;

    private boolean debug = false;

    private Long timeout;

    public RetrofitInitService() {
    }

    public RetrofitInitService(Duration timeout) {
        this(null, timeout, true, null);
    }

    public RetrofitInitService(String serverUrl, Duration timeout, boolean authentication, String token) {
        this(serverUrl, timeout, authentication, token, null);
    }

    public RetrofitInitService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        this(serverUrl, timeout, authentication, token, proxy, null);
    }

    public RetrofitInitService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy,
        Class<T> apiClass) {
        if (serverUrl != null) {
            baseUrl = serverUrl;
        }
        setAuthentication(authentication);
        setToken(token);
        setTimeout(timeout.getSeconds());
        setClient(defaultClient(timeout, proxy));
        setExecutorService(client.dispatcher().executorService());

        Retrofit retrofit = defaultRetrofit(client, JacksonUtils.getServiceMapper(getClass()));
        if (apiClass != null) {
            this.api = retrofit.create(apiClass);
        } else {
            this.api = retrofit.create(getServiceApiClass());
        }
    }

    public OkHttpClient getClient() {
        if (this.client == null) {
            this.client = defaultClient(Duration.ofSeconds(timeout), proxy);
        }
        return this.client;
    }

    public T getApi() {
        if (this.api == null) {
            ObjectMapper objectMapper = getMapper();
            Retrofit retrofit = defaultRetrofit(getClient(), objectMapper);
            this.api = retrofit.create(getServiceApiClass());
        }
        return this.api;
    }

    public ObjectMapper getMapper() {
        if (mapper == null) {
            return JacksonUtils.getServiceMapper(getClass());
        }
        return mapper;
    }

    public ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = client.dispatcher().executorService();
        }
        return this.executorService;
    }

    public abstract Class<T> getServiceApiClass();

    public RetrofitInitService(T api) {
        this.api = api;
        this.executorService = null;
    }

    public RetrofitInitService(T api, final ExecutorService executorService) {
        this.api = api;
        this.executorService = executorService;
    }

    public void shutdownExecutor() {
        Objects.requireNonNull(this.executorService, "executorService must be set in order to shut down");
        this.executorService.shutdown();
    }

    public static Flowable<SSE> stream(Call<ResponseBody> apiCall) {
        return stream(apiCall, false);
    }

    public static Flowable<SSE> stream(Call<ResponseBody> apiCall, boolean emitDone) {
        return Flowable.create(emitter -> apiCall.enqueue(new ResponseBodyCallback(emitter, emitDone)),
            BackpressureStrategy.BUFFER);
    }

    public static <T> Flowable<T> stream(Call<ResponseBody> apiCall, Class<T> cl) {
        Class<?> serviceClass = callerClass.get();
        return stream(apiCall).map(sse -> JacksonUtils.getServiceMapper(serviceClass).readValue(sse.getData(), cl));
    }

    public static Flowable<String> streamText(Call<ResponseBody> apiCall) {
        return stream(apiCall).map(sse -> sse.getData());
    }

    public static <T> T execute(Single<T> apiCall) {
        try {
            return apiCall.blockingGet();
        } catch (HttpException e) {
            if (e.response() == null || e.response().errorBody() == null) {
                throw e;
            }
            BufferedSource source = e.response().errorBody().source();
            String msg;
            try {
                msg = source.readUtf8();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            log.error(msg);
            throw new RuntimeException(msg);
        } catch (Throwable e) {
            log.error("unknown error", e);
            throw new RuntimeException(e);
        }
    }

    public OkHttpClient defaultClient(Duration timeout, Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .proxy(Proxy.NO_PROXY)
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.SECONDS))
            .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        // contribute by dapeng.fdp
        if (proxy != null) {
            builder.proxy(proxy);
        }
        if (isDebug()) {
            // 添加日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        if (authentication) {
            builder.addInterceptor(new AuthenticationInterceptor(token));
        }
        return builder.build();
    }

    public Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
    }
}

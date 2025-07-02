package com.alibaba.langengine.docloader.yuque.service;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * OkHttp Interceptor that adds an authorization token header
 *
 * @author xiaoxuan.lp
 */
public class AuthenticationInterceptor implements Interceptor {

    private final String token;

    AuthenticationInterceptor(String token) {
        Objects.requireNonNull(token, "FastChat token required");
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("User-Agent", "top")
                .header("X-Auth-Token", token)
                .build();
        return chain.proceed(request);
    }
}

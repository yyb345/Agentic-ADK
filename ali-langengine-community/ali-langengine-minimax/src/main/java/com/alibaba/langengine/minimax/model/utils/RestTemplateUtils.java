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
package com.alibaba.langengine.minimax.model.utils;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author aihe.ah
 * @time 2023/10/9
 * 功能说明：
 */
@Slf4j
public class RestTemplateUtils {
    private static RestTemplate restTemplate;

    private RestTemplateUtils() {
        // prevent instantiation
    }

    public static RestTemplate getInstance() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate(getClientHttpRequestFactory());
        }
        return restTemplate;
    }

    /**
     * 发送POST请求，参数为任意对象
     *
     * @param url    请求URL
     * @param params 请求参数
     * @return 响应体
     */
    public static String post(String url, Object params) {
        return execute(url, HttpMethod.POST, params);
    }

    /**
     * 发送GET请求
     *
     * @param url 请求URL
     * @return 响应体
     */
    public static String get(String url) {
        return execute(url, HttpMethod.GET, null);
    }

    /**
     * 发送DELETE请求
     *
     * @param url 请求URL
     * @return 响应体
     */
    public static String delete(String url) {
        return execute(url, HttpMethod.DELETE, null);
    }

    /**
     * 执行HTTP请求。
     *
     * @param url    请求URL
     * @param method HTTP方法
     * @param body   请求体
     * @return 响应体
     */
    private static String execute(String url, HttpMethod method, Object body) {
        return execute(url, new HttpHeaders(), method, body, null);
    }

    /**
     * 执行HTTP请求，允许自定义HTTP头。
     *
     * @param url     请求URL
     * @param headers 自定义HTTP头
     * @param method  HTTP方法
     * @param body    请求体
     * @return 响应体
     */
    public static String execute(String url, HttpHeaders headers, HttpMethod method, Object body) {
        return execute(url, headers, method, body, null);
    }

    /**
     * 执行HTTP请求，允许自定义HTTP头和媒体类型。
     *
     * @param url       请求URL
     * @param headers   自定义HTTP头
     * @param method    HTTP方法
     * @param body      请求体
     * @param mediaType 媒体类型
     * @return 响应体
     */
    public static String execute(String url, HttpHeaders headers, HttpMethod method, Object body, MediaType mediaType) {
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            headers.add("Content-Type", "application/json");
        }
        if (mediaType != null) {
            headers.setContentType(mediaType);
        }

        HttpEntity<String> entity = new HttpEntity<>(body != null ? JSON.toJSONString(body) : null, headers);

        try {
            ResponseEntity<String> response = getInstance().exchange(url, method, entity, String.class);
            log.info("Received response for {} request to URL: {} with body: {}", method, url, response.getBody());
            return response.getBody();
        } catch (HttpServerErrorException serverErrorException) {
            log.error("Error occurred while sending {} request to URL: {}. Error: {}", method, url,
                serverErrorException.getMessage());
            throw new RuntimeException(serverErrorException.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error occurred while sending {} request to URL: {}. Error: {}", method, url, e.getMessage());
            return null;
        }
    }

    /**
     * 获取ClientHttpRequestFactory
     */
    private static ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 120000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

}

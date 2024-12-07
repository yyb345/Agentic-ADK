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
package com.alibaba.langengine.tool.google.service;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Google Web Search Api
 *
 * @author xiaoxuan.lp
 */
public interface GoogleApi {

    /**
     * customsearch
     * 参数参考：https://developers.google.com/custom-search/v1/introduction?apix=true&hl=zh-cn
     *
     * @param q
     * @param num
     * @param cx
     * @param key
     * @return
     */
    @GET("/customsearch/v1")
    Single<Map<String, Object>> customSearch(@Query("q") String q,
                                             @Query("num") Integer num,
                                             @Query("cx") String cx,
                                             @Query("key") String key);
}
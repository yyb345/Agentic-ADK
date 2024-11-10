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
package com.alibaba.langengine.tool.bing.service;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Bing Web Search Api
 *
 * @author xiaoxuan.lp
 */
public interface BingApi {

    /**
     * 参数参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-web-search/reference/query-parameters
     *
     * @param q
     * @param textDecorations
     * @param textFormat
     * @param count
     * @return
     */
    @GET("/v7.0/search")
    Single<Map<String, Object>> webSearch(@Query("q") String q,
                                          @Query("textDecorations") Boolean textDecorations,
                                          @Query("textFormat") String textFormat,
                                          @Query("count") Integer count);

    /**
     * 参数参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-image-search/reference/query-parameters
     *
     * @param q
     * @param license
     * @param imageType
     * @param count
     * @return
     */
    @GET("/v7.0/images/search")
    Single<Map<String, Object>> imageSearch(@Query("q") String q,
                                            @Query("license") String license,
                                            @Query("imageType") String imageType,
                                            @Query("count") Integer count);

    /**
     * 参数参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-autosuggest/reference/query-parameters
     *
     * @param q
     * @param mkt
     * @return
     */
    @GET("/v7.0/Suggestions")
    Single<Map<String, Object>> suggestion(@Query("q") String q,
                                           @Query("mkt") String mkt);

    /**
     * 参数参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-spell-check/reference/query-parameters
     *
     * @param text
     * @param mkt
     * @param mode
     * @return
     */
    @GET("/v7.0/SpellCheck")
    Single<Map<String, Object>> spellCheck(@Query("text") String text,
                                           @Query("mkt") String mkt,
                                           @Query("mode") String mode);

    /**
     * 参数参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-entity-search/reference/query-parameters
     *
     * @param q
     * @param mkt
     * @return
     */
    @GET("/v7.0/entities")
    Single<Map<String, Object>> entitySearch(@Query("q") String q,
                                             @Query("mkt") String mkt);

    /**
     * 参数参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-video-search/reference/query-parameters
     *
     * @param q
     * @param pricing
     * @param videoLength
     * @param count
     * @return
     */
    @GET("/v7.0/videos/search")
    Single<Map<String, Object>> videoSearch(@Query("q") String q,
                                            @Query("pricing") String pricing,
                                            @Query("videoLength") String videoLength,
                                            @Query("count") Integer count);
}
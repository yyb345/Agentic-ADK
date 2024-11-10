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
package com.alibaba.langengine.tool;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 *  tool configuration
 *
 * @author xiaoxuan.lp
 */
public class ToolConfiguration {

    /**
     * openapi server url
     */
    public static String OPENAI_SERVER_URL = "https://api.openai.com/";

    /**
     * openai api key
     */
    public static String OPENAI_API_KEY = WorkPropertiesUtils.get("openai_api_key");

    /**
     * openai api timeout
     */
    public static String OPENAI_AI_TIMEOUT = WorkPropertiesUtils.get("openai_api_timeout", 100L);

    /**
     * bing server url
     */
    public static String BING_SERVER_URL = WorkPropertiesUtils.get("bing_server_url");

    /**
     * bing api key
     */
    public static String BING_API_KEY = WorkPropertiesUtils.get("bing_api_key");

    /**
     * google customsearch server url
     */
    public static String GOOGLE_CUSTOMSEARCH_SERVER_URL = WorkPropertiesUtils.get("google_customsearch_server_url");

    /**
     * google api key
     */
    public static String GOOGLE_API_KEY = WorkPropertiesUtils.get("google_api_key");

    /**
     * google cse id
     *
     * 参考：在Programmable Search Enginge (https://programmablesearchengine.google.com/controlpanel/create)中获取GOOGLE_CSE_ID
     */
    public static String GOOGLE_CSE_ID = WorkPropertiesUtils.get("google_cse_id");

    /**
     * serpapi server url
     */
    public static String SERPAPI_SERVER_URL = WorkPropertiesUtils.get("serpapi_server_url");

    /**
     * serpapi api key
     */
    public static String SERPAPI_KEY = WorkPropertiesUtils.get("serpapi_key");

    /**
     * serpapi api key
     */
    public static String TAVILY_API_KEY = WorkPropertiesUtils.get("tavily_api_key");
}

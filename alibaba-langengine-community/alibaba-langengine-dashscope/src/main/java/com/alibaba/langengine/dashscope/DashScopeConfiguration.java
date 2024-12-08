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
package com.alibaba.langengine.dashscope;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class DashScopeConfiguration {

    /**
     * dashscope api key
     */
    public static String DASHSCOPE_API_KEY = WorkPropertiesUtils.getFirstAvailable("dashscope_api_key", "DASHSCOPE_API_KEY");

    /**
     * datascope server url
     */
    public static String DASHSCOPE_SERVER_URL = WorkPropertiesUtils.get("dashscope_server_url");

    /**
     * dashscope openai compatible server url
     */
    public static String DASHSCOPE_OPENAI_COMPATIBLE_SERVER_URL = WorkPropertiesUtils.get("dashscope_openai_compatible_server_url");

    public static String DASHSCOPE_API_TIMEOUT = WorkPropertiesUtils.get("dashscope_api_timeout", 120l);
}

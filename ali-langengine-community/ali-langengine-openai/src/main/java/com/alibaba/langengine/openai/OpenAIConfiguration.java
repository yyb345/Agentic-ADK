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
package com.alibaba.langengine.openai;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * model configuration
 *
 * @author xiaoxuan.lp
 */
public class OpenAIConfiguration {

    /**
     * openapi server url
     */
    public static String OPENAI_SERVER_URL = WorkPropertiesUtils.get("openai_server_url");

    /**
     * openai api key
     * openai_api_key是配置在系统的配置文件中
     * OPENAI_API_KEY是业内的通用配置，在环境变量中
     */
    public static String OPENAI_API_KEY = WorkPropertiesUtils.getFirstAvailable("openai_api_key","OPENAI_API_KEY");

    /**
     * openai api timeout
     */
    public static String OPENAI_AI_TIMEOUT = WorkPropertiesUtils.get("openai_api_timeout", 120L);
}

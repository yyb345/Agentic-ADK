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
package com.alibaba.langengine.azure;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class AzureConfiguration {

    /**
     * openapi server url
     */
    public static String AZURE_OPENAI_SERVER_URL = WorkPropertiesUtils.get("azure_openai_server_url");

    /**
     * openai api key
     * openai_api_key是配置在系统的配置文件中
     * OPENAI_API_KEY是业内的通用配置，在环境变量中
     */
    public static String AZURE_OPENAI_API_KEY = WorkPropertiesUtils.getFirstAvailable("azure_openai_api_key", "OPENAI_API_KEY");

    /**
     * azure openai api timeout
     */
    public static String AZURE_OPENAI_AI_TIMEOUT = WorkPropertiesUtils.get("azure_openai_api_timeout", 100L);

    public static String AZURE_OPENAI_API_VERSION = WorkPropertiesUtils.get("azure_openai_api_version");

    public static String AZURE_DEPLOYMENT_NAME = WorkPropertiesUtils.get("azure_deployment_name");
}

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
package com.alibaba.langengine.vertexai;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class VertexaiConfiguration {

    public static String VERTEXAI_SERVER_URL = WorkPropertiesUtils.get("vertexai_server_url");

    /**
     * vertexai api key
     */
    public static String VERTEXAI_API_KEY = WorkPropertiesUtils.get("vertexai_api_key");

    /**
     * vertexai project id
     */
    public static String VERTEXAI_PROJECT_ID = WorkPropertiesUtils.get("vertexai_project_id");

    /**
     * GEMINI api timeout
     */
    public static String VERTEXAI_API_TIMEOUT = WorkPropertiesUtils.get("vertexai_api_timeout", 100L);
}

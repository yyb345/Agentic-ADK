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
package com.alibaba.langengine.claude;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class ClaudeConfiguration {

    /**
     * claude server url
     */
    public static String ANTHROPIC_SERVER_URL = WorkPropertiesUtils.get("anthropic_server_url", "https://api.anthropic.com/");

    /**
     * claude api key
     */
    public static String ANTHROPIC_API_KEY = WorkPropertiesUtils.getFirstAvailable("anthropic_api_key", "ANTHROPIC_API_KEY");

    /**
     * claude api timeout
     */
    public static String ANTHROPIC_API_TIMEOUT = WorkPropertiesUtils.get("anthropic_api_timeout", 120L);
}

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
package com.alibaba.langengine.moonshot.model;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class Config {
    public static String MOONSHOT_API_VERSION = WorkPropertiesUtils.get("moonshot_api_version","v1");

    public static String MOONSHOT_SERVER_URL =  WorkPropertiesUtils.get("moonshot_server_url", "https://api.moonshot.cn");

    public static String MOONSHOT_SERVER_TIMEOUT = WorkPropertiesUtils.get("moonshot_server_timeout", 300L);

    public static String MOONSHOT_API_KEY = WorkPropertiesUtils.get("moonshot_api_key");
}

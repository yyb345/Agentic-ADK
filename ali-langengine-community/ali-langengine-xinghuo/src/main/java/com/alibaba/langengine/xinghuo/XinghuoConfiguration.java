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
package com.alibaba.langengine.xinghuo;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class XinghuoConfiguration {

    /**
     * 星火-server url
     */
    public static String XINGHUO_SERVER_URL = WorkPropertiesUtils.get("xinghuo_server_url");

    /**
     * 星火-app id
     */
    public static String XINGHUO_APP_ID = WorkPropertiesUtils.get("xinghuo_app_id");

    /**
     * 星火-apikey
     */
    public static String XINGHUO_API_KEY = WorkPropertiesUtils.get("xinghuo_api_key");

    /**
     * 星火-apisecret
     */
    public static String XINGHUO_API_SECRET = WorkPropertiesUtils.get("xinghuo_api_secret");
}

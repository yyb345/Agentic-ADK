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
package com.alibaba.langengine.redis.memory;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * redis configuration
 *
 * @author xiaoxuan.lp
 */
public class RedisConfiguration {

    /**
     * redis(公有云tair) host
     */
    public static String REDIS_HOST = WorkPropertiesUtils.get("redis_host");

    /**
     * redis(公有云tair) port
     */
    public static String REDIS_PORT = WorkPropertiesUtils.get("redis_port");

    /**
     * redis(公有云tair) p
     */
    public static String REDIS_P = WorkPropertiesUtils.get("redis_p");

    /**
     * 多轮会话缓存时间
     */
    public static String REDIS_SESSION_EXPIRE_TIME_SECONDS = WorkPropertiesUtils.get("redis_session_expire_second");
}

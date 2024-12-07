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
package com.alibaba.langengine.tair.vectorstore;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * vectorstore configuration
 *
 * @author xiaoxuan.lp
 */
public class TairVectorstoreConfiguration {

    /**
     * tair host
     */
    public static String TAIR_HOST = WorkPropertiesUtils.get("tair_host");

    /**
     * tair port
     */
    public static String TAIR_PORT = WorkPropertiesUtils.get("tair_port");

    /**
     * tair p
     */
    public static String TAIR_P = WorkPropertiesUtils.get("tair_p");
}

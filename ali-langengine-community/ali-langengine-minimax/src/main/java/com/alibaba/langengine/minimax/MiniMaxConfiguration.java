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
package com.alibaba.langengine.minimax;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class MiniMaxConfiguration {

    /**
     * minimax api key
     */
    public static String MINIMAX_API_KEY = WorkPropertiesUtils.getFirstAvailable("minimax_api_key", "MINIMAX_API_KEY");

    /**
     * minimax group id
     */
    public static String MINIMAX_GROUP_ID = WorkPropertiesUtils.getFirstAvailable("minimax_group_id", "MINIMAX_GROUP_ID");

    /**
     * minimax api timeout
     */
    public static String MINIMAX_API_TIMEOUT = WorkPropertiesUtils.get("minimax_api_timeout", 100L);
}

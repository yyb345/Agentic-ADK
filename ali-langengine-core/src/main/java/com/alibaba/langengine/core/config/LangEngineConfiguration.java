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
package com.alibaba.langengine.core.config;

import com.alibaba.langengine.core.caches.BaseCache;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.util.NullAwareBeanUtilsBean;
import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * LangeEngine configuration
 *
 * @author xiaoxuan.lp
 */
public class LangEngineConfiguration {

    /**
     * qa recommend num
     */
    public static String RETRIEVAL_QA_RECOMMEND_COUNT = WorkPropertiesUtils.get("retrieval_qa_recommend_count", "2");

    public static BaseCache CurrentCache;

    /**
     * 全局callback manager
     */
    public static BaseCallbackManager CALLBACK_MANAGER = new CallbackManager();

    public static NullAwareBeanUtilsBean NULL_AWARE_BEAN_UTILS_BEAN = new NullAwareBeanUtilsBean();
}

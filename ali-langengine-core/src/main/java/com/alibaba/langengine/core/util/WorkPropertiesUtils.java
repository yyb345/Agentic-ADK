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
package com.alibaba.langengine.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 工作CLASSES属性辅助工具
 *
 * @author xiaoxuan.lp
 */
public class WorkPropertiesUtils {

    private static final PropertiesUtils PROPERTIES_UTILS = new PropertiesUtils(Constants.PROPERTIES_FILE_NAME);
    private static final PropertiesUtils APPLICATION_PROPERTIES_UTILS = new PropertiesUtils(Constants.APPLICATION_PROPERTIES_FILE_NAME);
    private static final PropertiesUtils APPLICATION_PRODUCTION_PROPERTIES_UTILS = new PropertiesUtils(Constants.APPLICATION_PRODUCTION_PROPERTIES_FILE_NAME);
    private static final PropertiesUtils APPLICATION_STAGING_PROPERTIES_UTILS = new PropertiesUtils(Constants.APPLICATION_STAGING_PROPERTIES_FILE_NAME);
    private static final PropertiesUtils APPLICATION_TESTING_PROPERTIES_UTILS = new PropertiesUtils(Constants.APPLICATION_TESTING_PROPERTIES_FILE_NAME);
    private static final PropertiesUtils APPLICATION_LOCAL_PROPERTIES_UTILS = new PropertiesUtils(Constants.APPLICATION_LOCAL_PROPERTIES_FILE_NAME);





    /**
     * 是否从环境变量中获取，将敏感信息（如API密钥）存储在配置文件中可能会导致泄露风险。
     * 为了降低这种风险，可以首先从环境变量中读取这些敏感信息。如果在环境变量中找不到对应的键，则可以从配置文件中读取。
     */
    private static final boolean FETCH_FROM_ENV = Boolean.parseBoolean(get("FETCH_FROM_ENV", "true"));

    /**
     * 通过key获取值
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * 通过key获取值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String get(String key, Object defaultValue) {

        String value = null;

        if (FETCH_FROM_ENV) {
            // 优先从系统环境变量中获取值
            value = System.getenv(key);
        }

        if (StringUtils.isEmpty(value)) {
            // 如果系统环境变量中找不到，再从框架配置文件中获取
            value = PROPERTIES_UTILS.get(key);
        }

        if(StringUtils.isEmpty(value)) {
            // 如果框架配置文件中找不到，再从环境应用配置文件中获取
            String profile = System.getProperty("spring.profiles.active");
            if(!StringUtils.isEmpty(profile)) {
                if("local".equals(profile)) {
                    value = APPLICATION_LOCAL_PROPERTIES_UTILS.get(key);
                } else if("testing".equals(profile)) {
                    value = APPLICATION_TESTING_PROPERTIES_UTILS.get(key);
                } else if("staging".equals(profile)) {
                    value = APPLICATION_STAGING_PROPERTIES_UTILS.get(key);
                } else if("production".equals(profile)) {
                    value = APPLICATION_PRODUCTION_PROPERTIES_UTILS.get(key);
                }
            }
        }

        if(StringUtils.isEmpty(value)) {
            // 如果环境应用框架配置文件中找不到，再从统一应用配置文件中获取
            value = APPLICATION_PROPERTIES_UTILS.get(key);
        }

        if (StringUtils.isEmpty(value)) {
            value = (defaultValue != null ? defaultValue.toString() : "");
        }

        return value;
    }

    /**
     * 获取第一个不为空的键值，如果都为空则返回null
     * 比如OpenAPI_key，系统中的key定义和当前业内通用的key定义不相同，这个时候可以考虑有限业务内定义，然后再从业内的定义中取
     *
     * @param keys 可变的键参数
     * @return 第一个不为空的键值，如果都为空则返回null
     */
    public static String getFirstAvailable(String... keys) {
        for (String key : keys) {
            String value = get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }
}

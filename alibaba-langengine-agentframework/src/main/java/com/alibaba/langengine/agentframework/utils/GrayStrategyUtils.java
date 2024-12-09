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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 通用灰度策略工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class GrayStrategyUtils {

    private static final String NOTVELOCITY_KEY = "notVelocity";
    private static final String ALLNOTVELOCITY_KEY = "allNotVelocity";
    private static final String DYNAMICSCRIPTWHITELIST_KEY = "dynamicScriptWhiteList";
    private static final String ALLDYNAMICSCRIPTWHITELIST_KEY = "allDynamicScriptWhiteList";

    public static boolean staifyGrayNotVelocity(String grayStrategyConfig, String value) {
        return satisfyGray(grayStrategyConfig, NOTVELOCITY_KEY, value);
    }

    public static boolean satisfyGrayDynamicScriptWhiteList(String grayStrategyConfig, String value) {
        return satisfyGray(grayStrategyConfig, DYNAMICSCRIPTWHITELIST_KEY, value);
    }
    
    public static boolean staifyAllNotVelocity(String grayStrategyConfig) {
        return satisfyAll(grayStrategyConfig, ALLNOTVELOCITY_KEY);
    }

    public static boolean satisfyAllDynamicScriptWhiteList(String grayStrategyConfig) {
        return satisfyAll(grayStrategyConfig, ALLDYNAMICSCRIPTWHITELIST_KEY);
    }

    public static boolean satisfyAll(String grayStrategyConfig, String key) {
        if(StringUtils.isEmpty(grayStrategyConfig)) {
            return false;
        }
        try {
            Map<String, Object> grayStrategyMap = JSON.parseObject(grayStrategyConfig, new TypeReference<Map<String, Object>>() {
            });
            if(grayStrategyMap == null) {
                return false;
            }
            if(grayStrategyMap.get(key) == null) {
                return false;
            }
            return (Boolean)grayStrategyMap.get(key);
        } catch (Throwable throwable) {
            log.error("staifyAllNotVelocity parse object error", throwable);
            return false;
        }
    }

    /**
     * 是否满足灰度策略
     *
     * @param grayStrategyConfig
     * @param strategyKey
     * @param value
     * @return
     */
    public static boolean satisfyGray(String grayStrategyConfig, String strategyKey, String value) {
        if(StringUtils.isEmpty(grayStrategyConfig)) {
            return false;
        }
        try {
            Map<String, Object> grayStrategyMap = JSON.parseObject(grayStrategyConfig, new TypeReference<Map<String, Object>>() {
            });
            if(grayStrategyMap == null) {
                return false;
            }
            if(grayStrategyMap.get(strategyKey) == null) {
                return false;
            }
            List<String> list = (List<String>)grayStrategyMap.get(strategyKey);
            if (!list.contains(value)) {
                return false;
            }
            return true;
        } catch (Throwable throwable) {
            log.error("satisfyGray parse object error", throwable);
            return false;
        }
    }
}

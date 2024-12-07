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

import lombok.extern.slf4j.Slf4j;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Properties辅助工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class PropertiesUtils {

    private ResourceBundle res;

    public PropertiesUtils(String bundleName) {
        try {
            res = ResourceBundle.getBundle(bundleName);
        } catch (Exception e) {
//            log.warn(String.format("not load bundleName=%s", bundleName), e);
//            throw new RuntimeException("The properties-file<" + bundleName + "> maybe not exist!", e);
        }
    }

    public String get(String key) {
        try {
            if(res == null) {
                return null;
            }
            if(!res.containsKey(key)) {
                return null;
            }
            String value = res.getString(key);
            return value;
        }
        catch (Exception e) {
            log.error(String.format("get key=%s error", key), e);
            return null;
        }
    }

    public PropertyResourceBundle getBundle() {
        return (PropertyResourceBundle) res;
    }
}

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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * JsonUtils
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class JsonUtils {

    /**
     * 从结果中过滤掉好的 JSON，以防存在其他文本
     *
     * @param input
     * @return
     */
    public static String extractJson(String input) {
        int startIndex = input.indexOf("{");
        int endIndex = input.lastIndexOf("}");
        if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) {
            String jsonString = input.substring(startIndex, endIndex + 1);
            try {
                JSON.parse(jsonString);
                return jsonString;
            } catch (Throwable e) {
                return null;
            }
        }
        return null;
    }

    public static Map<String, Object> obj2Map(Object obj) {
        if (obj == null) {
            return new HashMap<>(2);
        }

        try {
            String jsonString = JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue);
            return JSON.parseObject(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("对象转Map失败: " + e.getMessage());
            return null;
        }
    }
}

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * @author aihe.ah
 * @time 2023/10/1
 * 功能说明：
 */
public class MapUtils {

    /**
     * 过滤 Map 中的空值和指定的参数
     *
     * @param params
     * @param requiredParams
     * @return
     */
    public static Map<String, String> filterMap(Map<String, String> params, List<String> requiredParams) {
        Map<String, String> result = new HashMap<>();
        params.forEach((key, value) -> {
            if (requiredParams.contains(key)) {
                result.put(key, value);
            }
        });
        return result;
    }

    /**
     * 把Map按照预期类型进行转换
     *
     * @param input
     * @param requiredParams
     * @return
     */
    public static Map<String, Object> convertMapValues(Map<String, String> input, Map<String, Class> requiredParams) {
        Map<String, Object> output = new HashMap<>();
        for (Map.Entry<String, String> entry : input.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Class type = requiredParams.get(key);
            if (type != null) {
                if (type == Long.class) {
                    output.put(key, Long.valueOf(value));
                } else if (type == Integer.class) {
                    output.put(key, Integer.valueOf(value));
                } else if (type == Boolean.class) {
                    output.put(key, Boolean.valueOf(value));
                } else if (type == String.class) {
                    output.put(key, value);
                }
            }
        }
        return output;
    }

    public static Map<String, Object> convertMapValuesNotFilter(Map<String, String> input,
        Map<String, String> requiredParams) throws ClassNotFoundException {
        if (requiredParams == null) {
            requiredParams = new HashMap<>(1);
        }
        Map<String, Object> output = new HashMap<>();
        for (Map.Entry<String, String> entry : input.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String type = requiredParams.get(key);
            if (type != null) {
                Class<?> typeClass = Class.forName(type);
                if (typeClass.equals(Long.class)) {
                    output.put(key, Long.valueOf(value));
                } else if (typeClass.equals(Integer.class)) {
                    output.put(key, Integer.valueOf(value));
                } else if (typeClass.equals(Boolean.class)) {
                    output.put(key, Boolean.valueOf(value));
                } else if (typeClass.equals(String.class)) {
                    output.put(key, value);
                }
            } else {
                output.put(key, value);
            }
        }
        return output;
    }

    /**
     * 过滤，并且进行转换
     */
    public static Map<String, Object> filterAndConvertMapValues(Map<String, String> input,
        Map<String, Class> requiredParams) {
        return convertMapValues(filterMap(input, Lists.newArrayList(requiredParams.keySet())), requiredParams);
    }

    /**
     * 合并两个 Map 并将结果转换为 Map<String, String> 的格式
     * 后面的Map会覆盖前面的Map的Key
     *
     * @param maps 多个Map的集合
     * @return 合并后的 Map，其键和值都为 String 类型。
     */
    public static Map<String, String> mergeAndConvertToStringMap(Map<?, ?>... maps) {
        Map<String, String> result = new HashMap<>();

        for (Map<?, ?> map : maps) {
            if (map != null) {
                map.forEach((key, value) -> result.put(String.valueOf(key), String.valueOf(value)));
            }
        }

        return result;
    }

    /**
     * 仅仅合并，但是不转换为字符串
     */
    public static Map<String, Object> merge(Map<String, Object>... maps) {
        Map<String, Object> result = new HashMap<>();

        for (Map<String, Object> map : maps) {
            if (map != null) {
                result.putAll(map);
            }
        }

        return result;
    }

    /**
     * 获取某个Key的值，加个默认值
     */
    public static String get(Map<String, String> map, String key, String defaultValue) {
        String value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 从existingMap中的key复制出来，新增一个Key
     *
     * @param existingMap
     * @param newKeys
     * @param <K>
     * @param <V>
     */
    public static <K, V> void copyKeys(Map<K, V> existingMap, Map<K, K> newKeys) {
        for (Map.Entry<K, K> entry : newKeys.entrySet()) {
            K oldKey = entry.getKey();
            K newKey = entry.getValue();
            if (existingMap.containsKey(oldKey)) {
                V value = existingMap.get(oldKey);
                existingMap.put(newKey, value);
            }
        }
    }

    /**
     * 把Map转换为String
     */
    public static String mapToString(Map<String, String> map) {
        return map.entrySet()
            .stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .collect(Collectors.joining("\n"));
    }
}

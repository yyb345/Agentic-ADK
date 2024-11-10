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

import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * jackson辅助工具
 *
 * @author xiaoxuan.lp
 */
public class JacksonUtils {

    /**
     * 针对不同的Service，应该要有一个不同的Mapper
     */

    private static final ConcurrentHashMap mapperMap = new ConcurrentHashMap<Class, ObjectMapper>(4);

    /**
     * 注册服务对应的Mapper
     */
    public static void registerMapper(Class clazz, ObjectMapper mapper) {
        mapperMap.put(clazz, mapper);
    }

    /**
     * 获取服务对应的Mapper
     */
    public static ObjectMapper getMapper(Class clazz) {
        return (ObjectMapper)mapperMap.get(clazz);
    }

    /**
     * 类字段key
     */
    public static final String PROPERTY_CLASS_NAME = "className";

    public static final ObjectMapper API_SERVICE_MAPPER = defaultObjectMapper();

    /**
     * mapper对象
     */
    public static ObjectMapper MAPPER;

    static {
        ObjectMapper _mapper = new ObjectMapper();
        _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        _mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        _mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
                _mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        MAPPER = _mapper;
    }

    /**
     * 获取服务对应的Mapper
     *
     * @param clazz
     * @return
     */
    public static ObjectMapper getServiceMapper(Class clazz) {
        if (clazz == null) {
            return API_SERVICE_MAPPER;
        }
        ObjectMapper mapper = (ObjectMapper)mapperMap.get(clazz);
        if (mapper == null) {
            mapper = defaultObjectMapper();
            mapperMap.put(clazz, mapper);
        }
        return mapper;
    }

    public static ObjectMapper defaultObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        return mapper;
    }

}

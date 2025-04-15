/*
 * Copyright 2025 Alibaba Group Holding Ltd.
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
package com.alibaba.langengine.modelcontextprotocol.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * MCP ObjectMapper 工具类
 * 提供专门配置的 ObjectMapper 实例，用于处理 MCP 对象的序列化和反序列化
 * 
 * @author aihe.ah
 * @date 2025/4/3
 */
public class McpObjectMapperUtil {
    
    // 单例实例
    private static ObjectMapper INSTANCE;
    
    /**
     * 获取配置好的 MCP ObjectMapper 实例
     * 
     * @return 配置好的 ObjectMapper 实例
     */
    public static synchronized ObjectMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = createMcpObjectMapper();
        }
        return INSTANCE;
    }
    
    /**
     * 创建并配置 MCP ObjectMapper
     * 
     * @return 配置好的 ObjectMapper 实例
     */
    private static ObjectMapper createMcpObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 配置可见性，使其能够处理没有标准 getter 前缀的方法
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        // 允许序列化空对象
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // 忽略未知属性，避免反序列化失败
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        

        // 不使用科学计数法表示数字
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        
        // 允许单值作为数组
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        return objectMapper;
    }
    
    /**
     * 配置现有的 ObjectMapper 实例，使其适用于 MCP 对象
     * 
     * @param objectMapper 要配置的 ObjectMapper 实例
     * @return 配置好的 ObjectMapper 实例
     */
    public static ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
        // 创建 ObjectMapper 的副本，避免修改原始实例
        ObjectMapper configuredMapper = objectMapper.copy();
        
        // 配置可见性，使其能够处理没有标准 getter 前缀的方法
        configuredMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        configuredMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        // 允许序列化空对象
        configuredMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // 忽略未知属性，避免反序列化失败
        configuredMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 不使用科学计数法表示数字
        configuredMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        
        // 允许单值作为数组
        configuredMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        return configuredMapper;
    }
    
    /**
     * 将对象转换为 JSON 字符串
     * 
     * @param object 要转换的对象
     * @return JSON 字符串
     * @throws Exception 如果转换失败
     */
    public static String toJson(Object object) throws Exception {
        return getInstance().writeValueAsString(object);
    }
    
    /**
     * 将 JSON 字符串转换为对象
     * 
     * @param <T> 目标类型
     * @param json JSON 字符串
     * @param clazz 目标类
     * @return 转换后的对象
     * @throws Exception 如果转换失败
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return getInstance().readValue(json, clazz);
    }
}

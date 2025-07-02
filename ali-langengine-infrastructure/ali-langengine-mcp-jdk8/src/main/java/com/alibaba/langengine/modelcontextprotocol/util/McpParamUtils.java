package com.alibaba.langengine.modelcontextprotocol.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.modelcontextprotocol.spec.JsonSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP参数处理工具类
 * 用于处理MCP调用参数的类型转换等操作
 *
 * @author aihe.ah
 */
@Slf4j
public class McpParamUtils {

    /**
     * 预处理输入参数，根据Schema定义转换参数类型
     *
     * @param inputParams 输入参数
     * @param schema      Schema定义
     * @return 处理后的参数
     */
    public static Map<String, Object> preProcessInputParams(Map<String, Object> inputParams, JsonSchema schema) {
        if (inputParams == null || schema == null) {
            return inputParams;
        }

        try {
            Map<String, Object> processedParams = new HashMap<>(inputParams);
            JSONObject properties = JSON.parseObject(JSON.toJSONString(schema.properties()));

            if (properties == null || properties.isEmpty()) {
                return processedParams;
            }

            // 遍历所有属性进行类型转换
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                if (!processedParams.containsKey(propertyName)) {
                    continue;
                }

                Object propertySchema = entry.getValue();
                if (!(propertySchema instanceof JSONObject)) {
                    continue;
                }

                JSONObject propertyConfig = (JSONObject) propertySchema;
                String expectedType = propertyConfig.getString("type");
                if (StringUtils.isEmpty(expectedType)) {
                    continue;
                }

                Object originalValue = processedParams.get(propertyName);
                Object convertedValue = convertValueToType(originalValue, expectedType, propertyName);
                if (convertedValue != null) {
                    processedParams.put(propertyName, convertedValue);
                }
            }

            return processedParams;
        } catch (Exception e) {
            log.warn("参数预处理异常，将使用原始参数, error: {}", e.getMessage());
            return inputParams;
        }
    }

    /**
     * 将值转换为指定类型
     *
     * @param value        原始值
     * @param targetType   目标类型
     * @param propertyName 属性名（用于日志）
     * @return 转换后的值
     */
    private static Object convertValueToType(Object value, String targetType, String propertyName) {
        if (value == null) {
            return null;
        }

        try {
            switch (targetType.toLowerCase()) {
                case "string":
                    if (!(value instanceof String)) {
                        String convertedString = String.valueOf(value);
                        log.info("参数[{}]类型转换: {} -> string: {}", propertyName, value.getClass().getSimpleName(), convertedString);
                        return convertedString;
                    }
                    break;

                case "number":
                case "integer":
                    if (value instanceof String) {
                        try {
                            // 先尝试转换为Double
                            Double doubleValue = Double.parseDouble((String) value);
                            // 如果是整数类型且值是整数
                            if ("integer".equals(targetType) && doubleValue == doubleValue.longValue()) {
                                Long longValue = doubleValue.longValue();
                                log.info("参数[{}]类型转换: string -> integer: {}", propertyName, longValue);
                                return longValue;
                            }
                            log.info("参数[{}]类型转换: string -> number: {}", propertyName, doubleValue);
                            return doubleValue;
                        } catch (NumberFormatException e) {
                            log.warn("参数[{}]数值转换失败: {}", propertyName, value);
                        }
                    }
                    break;

                case "boolean":
                    if (!(value instanceof Boolean)) {
                        if (value instanceof String) {
                            String strValue = ((String) value).toLowerCase();
                            boolean boolValue = "true".equals(strValue) || "1".equals(strValue);
                            log.info("参数[{}]类型转换: string -> boolean: {}", propertyName, boolValue);
                            return boolValue;
                        } else if (value instanceof Number) {
                            boolean boolValue = ((Number) value).intValue() != 0;
                            log.info("参数[{}]类型转换: number -> boolean: {}", propertyName, boolValue);
                            return boolValue;
                        }
                    }
                    break;

                case "array":
                    if (value instanceof String) {
                        try {
                            Object arrayValue = JSONObject.parseArray((String) value);
                            log.info("参数[{}]类型转换: string -> array", propertyName);
                            return arrayValue;
                        } catch (Exception e) {
                            log.warn("参数[{}]数组转换失败: {}", propertyName, value);
                        }
                    }
                    break;

                case "object":
                    if (value instanceof String) {
                        try {
                            Object objValue = JSONObject.parseObject((String) value);
                            log.info("参数[{}]类型转换: string -> object", propertyName);
                            return objValue;
                        } catch (Exception e) {
                            log.warn("参数[{}]对象转换失败: {}", propertyName, value);
                        }
                    }
                    break;

                default:
                    log.warn("参数[{}]未知的目标类型: {}", propertyName, targetType);
            }
        } catch (Exception e) {
            log.warn("参数[{}]类型转换异常: {}", propertyName, e.getMessage());
        }

        return value;
    }
} 
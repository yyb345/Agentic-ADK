package com.alibaba.langengine.jsonrepair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * JsonSafeParser - 安全的JSON解析工具类
 * 
 * 该类提供了一系列方法，用于安全地解析JSON字符串，处理各种异常情况：
 * 1. 首先尝试使用FastJSON直接解析
 * 2. 如果失败，尝试使用JsonRepair修复后再解析
 * 3. 处理可能被错误解析为数组的JSON对象
 * 4. 确保返回有效的JSON字符串
 */
@Slf4j
public class JsonSafeParser {
    
    /**
     * 安全地解析JSON字符串为JSONObject
     * 
     * @param jsonStr 要解析的JSON字符串
     * @return 解析后的JSONObject，如果解析失败则返回空的JSONObject
     */
    public static JSONObject parseObject(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            log.warn("Empty or null JSON string provided for parsing");
            return new JSONObject();
        }
        
        try {
            // 1. 首先尝试直接解析
            return JSON.parseObject(jsonStr);
        } catch (Exception e) {
            log.warn("Failed to parse JSON directly: {}", e.getMessage());
            
            // 2. 尝试修复后解析
            try {
                String repairedJson = JsonRepair.repairJson(jsonStr);
                log.info("JSON repaired. Original length: {}, Repaired length: {}", 
                        jsonStr.length(), repairedJson.length());
                
                // 检查修复后的JSON是否以{开头，确保它是一个对象
                if (repairedJson.trim().startsWith("{")) {
                    try {
                        return JSON.parseObject(repairedJson);
                    } catch (Exception e2) {
                        log.warn("Failed to parse repaired JSON as object: {}", e2.getMessage());
                    }
                }
                
                // 3. 如果修复后的JSON被解析为数组，但原始字符串以{开头，尝试提取第一个元素
                if (jsonStr.trim().startsWith("{")) {
                    try {
                        JSONArray array = JSON.parseArray(repairedJson);
                        if (array != null && !array.isEmpty() && array.get(0) instanceof JSONObject) {
                            log.info("Extracted first object from array");
                            return array.getJSONObject(0);
                        }
                    } catch (Exception e3) {
                        log.warn("Failed to parse repaired JSON as array: {}", e3.getMessage());
                    }
                }
                
                // 4. 最后尝试手动修复常见问题
                try {
                    String manuallyFixedJson = manuallyFixJson(jsonStr);
                    return JSON.parseObject(manuallyFixedJson);
                } catch (Exception e4) {
                    log.error("All parsing attempts failed: {}", e4.getMessage());
                }
            } catch (Exception repairEx) {
                log.error("JSON repair failed: {}", repairEx.getMessage());
            }
        }
        
        // 如果所有尝试都失败，返回空对象
        log.error("Returning empty JSONObject after all parsing attempts failed");
        return new JSONObject();
    }
    
    /**
     * 安全地解析JSON字符串为JSONArray
     * 
     * @param jsonStr 要解析的JSON字符串
     * @return 解析后的JSONArray，如果解析失败则返回空的JSONArray
     */
    public static JSONArray parseArray(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            log.warn("Empty or null JSON string provided for parsing");
            return new JSONArray();
        }
        
        try {
            // 1. 首先尝试直接解析
            return JSON.parseArray(jsonStr);
        } catch (Exception e) {
            log.warn("Failed to parse JSON directly as array: {}", e.getMessage());
            
            // 2. 尝试修复后解析
            try {
                String repairedJson = JsonRepair.repairJson(jsonStr);
                log.info("JSON repaired. Original length: {}, Repaired length: {}", 
                        jsonStr.length(), repairedJson.length());
                
                // 检查修复后的JSON是否以[开头，确保它是一个数组
                if (repairedJson.trim().startsWith("[")) {
                    try {
                        return JSON.parseArray(repairedJson);
                    } catch (Exception e2) {
                        log.warn("Failed to parse repaired JSON as array: {}", e2.getMessage());
                    }
                }
                
                // 3. 如果修复后的JSON被解析为对象，但原始字符串以[开头，尝试将其包装为数组
                if (jsonStr.trim().startsWith("[")) {
                    try {
                        JSONObject obj = JSON.parseObject(repairedJson);
                        if (obj != null) {
                            JSONArray array = new JSONArray();
                            array.add(obj);
                            log.info("Wrapped object in array");
                            return array;
                        }
                    } catch (Exception e3) {
                        log.warn("Failed to parse repaired JSON as object: {}", e3.getMessage());
                    }
                }
                
                // 4. 最后尝试手动修复常见问题
                try {
                    String manuallyFixedJson = manuallyFixJson(jsonStr);
                    if (!manuallyFixedJson.trim().startsWith("[")) {
                        manuallyFixedJson = "[" + manuallyFixedJson + "]";
                    }
                    return JSON.parseArray(manuallyFixedJson);
                } catch (Exception e4) {
                    log.error("All parsing attempts failed: {}", e4.getMessage());
                }
            } catch (Exception repairEx) {
                log.error("JSON repair failed: {}", repairEx.getMessage());
            }
        }
        
        // 如果所有尝试都失败，返回空数组
        log.error("Returning empty JSONArray after all parsing attempts failed");
        return new JSONArray();
    }
    
    /**
     * 安全地解析JSON字符串，返回可用的JSON字符串
     * 
     * @param jsonStr 要解析的JSON字符串
     * @return 解析后的有效JSON字符串
     */
    public static String getValidJsonString(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            log.warn("Empty or null JSON string provided");
            return "{}";
        }
        
        // 记录原始字符串的特征
        boolean startsWithObject = jsonStr.trim().startsWith("{");
        boolean startsWithArray = jsonStr.trim().startsWith("[");
        
        try {
            // 1. 首先尝试直接解析
            Object parsed = JSON.parse(jsonStr);
            // 如果解析成功，返回原始字符串
            log.info("JSON parsed successfully without repair");
            return jsonStr;
        } catch (JSONException e) {
            log.warn("Failed to parse JSON directly: {}", e.getMessage());
            
            // 2. 尝试修复
            try {
                String repairedJson = JsonRepair.repairJson(jsonStr);
                log.info("JSON repaired. Original length: {}, Repaired length: {}", 
                        jsonStr.length(), repairedJson.length());
                
                // 尝试解析修复后的JSON
                try {
                    Object parsed = JSON.parse(repairedJson);
                    
                    // 检查解析结果类型是否与原始字符串的开头符号一致
                    if (startsWithObject && parsed instanceof JSONArray) {
                        log.warn("Original JSON starts with '{' but was parsed as array");
                        // 尝试提取第一个元素
                        JSONArray array = (JSONArray) parsed;
                        if (!array.isEmpty() && array.get(0) instanceof JSONObject) {
                            log.info("Extracted first object from array");
                            return array.getJSONObject(0).toJSONString();
                        } else {
                            // 强制将结果转换为对象
                            log.info("Forcing result to be an object");
                            JSONObject forcedObject = new JSONObject();
                            forcedObject.put("data", parsed);
                            return forcedObject.toJSONString();
                        }
                    } else if (startsWithArray && parsed instanceof JSONObject) {
                        log.warn("Original JSON starts with '[' but was parsed as object");
                        // 强制将结果转换为数组
                        log.info("Forcing result to be an array");
                        JSONArray forcedArray = new JSONArray();
                        forcedArray.add(parsed);
                        return forcedArray.toJSONString();
                    }
                    
                    // 如果类型一致，返回修复后的JSON
                    return repairedJson;
                } catch (Exception parseEx) {
                    log.warn("Failed to parse repaired JSON: {}", parseEx.getMessage());
                }
                
                // 3. 尝试手动修复
                String manuallyFixedJson = manuallyFixJson(jsonStr);
                try {
                    JSON.parse(manuallyFixedJson);
                    log.info("Manually fixed JSON parsed successfully");
                    return manuallyFixedJson;
                } catch (Exception e3) {
                    log.error("All parsing attempts failed: {}", e3.getMessage());
                }
            } catch (Exception repairEx) {
                log.error("JSON repair failed: {}", repairEx.getMessage());
            }
        }
        
        // 如果所有尝试都失败，返回一个基于原始字符串开头的空对象或数组
        if (startsWithArray) {
            log.error("Returning empty array after all parsing attempts failed");
            return "[]";
        } else {
            log.error("Returning empty object after all parsing attempts failed");
            return "{}";
        }
    }
    
    /**
     * 手动修复常见的JSON问题
     * 
     * @param jsonStr 要修复的JSON字符串
     * @return 修复后的JSON字符串
     */
    private static String manuallyFixJson(String jsonStr) {
        if (jsonStr == null) {
            return "{}";
        }
        
        // 去除前后空白
        String result = jsonStr.trim();
        
        // 处理转义字符问题
        result = handleEscapeCharacters(result);
        
        // 确保JSON对象和数组有正确的开始和结束
        if (result.startsWith("{") && !result.endsWith("}")) {
            result = result + "}";
        } else if (result.startsWith("[") && !result.endsWith("]")) {
            result = result + "]";
        } else if (!result.startsWith("{") && !result.startsWith("[")) {
            // 如果不是以{或[开头，尝试将其包装为对象
            result = "{" + result + "}";
        }
        
        // 处理多余的逗号
        result = handleTrailingCommas(result);
        
        return result;
    }
    
    /**
     * 处理JSON中的转义字符问题
     * 
     * @param jsonStr 要处理的JSON字符串
     * @return 处理后的JSON字符串
     */
    private static String handleEscapeCharacters(String jsonStr) {
        // 处理Unicode转义序列
        StringBuilder sb = new StringBuilder(jsonStr);
        int i = 0;
        while (i < sb.length() - 1) {
            // 检查是否有未转义的控制字符
            if (sb.charAt(i) < 32 && sb.charAt(i) != '\t' && sb.charAt(i) != '\n' && sb.charAt(i) != '\r') {
                sb.deleteCharAt(i);
                continue;
            }
            
            // 检查是否有未正确转义的引号
            if (i > 0 && sb.charAt(i) == '\"' && sb.charAt(i - 1) != '\\' && 
                    (i < 2 || sb.charAt(i - 2) != '\\')) {
                // 在引号前添加转义字符
                if (isWithinString(sb.toString(), i)) {
                    sb.insert(i, '\\');
                    i++;
                }
            }
            i++;
        }
        
        return sb.toString();
    }
    
    /**
     * 判断指定位置是否在JSON字符串内
     * 
     * @param jsonStr JSON字符串
     * @param pos 位置
     * @return 如果在字符串内返回true，否则返回false
     */
    private static boolean isWithinString(String jsonStr, int pos) {
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < pos; i++) {
            char c = jsonStr.charAt(i);
            if (c == '\\' && !escaped) {
                escaped = true;
            } else if (c == '\"' && !escaped) {
                inString = !inString;
                escaped = false;
            } else {
                escaped = false;
            }
        }
        
        return inString;
    }
    
    /**
     * 处理JSON中的多余逗号
     * 
     * @param jsonStr 要处理的JSON字符串
     * @return 处理后的JSON字符串
     */
    private static String handleTrailingCommas(String jsonStr) {
        // 处理对象中的多余逗号
        StringBuilder sb = new StringBuilder(jsonStr);
        int i = 0;
        while (i < sb.length() - 1) {
            if (sb.charAt(i) == ',' && 
                    (sb.charAt(i + 1) == '}' || sb.charAt(i + 1) == ']')) {
                sb.deleteCharAt(i);
                continue;
            }
            i++;
        }
        
        return sb.toString();
    }
    
    // For examples of how to use this class, see the examples package:
    // com.alibaba.langengine.jsonrepair.examples
}

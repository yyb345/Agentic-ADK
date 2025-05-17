package com.alibaba.langengine.jsonrepair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * JsonRepair - A utility class to repair malformed JSON strings
 * <p>
 * This class provides methods to repair and parse malformed JSON strings
 * using Alibaba's FastJSON library.
 * <p>
 * It can handle various common JSON errors including:
 * - Missing quotes around keys
 * - Missing commas between elements
 * - Trailing commas in arrays and objects
 * - Single quotes instead of double quotes
 * - Comments (both // and /* style)
 * - Unquoted string literals
 */
@Slf4j
public class JsonRepair {


    /**
     * Repairs a malformed JSON string and returns the repaired JSON string
     *
     * @param jsonStr The malformed JSON string to repair
     * @return The repaired JSON string
     */
    public static String repairJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            log.warn("Empty or null JSON string provided for repair");
            return "{}";
        }
        return String.valueOf(repairJson(jsonStr, false, false));
    }

    /**
     * Repairs a malformed JSON string and returns either the repaired JSON string
     * or the parsed object
     *
     * @param jsonStr       The malformed JSON string to repair
     * @param returnObjects If true, returns the parsed object instead of a string
     * @param skipJsonParse If true, skips trying to parse with FastJSON first
     * @return The repaired JSON string or parsed object
     */
    public static Object repairJson(String jsonStr, boolean returnObjects, boolean skipJsonParse) {
        // 预处理JSON字符串，处理一些常见错误
        jsonStr = preProcessJson(jsonStr);

        JsonParser parser = new JsonParser(jsonStr);
        Object parsedJson;

        if (skipJsonParse) {
            log.info("Skipping FastJSON parse, using custom parser directly");
            parsedJson = parser.parse();
        } else {
            try {
                // Try parsing with FastJSON first
                log.info("Attempting to parse with FastJSON");
                parsedJson = JSON.parse(jsonStr);
                log.info("Successfully parsed with FastJSON");
            } catch (JSONException e) {
                // If FastJSON fails, use our custom parser
                log.info("FastJSON parse failed: " + e.getMessage() + ", using custom parser");
                parsedJson = parser.parse();
            }
        }

        // Return the parsed object or the JSON string
        if (returnObjects) {
            return parsedJson;
        }

        return JSON.toJSONString(parsedJson);
    }

    /**
     * 预处理JSON字符串，修复一些常见错误
     *
     * @param jsonStr 原始JSON字符串
     * @return 预处理后的JSON字符串
     */
    private static String preProcessJson(String jsonStr) {
        if (jsonStr == null) {
            return "{}";
        }

        // 去除前后空白
        jsonStr = jsonStr.trim();

        // 如果是空字符串，返回空对象
        if (jsonStr.isEmpty()) {
            return "{}";
        }

        // 确保JSON对象和数组有正确的开始和结束
        if (jsonStr.startsWith("{") && !jsonStr.endsWith("}")) {
            jsonStr = jsonStr + "}";
        } else if (jsonStr.startsWith("[") && !jsonStr.endsWith("]")) {
            jsonStr = jsonStr + "]";
        } else if (!jsonStr.startsWith("{") && !jsonStr.startsWith("[")) {
            // 如果不是以{或[开头，尝试将其包装为对象
            jsonStr = "{" + jsonStr + "}";
        }

        return jsonStr;
    }

    /**
     * Loads and repairs a JSON string, similar to JSON.parse() but with repair capability
     *
     * @param jsonStr The JSON string to load and repair
     * @return The parsed object
     */
    public static Object loads(String jsonStr) {
        return repairJson(jsonStr, true, false);
    }

    /**
     * Loads and repairs a JSON string, similar to JSON.parse() but with repair capability
     *
     * @param jsonStr       The JSON string to load and repair
     * @param skipJsonParse If true, skips trying to parse with FastJSON first
     * @return The parsed object
     */
    public static Object loads(String jsonStr, boolean skipJsonParse) {
        return repairJson(jsonStr, true, skipJsonParse);
    }

    /**
     * Loads and repairs JSON from a Reader
     *
     * @param reader The Reader containing JSON data
     * @return The parsed object
     * @throws IOException If an I/O error occurs
     */
    public static Object load(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, n);
        }
        return loads(sb.toString());
    }

    /**
     * Loads and repairs JSON from a file
     *
     * @param filename The name of the file containing JSON data
     * @return The parsed object
     * @throws IOException If an I/O error occurs
     */
    public static Object fromFile(String filename) throws IOException {
        try (FileReader reader = new FileReader(new File(filename))) {
            return load(reader);
        }
    }

    // For examples of how to use this class, see the examples package:
    // com.alibaba.langengine.jsonrepair.examples
}

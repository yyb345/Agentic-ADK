package com.alibaba.langengine.modelcontextprotocol.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * MCP FastJSON 工具类
 * 提供专门配置的 FastJSON 配置，用于处理 MCP 对象的序列化和反序列化
 *
 * @author aihe.ah
 * @date 2025/4/5
 */
public class McpFastJsonUtil {

    // 序列化配置单例
    private static SerializeConfig SERIALIZE_CONFIG;

    // 解析配置单例
    private static ParserConfig PARSER_CONFIG;

    /**
     * 获取配置好的序列化配置实例
     *
     * @return 配置好的 SerializeConfig 实例
     */
    public static synchronized SerializeConfig getSerializeConfig() {
        if (SERIALIZE_CONFIG == null) {
            SERIALIZE_CONFIG = createSerializeConfig();
        }
        return SERIALIZE_CONFIG;
    }

    /**
     * 获取配置好的解析配置实例
     *
     * @return 配置好的 ParserConfig 实例
     */
    public static synchronized ParserConfig getParserConfig() {
        if (PARSER_CONFIG == null) {
            PARSER_CONFIG = createParserConfig();
        }
        return PARSER_CONFIG;
    }

    /**
     * 创建并配置序列化配置
     *
     * @return 配置好的 SerializeConfig 实例
     */
    private static SerializeConfig createSerializeConfig() {
        SerializeConfig serializeConfig = new SerializeConfig();

        // 配置属性命名策略，使其能够处理没有标准 getter 前缀的方法
        serializeConfig.setPropertyNamingStrategy(PropertyNamingStrategy.NoChange);

        return serializeConfig;
    }

    /**
     * 创建并配置解析配置
     *
     * @return 配置好的 ParserConfig 实例
     */
    private static ParserConfig createParserConfig() {
        ParserConfig parserConfig = new ParserConfig();

        // 配置属性命名策略，使其能够处理没有标准 getter 前缀的方法
        parserConfig.propertyNamingStrategy = PropertyNamingStrategy.NoChange;

        // 允许自动类型推断
        parserConfig.setAutoTypeSupport(true);

        return parserConfig;
    }

    /**
     * 配置现有的 SerializeConfig 实例，使其适用于 MCP 对象
     *
     * @param config 要配置的 SerializeConfig 实例
     * @return 配置好的 SerializeConfig 实例
     */
    public static SerializeConfig configureSerializeConfig(SerializeConfig config) {
        SerializeConfig configuredConfig = new SerializeConfig();

        // 配置属性命名策略，使其能够处理没有标准 getter 前缀的方法
        configuredConfig.setPropertyNamingStrategy(PropertyNamingStrategy.NoChange);

        return configuredConfig;
    }

    /**
     * 配置现有的 ParserConfig 实例，使其适用于 MCP 对象
     *
     * @param config 要配置的 ParserConfig 实例
     * @return 配置好的 ParserConfig 实例
     */
    public static ParserConfig configureParserConfig(ParserConfig config) {
        ParserConfig configuredConfig = new ParserConfig();

        // 配置属性命名策略，使其能够处理没有标准 getter 前缀的方法
        configuredConfig.propertyNamingStrategy = PropertyNamingStrategy.NoChange;

        // 允许自动类型推断
        configuredConfig.setAutoTypeSupport(true);

        return configuredConfig;
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 要转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        return JSON.toJSONString(
                object,
                getSerializeConfig(),
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.IgnoreNonFieldGetter
        );
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param <T>   目标类型
     * @param json  JSON 字符串
     * @param clazz 目标类
     * @return 转换后的对象
     * @throws JSONException 如果转换失败
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(
                json,
                clazz,
                getParserConfig(),
                Feature.SupportAutoType,
                Feature.AllowSingleQuotes
        );
    }

    /**
     * 将对象转换为 JSON 字符串，使用默认的序列化特性
     *
     * @param object   要转换的对象
     * @param features 要使用的序列化特性
     * @return JSON 字符串
     */
    public static String toJson(Object object, SerializerFeature... features) {
        return JSON.toJSONString(object, getSerializeConfig(), features);
    }

    /**
     * 将 JSON 字符串转换为对象，使用默认的解析特性
     *
     * @param <T>      目标类型
     * @param json     JSON 字符串
     * @param clazz    目标类
     * @param features 要使用的解析特性
     * @return 转换后的对象
     * @throws JSONException 如果转换失败
     */
    public static <T> T fromJson(String json, Class<T> clazz, Feature... features) {
        return JSON.parseObject(json, clazz, getParserConfig(), features);
    }

}
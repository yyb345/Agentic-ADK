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
package com.alibaba.langengine.core.outputparser;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

/**
 * @author aihe.ah
 * @time 2024/1/2
 * 功能说明：有时候模型涉及到一些推理过程，输出的内容未必是完全的JSON，只是包含了对应的JSONArray。
 * 这里做一个兼容版本的，只提取里面的JSON进行解析，不强制要求模型输出JSONArray，包含就行
 */
public class JsonArrayOutputParserV2<T> extends BaseOutputParser<List<T>> {

    private static Pattern pattern = Pattern.compile("\\[.*?\\]");

    private Class<T> parserClass;

    public JsonArrayOutputParserV2(Class<T> parserClass) {
        this.parserClass = parserClass;
    }

    /**
     * 返回类型键
     *
     * @return 返回解析器类型
     */
    @Override
    public String getParserType() {
        return "json_array_output_parser_v2";
    }

    /**
     * 解析LLM调用的输出
     *
     * @param text 要解析的文本
     * @return 解析后的对象列表，或者在解析失败时返回null
     */
    @Override
    public List<T> parse(String text) {
        if (Objects.isNull(text)) {
            return null;
        }

        try {
            List<T> array = JSON.parseArray(text, parserClass);
            return array;
        }catch (Exception e){

        }

        String json = extractJsonArray(text);
        if (json == null) {
            return null;
        }

        try {
            return JSONArray.parseArray(json, parserClass);
        } catch (Exception e) {
            // 记录错误或进行其他错误处理
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从文本中提取JSON数组字符串
     *
     * @param text 包含JSON数组的文本
     * @return 提取的JSON数组字符串，如果没有找到则返回null
     */
    private String extractJsonArray(String text) {

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }
}
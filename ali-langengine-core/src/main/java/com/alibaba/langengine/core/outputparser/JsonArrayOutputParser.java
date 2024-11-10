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

import com.alibaba.fastjson.JSONArray;
import java.util.List;
import java.util.Objects;

/**
 * @author yushuo
 * @version JsonArrayOutputParser.java, v 0.1 2023年12月26日 10:44 yushuo
 */
public class JsonArrayOutputParser<T> extends BaseOutputParser<List<T>> {

    private Class<T> parserClass;

    public JsonArrayOutputParser(Class<T> parserClass) {
        this.parserClass = parserClass;
    }

    /**
     * 返回类型键
     *
     * @return
     */
    @Override
    public String getParserType() {
        return "json_array_output_parser";
    }

    /**
     * 解析LLM调用的输出
     *
     * @param text
     * @return
     */
    @Override
    public List<T> parse(String text) {
        if (Objects.isNull(text)) {
            return null;
        }

        text = text.replace("```json", "")
                .replace("```", "");
        try {
            return (List<T>) JSONArray.parseArray(text, parserClass);
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }

}

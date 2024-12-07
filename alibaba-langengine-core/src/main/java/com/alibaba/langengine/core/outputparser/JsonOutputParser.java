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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cuzz.lb
 * @date 2023/11/28 20:44
 */
public class JsonOutputParser<T> extends BaseOutputParser<T> {

    private static final String FORMAT_INSTRUCTIONS = "The output should be formatted as a JSON instance that conforms to the JSON schema below.\n" +
            "\n" +
            "As an example, for the schema {{\"properties\": {{\"foo\": {{\"title\": \"Foo\", \"description\": \"a list of strings\", \"type\": \"array\", \"items\": {{\"type\": \"string\"}}}}}}, \"required\": [\"foo\"]}}\n" +
            "the object {{\"foo\": [\"bar\", \"baz\"]}} is a well-formatted instance of the schema. The object {{\"properties\": {{\"foo\": [\"bar\", \"baz\"]}}}} is not well-formatted.\n" +
            "\n" +
            "Here is the output schema:\n" +
            "```\n" +
            "{{schema}}\n" +
            "```";
    private Class<T> parserClass;

    public JsonOutputParser(Class<T> parserClass) {
        this.parserClass = parserClass;
    }

    @Override
    public String getFormatInstructions() {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        try {
            JsonSchema schema = schemaGen.generateSchema(parserClass);
            String schemaJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            return FORMAT_INSTRUCTIONS.replace("{{schema}}", schemaJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getParserType() {
        return "json-parser";
    }

    @Override
    public T parse(String text) {
        try {
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text.trim());
            String jsonStr = "";
            if (matcher.find()) {
                jsonStr = matcher.group();
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return (T) objectMapper.readValue(jsonStr, parserClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

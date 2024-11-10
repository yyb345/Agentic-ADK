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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author aihe.ah
 * @time 2024/1/2
 * 功能说明：
 */
public class JsonArrayOutputParserV2Test {
    //Field parserClass of type Class - was not mocked since Mockito doesn't mock a Final class when
    // 'mock-maker-inline' option is not set

    @Test
    public void testGetParserType() throws Exception {
        // success
        JsonArrayOutputParserV2<Map> mapJsonArrayOutputParserV2 = new JsonArrayOutputParserV2<>(Map.class);
        List<Map> parse = mapJsonArrayOutputParserV2.parse(
            "根据提供的上下文和问题，似乎没有直接与“男性用户”相关的标签信息。因此，输出结果应该是空的JSONArray： ```json [] ```");
        System.out.println(parse);

        List<Map> parse1 = mapJsonArrayOutputParserV2.parse(
            "```json [{\"labelId\": \"4002\", \"labelName\": \"手淘近期成交金额\", "
                + "\"description\": \"手淘近期成交金额，可根据近期成交金额进行圈人\"}] ```");
        System.out.println(parse1);
    }

    @Test
    public void testGetParserArray() throws Exception {
        // success
        JsonArrayOutputParserV2<String> mapJsonArrayOutputParserV2 = new JsonArrayOutputParserV2<>(String.class);
        List<String> parse = mapJsonArrayOutputParserV2.parse("[\n"
            + "  \"我想查找关于'1999年'的标签\",\n"
            + "  \"我想查找关于'女性'的标签\",\n"
            + "  \"我想查找关于'南京'的标签\",\n"
            + "  \"我想查找关于'淘宝'的标签\",\n"
            + "  \"我想查找关于'加购'的标签\",\n"
            + "  \"我想查找关于'浏览'的标签\",\n"
            + "  \"我想查找关于'二级类目'的标签\"\n"
            + "]");
        System.out.println(parse);
        for (String s : parse) {
            System.out.println(s);
        }

    }

    @Test
    public void testParseNormalJsonArray() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "[{\"name\":\"John\", \"age\":30}, {\"name\":\"Jane\", \"age\":25}]";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testParseEmptyString() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "";
        List<Map> result = parser.parse(input);
        assertNull(result);
    }

    @Test
    public void testParseNonJsonString() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "This is not a JSON array";
        List<Map> result = parser.parse(input);
        assertNull(result);
    }

    @Test
    public void testParseStringWithJsonArray() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "Some text before the array: [{\"item\":1}, {\"item\":2}] and some text after.";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testParseNull() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        List<Map> result = parser.parse(null);
        assertNull(result);
    }

    @Test
    public void testParseJsonArrayWithSingleObject() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "[{\"single\":\"object\"}]";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testParseEmptyJsonArray() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "[]";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseMalformedJsonArray() {
        // success
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "[{\"name\":\"John\", \"age\":30}]";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
    }

    @Test
    public void testParseNestedJsonArray() {
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "[{\"person\":{\"name\":\"John\", \"age\":30}}, {\"person\":{\"name\":\"Jane\", \"age\":25}}]";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testParseJsonArrayWithSpecialCharacters() {
        JsonArrayOutputParserV2<Map> parser = new JsonArrayOutputParserV2<>(Map.class);
        String input = "[{\"name\":\"John \\\"Doe\\\"\", \"age\":30}, {\"name\":\"Jane /n Doe\", \"age\":25}]";
        List<Map> result = parser.parse(input);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

}
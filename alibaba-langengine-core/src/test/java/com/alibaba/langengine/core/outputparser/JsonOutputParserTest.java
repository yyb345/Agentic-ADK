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

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @author cuzz.lb
 * @date 2023/11/28 19:18
 */
public class JsonOutputParserTest {
    // Field parserClass of type Class - was not mocked since Mockito doesn't mock a Final class when 'mock-maker-inline' option is not set

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testGetFormatInstructions() throws Exception {
        // success
        JsonOutputParser<Answer>  jsonOutputParser = new JsonOutputParser<Answer>(Answer.class);
        String result = jsonOutputParser.getFormatInstructions();
        System.out.println(result);
    }

    @Test
    public void testParse() throws Exception {
        // success
        JsonOutputParser<Answer>  jsonOutputParser = new JsonOutputParser<Answer>(Answer.class);
        String result = jsonOutputParser.getFormatInstructions();
        Answer parse = jsonOutputParser.parse("{\n" +
                "  \"list\": [\"dog\", \"cat\", \"bird\"],\n" +
                "  \"source\": \"Animal Planet website\"\n" +
                "}");
        System.out.println(JSONObject.toJSON(parse));
    }

    @Test
    public void testParse_extra() throws Exception {
        // success
        JsonOutputParser<Map>  jsonOutputParser = new JsonOutputParser<Map>(Map.class);
        String result = jsonOutputParser.getFormatInstructions();
        Map parse = jsonOutputParser.parse("abc{\n" +
            "  \"list\": [\"dog\", \"cat\", \"bird\"],\n" +
            "  \"source\": \"Animal Planet website\"\n" +
            "}");
        System.out.println(JSONObject.toJSON(parse));
    }


    @Data
    static class Answer {
        @JsonPropertyDescription("answer to the user's question")
        private List<String> list;
        @JsonPropertyDescription("source used to answer the user's question, should be a website")
        private String source;
    }
}

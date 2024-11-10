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
package com.alibaba.langengine.core.chain.tot;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * Class to parse the output of a PROPOSE_PROMPT response.
 *
 * @author xiaoxuan.lp
 */
@Data
public class JSONListOutputParser extends BaseOutputParser<List<String>> {

    @Override
    public List<String> parse(String text) {
        try {
            String jsonContent = text.split("```json")[1]
                    .trim()
                    .replaceAll("^```|```$", "")
                    .trim();
            return JSON.parseArray(jsonContent, String.class);
        } catch (Throwable e) {
            try {
                return JSON.parseArray(text, String.class);
            } catch (Throwable ex) {
                return Arrays.asList(text.split("\n\n"));
            }
        }
    }

    public static void main(String[] args) {
        String text = "3,4,1,2|1,*,3,*|*,1,*,3|4,*,*,1\n" +
                "\n" +
                "3,4,1,2|1,*,3,*|*,1,*,3|4,*,*,1\n" +
                "\n" +
                "3,4,1,2|1,*,3,*|*,1,*,3|4,*,*,1\n" +
                "\n" +
                "3,4,1,2|1,*,3,*|*,1,*,3|4,*,*,1\n" +
                "\n" +
                "3,4,1,2|1,*,3,*|*,1,*,3|4,*,*,1";
        JSONListOutputParser parser = new JSONListOutputParser();
        List<String> tests = parser.parse(text);
        System.out.print(tests);
    }

    @Override
    public String getParserType() {
        return "json_list";
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cuzz.lb
 * @date 2023/11/28 20:44
 */
public class NumberedListOutputParser extends ListOutputParser {
    @Override
    public String getFormatInstructions() {
        return (
                "Your response should be a numbered list with each item on a new line."  +
                        "For example: \n\n1. foo\n\n2. bar\n\n3. baz"
        );
    }

    @Override
    public String getParserType() {
        return "numbered-list";
    }

    @Override
    public List<String> parse(String text) {
        Pattern pattern = Pattern.compile("\\d+\\.\\s([^\\n]+)");
        Matcher matcher = pattern.matcher(text);

        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }
}

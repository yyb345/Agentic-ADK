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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cuzz.lb
 * @date 2023/11/28 20:44
 */
public class CommaSeparatedListOutputParser extends ListOutputParser {
    @Override
    public String getFormatInstructions() {
        return (
                "Your response should be a list of comma separated values, " +
                        "eg: `foo, bar, baz`"
        );
    }

    @Override
    public String getParserType() {
        return "comma-separated-list";
    }

    @Override
    public List<String> parse(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split(", ")).collect(Collectors.toList());
    }
}

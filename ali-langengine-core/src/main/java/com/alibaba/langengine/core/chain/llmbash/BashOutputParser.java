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
package com.alibaba.langengine.core.chain.llmbash;

import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for bash output.
 *
 * @author xiaoxuan.lp
 */
@Data
public class BashOutputParser extends BaseOutputParser<List<String>> {

    @Override
    public List<String> parse(String text) {
        if (text.contains("```bash")) {
            return getCodeBlocks(text);
        } else {
            throw new RuntimeException("Failed to parse bash output. Got: " + text);
        }
    }

    private List<String> getCodeBlocks(String text) {
        List<String> codeBlocks = new ArrayList<>();
        Pattern pattern = Pattern.compile("```bash(.*?)(?:\\n\\s*)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String matched = matcher.group(1).trim();
            if (!matched.isEmpty()) {
                codeBlocks.addAll(Arrays.asList(matched.split("\n")));
            }
        }
        return codeBlocks;
    }

    @Override
    public String getParserType() {
        return "bash";
    }
}

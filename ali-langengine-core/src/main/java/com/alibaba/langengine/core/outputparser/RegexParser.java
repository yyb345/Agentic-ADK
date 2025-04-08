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

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse the output into a dictionary.
 * 将输出解析为字典的类。
 *
 * @author xiaoxuan.lp
 */
@Data
public class RegexParser extends BaseOutputParser<Map<String, Object>> {

    private String regex;

    private List<String> outputKeys;

    private String defaultOutputKey;

    @Override
    public Map<String, Object> parse(String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        Map<String, Object> result = new HashMap<>();
        if(matcher.find()) {
            for(int i = 0; i < outputKeys.size(); i++) {
                String key = outputKeys.get(i);
                result.put(key, matcher.group(i + 1));
            }
            return result;
        }
        return result;
    }
}

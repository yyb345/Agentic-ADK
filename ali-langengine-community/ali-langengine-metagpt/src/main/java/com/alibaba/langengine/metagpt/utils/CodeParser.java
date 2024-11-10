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
package com.alibaba.langengine.metagpt.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CodeParser {

    public static List<String> parseFileList(String block, String text, String lang) {
        String code = parseCode(block, text, lang);
        String pattern = "\\s*(.=.)?(\\[.*\\])";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(code);
        if (matcher.find()) {
            String tasksListStr = matcher.group(2);
            return JSON.parseArray(tasksListStr, String.class);
        } else {
            throw new RuntimeException();
        }
    }

    public static String parseCode(String block, String text, String lang) {
        if (block != null) {
            text = parseBlock(block, text);
        }
        String pattern = String.format("%s.*?\\s+(.*?)", lang);
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(text);
        if (matcher.find()) {
            String code = matcher.group(1);
            return code;
        } else {
            log.error(pattern + " not match following text:");
            log.error(text);
            return text;
        }
    }

    public static String parseBlock(String block, String text) {
        Map<String, String> blocks = parse_blocks(text);
        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            if (entry.getKey().contains(block)) {
                return entry.getValue();
            }
        }
        return "";
    }

    public static Map<String, String> parse_blocks(String text) {
        String[] blocks = text.split("##");
        Map<String, String> block_dict = new HashMap<>();
        for (String block : blocks) {
            if (!block.trim().isEmpty()) {
                String[] block_parts = block.split("\n", 2);
                String block_title = block_parts[0].trim();
                String block_content = block_parts[1].trim();
                block_dict.put(block_title, block_content);
            }
        }
        return block_dict;
    }

    public static String parseLangCode(String text, String lang) {
        // 匹配代码段标题
        Pattern codePattern = Pattern.compile("```" + lang + "(.+?)```", Pattern.DOTALL);
        Matcher codeMatcher = codePattern.matcher(text);
        if (codeMatcher.find()) {
            return codeMatcher.group(1);
        }
        return text;
    }

    public static String parseBlockCode(String text, String block) {
        String pattern = "\\[" + block + "\\]" + "(.+?)" + "\\[/" + block + "\\]";
        // 匹配代码段标题
        Pattern codePattern = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher codeMatcher = codePattern.matcher(text);
        if (codeMatcher.find()) {
            return codeMatcher.group(1);
        }
        return text;
    }
}

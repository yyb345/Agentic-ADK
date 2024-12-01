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
package com.alibaba.agentmagic.framework.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
    /**
     * 验证URL的合法性
     *
     * @param urlStr 要验证的URL字符串
     * @return 如果URL合法，返回true；否则，返回false
     */
    public static boolean isValidURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            url.toURI(); // 检查URI的合法性
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从字符串中提取URL
     *
     * @param input 输入字符串
     * @return 提取到的URL列表
     */
    public static List<String> extractUrls(String input) {
        List<String> urls = new ArrayList<>();

        // 去除首尾的方括号和空格
        input = input.trim();
        if (input.startsWith("[")) {
            input = input.substring(1);
        }
        if (input.endsWith("]")) {
            input = input.substring(0, input.length() - 1);
        }

        // 定义匹配 URL 的正则表达式
        String urlRegex = "https?://[^,\\s]+(?:,[^,\\s]+)*";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(input);

        // 查找所有匹配的 URL
        while (matcher.find()) {
            String url = matcher.group();
            urls.add(url);
        }

        return urls;
    }

}



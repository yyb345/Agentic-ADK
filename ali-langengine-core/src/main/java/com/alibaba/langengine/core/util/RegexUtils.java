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
package com.alibaba.langengine.core.util;

/**
 * @author yixuan
 * @date 2024/6/17 13:40
 */
public class RegexUtils {

    /**
     * md文件切割用的正则（二级标题）
     */
    public static final String MARKDOWN_CUSTOMIZATION_SPLIT_REGEX = "(?m)^## (?=[^#])";

    /**
     * md文件切割用的正则（三级标题）
     */
    public static final String MARKDOWN_CUSTOMIZATION_SPLIT_REGEX_THREE_LEVEL_SECTION = "(?m)^### (?=[^#])";

    /**
     * 解析opensearch版本用的正则
     */
    public static final String VERSION_REGEX = "^(.*)_v([0-9]+)$";
}

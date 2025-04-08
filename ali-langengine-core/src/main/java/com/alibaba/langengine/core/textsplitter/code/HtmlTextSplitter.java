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
package com.alibaba.langengine.core.textsplitter.code;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.textsplitter.py.PythonCodeConstants;
import com.alibaba.langengine.core.util.PythonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Attempts to split the text along Html-formatted headings.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class HtmlTextSplitter extends RecursiveCharacterTextSplitter {

    public HtmlTextSplitter() {
        setSeparators(Arrays.asList(new String[] {
                "<body",
                "<div",
                "<p",
                "<br",
                "<li",
                "<h1",
                "<h2",
                "<h3",
                "<h4",
                "<h5",
                "<h6",
                "<span",
                "<table",
                "<tr",
                "<td",
                "<th",
                "<ul",
                "<ol",
                "<header",
                "<footer",
                "<nav",
                "<head",
                "<style",
                "<script",
                "<meta",
                "<title",
                "",
        }));
    }

    public String getKeepSeparatorRegex(String separator) {
        return "(?<=" + separator + ")|(?=" + separator + ")";
    }
}

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
package com.alibaba.langengine.core.textsplitter.py;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.TextSplitter;
import com.alibaba.langengine.core.util.PythonUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ByPythonTextSplitter extends TextSplitter {

    public List<Document> createDocuments(List<String> texts, List<Map<String, Object>> metadatas) {
        texts = texts.stream().map(text -> text
                        .replaceAll("\n", "\\\\n")
                        .replaceAll("\"", "\\\\\"")
                        .replaceAll("'", "")
//                        .replaceAll("\\\\", "\\\\\\\\")
                        )
                .collect(Collectors.toList());
        String input = JSON.toJSONString(texts);
        String result = PythonUtils.invokePythonCode(fetchPythonCode(), input);
//        String result = PythonUtils.invokeMethodAsResource(getClass(), "htmlTextSplitter_createDocuments.py", JSON.toJSONString(texts));
        List<Document> documents = JSON.parseArray(result, Document.class);
        return documents;
    }

    public List<String> splitText(String text) {
        return null;
    }

    public abstract String fetchPythonCode();
}

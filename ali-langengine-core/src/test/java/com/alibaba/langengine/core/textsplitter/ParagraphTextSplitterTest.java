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
package com.alibaba.langengine.core.textsplitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParagraphTextSplitterTest {

    @Test
    public void test_createDocuments() {
        // success
        ParagraphTextSplitter textSplitter = new ParagraphTextSplitter();
        textSplitter.setMaxChunkSize(60);
        textSplitter.setMaxChunkOverlap(0);
        String text = "# LangChain\\R" +
                "\\R" +
                "Building applications with LLMs through composability \n" +
                "\n" +
                "## Quick Install\n" +
                "\n" +
                "```bash\n" +
                "# Hopefully this code block isn't split\n" +
                "pip install langchain\n" +
                "```\n" +
                "\n" +
                "As an open source project in a rapidly developing field, we are extremely open to contributions.";
        long start = System.currentTimeMillis();
        List<Document> documents = textSplitter.createDocuments(Arrays.asList(new String[]{text}), new ArrayList<>());
        System.out.println(JSON.toJSONString(documents));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LatexTextSplitterTest {

    @Test
    public void test_createDocuments() {
        // success
        LatexTextSplitter textSplitter = new LatexTextSplitter();
        textSplitter.setMaxChunkSize(60);
        textSplitter.setMaxChunkOverlap(0);
        textSplitter.setSeparatorRegex(true);
        String text = "\\documentclass{article}\n" +
                "\n" +
                "\\begin{document}\n" +
                "\n" +
                "\\maketitle\n" +
                "\n" +
                "\\section{Introduction}\n" +
                "Large language models (LLMs) are a type of machine learning model that can be trained on vast amounts of text data to generate human-like language. In recent years, LLMs have made significant advances in a variety of natural language processing tasks, including language translation, text generation, and sentiment analysis.\n" +
                "\n" +
                "\\subsection{History of LLMs}\n" +
                "The earliest LLMs were developed in the 1980s and 1990s, but they were limited by the amount of data that could be processed and the computational power available at the time. In the past decade, however, advances in hardware and software have made it possible to train LLMs on massive datasets, leading to significant improvements in performance.\n" +
                "\n" +
                "\\subsection{Applications of LLMs}\n" +
                "LLMs have many applications in industry, including chatbots, content creation, and virtual assistants. They can also be used in academia for research in linguistics, psychology, and computational linguistics.\n" +
                "\n" +
                "\\end{document}";
        List<Document> documents = textSplitter.createDocuments(Arrays.asList(new String[] { text }), new ArrayList<>());
        System.out.println(JSON.toJSONString(documents));
    }
}

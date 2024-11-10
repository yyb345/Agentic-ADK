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
import com.alibaba.langengine.core.tokenizers.GPT2Tokenizer;
import com.alibaba.langengine.core.tokenizers.QwenTokenizer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecursiveCharacterTextSplitterTest {

    @Test
    public void test() {
        // success
        String text = "Who are you? I'am sunleepy.";

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(10);
        textSplitter.setMaxChunkOverlap(0);

        List<Document> documents = textSplitter.createDocuments(text);
        System.out.println(JSON.toJSONString(documents));
    }

    @Test
    public void test_GPT2Tokenizer() throws IOException {
        // success
        String filePath = getClass().getClassLoader().getResource("example_data/information.txt").getPath();
        String text = Files.readAllLines(Paths.get(filePath)).stream().collect(Collectors.joining("\n\n"));

        CharacterTextSplitter textSplitter = new CharacterTextSplitter();
        textSplitter.setMaxChunkSize(100);
        textSplitter.setMaxChunkOverlap(0);
        textSplitter.setTokenizer(new GPT2Tokenizer());
        System.out.println(textSplitter.getLength(text));

        List<String> texts = textSplitter.splitText(text);
        System.out.println(JSON.toJSONString(texts));
    }

    @Test
    public void test_QwenTokenizer() throws IOException {
        // success
        String filePath = getClass().getClassLoader().getResource("example_data/information.txt").getPath();
        String text = Files.readAllLines(Paths.get(filePath)).stream().collect(Collectors.joining("\n\n"));

        CharacterTextSplitter textSplitter = new CharacterTextSplitter();
        textSplitter.setMaxChunkSize(100);
        textSplitter.setMaxChunkOverlap(0);
        textSplitter.setTokenizer(new QwenTokenizer());
        System.out.println(textSplitter.getLength(text));

        List<String> texts = textSplitter.splitText(text);
        System.out.println(JSON.toJSONString(texts));
    }


    @Test
    public void test_read_direct() throws IOException {
        // success
        String filePath = getClass().getClassLoader().getResource("example_data/information.txt").getPath();

        String text = Files.readAllLines(Paths.get(filePath)).stream().collect(Collectors.joining("\n\n"));

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(100);
        textSplitter.setMaxChunkOverlap(20);

        List<Document> documents = textSplitter.createDocuments(Arrays.asList(new String[] { text }), new ArrayList<>());
        System.out.println(documents.get(0).getPageContent());
        System.out.println(documents.get(1).getPageContent());
    }
}

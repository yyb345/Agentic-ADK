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
package com.alibaba.langengine.chroma.vectorstore;

import java.util.HashMap;
import java.util.List;

import com.alibaba.langengine.core.docloader.UnstructuredTxtLoader;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;

import org.junit.jupiter.api.Test;

/**
 * @author aihe.ah
 * @time 2023/12/14
 * 功能说明：
 */
public class ChromaTest {

    @Test
    public void testAddDocuments() throws Exception {

        Chroma chroma = new Chroma(
            "http://127.0.0.1:8000", new FakeEmbeddings(),
            "collectionId"
        );

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(1000);
        UnstructuredTxtLoader unstructuredTxtLoader = new UnstructuredTxtLoader("langengine.properties",
            textSplitter);
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("source", "example_data/state_of_the_union.txt");
        unstructuredTxtLoader.setMetadata(metadata);
        List<Document> documents = unstructuredTxtLoader.load();

        chroma.addDocuments(documents);

    }

    @Test
    public void testSimilaritySearch() throws Exception {
        Chroma chroma = new Chroma(
            "http://127.0.0.1:8000"
            , new FakeEmbeddings(),
            "collectionId"
        );

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(1000);
        UnstructuredTxtLoader unstructuredTxtLoader = new UnstructuredTxtLoader("langengine.properties",
            textSplitter);
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("source", "example_data/state_of_the_union.txt");
        unstructuredTxtLoader.setMetadata(metadata);
        List<Document> documents = unstructuredTxtLoader.load();

        List<Document> hgpostcn = chroma.similaritySearch(
            "hgpostcn",
            5
        );
        hgpostcn.forEach(document -> {
            System.out.println(document.getUniqueId());
            System.out.println(document.getScore());
            System.out.println(document.getMetadata());
            System.out.println(document.getPageContent());
        });
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
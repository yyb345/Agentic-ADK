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
package com.alibaba.langengine.core.doctransformer;

import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LongContextReorderTest {

    @Test
    public void test_transformDocuments() {
        // success
        OpenAIEmbeddings embeddings = new OpenAIEmbeddings();
        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(embeddings);

        List<String> texts = Arrays.asList(new String[] {
            "Basquetball is a great sport.",
            "Fly me to the moon is one of my favourite songs.",
            "The Celtics are my favourite team.",
            "This is a document about the Boston Celtics",
            "I simply love going to the movies",
            "The Boston Celtics won the game by 20 points",
            "This is just a random text.",
            "Elden Ring is one of the best games in the last 15 years.",
            "L. Kornet is one of the best Celtics players.",
            "Larry Bird was an iconic NBA player.",
        });
        List<Document> documents =  texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        vectorStore.addDocuments(documents);

        BaseRetriever retriever = vectorStore.asRetriever();
        documents =  retriever.getRelevantDocuments("What can you tell me about the Celtics?", 10);

        System.out.println("original:");
        documents.forEach(e -> System.out.println(e.getPageContent()));

        LongContextReorder reordering = new LongContextReorder();
        List<Document> actual = reordering.transformDocuments(documents);

        System.out.println("reordered:");
        actual.forEach(e -> System.out.println(e.getPageContent()));
    }
}

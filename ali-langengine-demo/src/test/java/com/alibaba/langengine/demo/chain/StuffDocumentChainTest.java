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
package com.alibaba.langengine.demo.chain;

import com.alibaba.langengine.core.chain.retrievalqa.RetrievalQA;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 概念介绍：填充文档链 是最直接的文档链。 它需要一个文档列表，将它们全部插入到提示中，并将该提示传递给LLM。
 * 适用场景：该链非常适合文档较小且大多数调用只传递少量文档的应用程序。也是最常用的配合知识库向量检索的方式。
 *
 * @author xiaoxuan.lp
 */
public class StuffDocumentChainTest {

    @Test
    public void test_openai_run() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(new OpenAIEmbeddings());

        //文档初始向量化
        List<Document> documents = new ArrayList<>();
        Document document = new Document();
        document.setPageContent("淘宝开放平台创建应用，可以参考：https://open.taobao.com 。");
        documents.add(document);
        document = new Document();
        document.setPageContent("top接口传数据是有大小限制，API调用请求最大报文限制在10M以内。");
        documents.add(document);
        document = new Document();
        document.setPageContent("注销开放平台&删除应用，请按照以下说明操作，进入 控制台-设置-账号注销，点击注销账号或者删除应用。");
        documents.add(document);
        vectorStore.addDocuments(documents);

        //知识库向量检索
        RetrievalQA qa = new RetrievalQA();
        qa.setRecommend(2);
        qa.setLlm(llm);
        qa.setRetriever(vectorStore.asRetriever());
        qa.init();

        String answer = qa.chat("如何创建淘宝开放平台的应用？");
//        String answer = qa.chat("API接口有报文限制不，有的话是多少");
        System.out.println(answer);
    }
}

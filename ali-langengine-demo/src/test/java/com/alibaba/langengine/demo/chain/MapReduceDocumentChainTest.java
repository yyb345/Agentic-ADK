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
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MapReduceDocumentChainTest {

    private static final String CHAIN_TYPE = "map_reduce";

    @Test
    public void test_openai_run() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(new OpenAIEmbeddings());

        //文档初始向量化
        List<Document> documents = new ArrayList<>();
        Document document = new Document();
        document.setPageContent("SaaS模板接入模式\n" +
                "\n" +
                "\n" +
                "服务商入驻淘宝开放平台，选择某个SaaS模板类目创建商家应用，开发消费者端（淘宝、天猫）小程序模板，以及商家端（千牛移动、千牛PC）小程序。开发完成且均通过版本审核后，将商家应用发布上架到服务市场。\n" +
                "\n" +
                "商家付费订购后，即可快速根据模板代码进行实例化属于自己的小程序。\n" +
                "\n" +
                " \n" +
                "\n" +
                "适用场景：\n" +
                "\n" +
                "开发满足某些行业通用性需求的服务商；\n" +
                "\n" +
                "没有特殊需求，希望快速订购使用模板小程序的商家；\n" +
                "\n" +
                " \n" +
                "\n" +
                "业务特点：\n" +
                "\n" +
                "低成本研发：仅需一次开发，批量拓展商家；\n" +
                "\n" +
                "快速开通：商家仅需完成订购，即完成授权，通过模板快速生成自有小程序；\n" +
                "\n" +
                "高效维护：服务商迭代模板版本，商家更新后即可享受最新版本服务；\n" +
                "\n" +
                " \n" +
                "\n" +
                "点击查看SaaS模板接入指南。");
        documents.add(document);
        document = new Document();
        document.setPageContent("定制开发接入模式\n" +
                "\n" +
                "\n" +
                "有定制化需求但是自身没有研发能力的商家，可在服务市场发布需求，寻找适合的服务商代其开发，服务商确认接单后可自动获取授权。如果不知道如何寻找定制服务商，您也可以联系技术支持，寻求帮助。\n" +
                "\n" +
                "服务商获得商家授权后，即可通过授权开发者身份，代替商家进行代码研发、部署、提交审核等操作。\n" +
                "\n" +
                " \n" +
                "\n" +
                "适用场景：\n" +
                "\n" +
                "有特殊定制诉求，或有自己的ERP系统需要做对接的商家；\n" +
                "\n" +
                "希望数据资产控制在商家自己的服务端，有高敏感数据要求的商家；\n" +
                "\n" +
                " \n" +
                "\n" +
                "业务特点：\n" +
                "\n" +
                "开放：部分禁止向服务商开放的接口，可以向商家开放，提供更多玩法的可能性；\n" +
                "\n" +
                "安全：基于应用的定向授权，服务商无需拿商家账户即可代商家完成开发，安全可控；\n" +
                "\n" +
                "高效维护：拿到商家授权后，服务商即可代商家实现快速迭代与维护，省时省力；\n" +
                "\n" +
                " \n" +
                "\n" +
                "查看定制开发接入指南。");
        documents.add(document);
        document = new Document();
        document.setPageContent("自研接入模式\n" +
                "\n" +
                "\n" +
                "有一定IT研发能力的商家，可以直接入驻淘宝开放平台，选择某个自研类目创建商家应用，开发满足自身需求的消费者端（淘宝、天猫）小程序，以及商家端（千牛移动、千牛PC）小程序。\n" +
                "\n" +
                " \n" +
                "\n" +
                "适用场景：\n" +
                "\n" +
                "有特定开发业务诉求且有一定IT研发能力的商家，适合长期维护自身商家应用；\n" +
                "\n" +
                "希望数据资产控制在商家自己的服务端，有高敏感数据要求的商家；");
        documents.add(document);
        vectorStore.addDocuments(documents);

        //知识库向量检索
        RetrievalQA qa = new RetrievalQA();
        qa.setRecommend(2);
        qa.setLlm(llm);
        qa.setRetriever(vectorStore.asRetriever());
        qa.init(true, CHAIN_TYPE);

        String answer = qa.chat("如何创建商家自研应用？");
//        String answer = qa.chat("API接口有报文限制不，有的话是多少");
        System.out.println(answer);
    }
}

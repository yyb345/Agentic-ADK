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
package com.alibaba.langengine.core.chain.retrievalqa;

import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.callback.StdOutCallbackHandler;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.hologres.vectorstore.HologresDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class RetrievalQATest {

    @Test
    public void test_run() {

        String question = "淘宝开放平台介绍";

        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);


        //holo数据库知识库向量持久化
        HologresDB hologresDB = new HologresDB();
        hologresDB.setEmbedding(new OpenAIEmbeddings()); //通义千问提供的embeddings


        PromptTemplate prompt = new PromptTemplate();
        String tm = "请先将用户的提问翻译成英文，再进行回答，然后再把结果翻译成中文。问题是"+question;
        prompt.setTemplate(tm);
        //知识库向量检索
        RetrievalQA qa = new RetrievalQA();
        qa.setRecommend(2);
//        qa.setMaxDistanceValue(15000.0d);

        qa.setLlm(llm);
        qa.setPrompt(prompt);
        qa.setRetriever(hologresDB.asRetriever());
        qa.init();

        List<Document> faqDocuments = hologresDB.similaritySearch(question, 5);
        Document document1 = faqDocuments.get(0);
        System.out.println(document1.getPageContent());
    }

    @Test
    public void test_run_OpenAIEmbeddings_OpenAIChat() {
        //holo数据库知识库向量持久化
        HologresDB hologresDB = new HologresDB();
        hologresDB.setEmbedding(new OpenAIEmbeddings()); //openai提供的embeddings
        //模拟知识库
        List<String> knowledges = Arrays.asList(new String[]{
//                "淘宝开放平台创建应用，可以参考：https://www.atatech.org/articles/112988",
//                "API调用请求最大报文限制在10M以内，这个是nginx限制的要求"
        });
        hologresDB.addTexts(knowledges);

        //知识库向量检索
        RetrievalQA qa = new RetrievalQA();
        qa.setLlm(new ChatOpenAI()); //chatgpt大模型
        qa.setRetriever(hologresDB.asRetriever());
        qa.init();

        //qa问答
        String answer = qa.chat("API接口有报文限制不，有的话是多少");
        System.out.println(answer);

        answer = qa.chat("如何创建开放平台应用");
        System.out.println(answer);
    }

    @Test
    public void test_run_OpenAIEmbeddings_OpenAIChat_serialize() throws JsonProcessingException {
        //holo数据库知识库向量持久化
        HologresDB hologresDB = new HologresDB();
        hologresDB.setEmbedding(new OpenAIEmbeddings()); //openai提供的embeddings
        //模拟知识库
        List<String> knowledges = Arrays.asList(new String[]{
//                "淘宝开放平台创建应用，可以参考：https://www.atatech.org/articles/112988",
//                "API调用请求最大报文限制在10M以内，这个是nginx限制的要求"
        });
        hologresDB.addTexts(knowledges);

        //知识库向量检索
        RetrievalQA qa = new RetrievalQA();
        qa.setLlm(new ChatOpenAI()); //chatgpt大模型
        qa.setRetriever(hologresDB.asRetriever());
        qa.init();

        String qaJson = qa.serialize();
        RetrievalQA newQa = JacksonUtils.MAPPER.readValue(qaJson, RetrievalQA.class);
        System.out.println(newQa);

        //qa问答
        String answer = qa.chat("API接口有报文限制不，有的话是多少");
        System.out.println("answer1:" + answer);

        answer = qa.chat("如何创建开放平台应用");
        System.out.println("answer1:" + answer);

        answer = newQa.chat("API接口有报文限制不，有的话是多少");
        System.out.println("answer2:" + answer);

        answer = newQa.chat("如何创建开放平台应用");
        System.out.println("answer2:" + answer);
    }

    @Test
    public void test_run_OpenAIEmbeddings_OpenAIChat_callbackManager() {
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.addHandler(new StdOutCallbackHandler());

        //holo数据库知识库向量持久化
        HologresDB hologresDB = new HologresDB();
        hologresDB.setEmbedding(new OpenAIEmbeddings()); //openai提供的embeddings
        //模拟知识库
        List<String> knowledges = Arrays.asList(new String[]{
//                "淘宝开放平台创建应用，可以参考：https://www.atatech.org/articles/112988",
//                "API调用请求最大报文限制在10M以内，这个是nginx限制的要求"
        });
        hologresDB.addTexts(knowledges);

        //知识库向量检索
        RetrievalQA qa = new RetrievalQA();
        qa.setLlm(new ChatOpenAI()); //chatgpt大模型
        qa.setRetriever(hologresDB.asRetriever());
        qa.setCallbackManager(callbackManager);
        qa.init();

        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setChain(qa);
        //qa问答
        String answer = qa.chat("API接口有报文限制不，有的话是多少", executionContext, null, null);
        System.out.println(answer);

        answer = qa.chat("如何创建开放平台应用", executionContext, null, null);
        System.out.println(answer);
    }
}

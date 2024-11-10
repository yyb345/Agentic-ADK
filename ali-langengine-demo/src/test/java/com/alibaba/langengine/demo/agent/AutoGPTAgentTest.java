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
package com.alibaba.langengine.demo.agent;

import com.alibaba.langengine.core.agent.autogpt.AutoGPTAgent;
import com.alibaba.langengine.demo.agent.tool.MySerpapiTool;
import com.alibaba.langengine.demo.agent.tool.SearchAPITool;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.StdOutCallbackHandler;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.demo.vectorstore.BeanConfiguration;
import com.alibaba.langengine.hologres.vectorstore.HologresConfig;
import com.alibaba.langengine.hologres.vectorstore.HologresDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ReadFileTool;
import com.alibaba.langengine.tool.WriteFileTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BeanConfiguration.class, HologresConfig.class})
public class AutoGPTAgentTest {

    @Resource
    private HologresDB hologresDB;

    @Test
    public void test_run() {
        Embeddings embedding = new OpenAIEmbeddings();
        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(embedding);

        VectorStoreRetriever retriever = (VectorStoreRetriever) vectorStore.asRetriever();

        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        List<BaseTool> tools = new ArrayList<>();
        MySerpapiTool mySerpapiTool = new MySerpapiTool();
        tools.add(mySerpapiTool);
        WriteFileTool writeFileTool = new WriteFileTool();
        writeFileTool.setRootDir("/Users/xiaoxuan.lp/works/files");
        tools.add(writeFileTool);
        tools.add(new ReadFileTool());
        AutoGPTAgent autoGPTAgent = AutoGPTAgent.fromLlmAndTools("Tom", "Assistant", retriever, tools, llm, null);
        autoGPTAgent.setMaxLimit(5);
        String response = autoGPTAgent.run(Arrays.asList(new String[] { "写一个明天杭州天气的报告" }));

        System.out.println(response);
    }

    @Test
    public void test_run_serialize() throws JsonProcessingException {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setMaxTokens(1024);
        llm.setTemperature(0.0d);

        Embeddings embedding = new OpenAIEmbeddings();
        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(embedding);

        VectorStoreRetriever retriever = (VectorStoreRetriever) vectorStore.asRetriever();

        List<BaseTool> tools = new ArrayList<>();
        tools.add(new SearchAPITool());
        WriteFileTool writeFileTool = new WriteFileTool();
        writeFileTool.setRootDir("/Users/xiaoxuan.lp/works/files");
        tools.add(writeFileTool);
        tools.add(new ReadFileTool());

        AutoGPTAgent autoGPTAgent = AutoGPTAgent.fromLlmAndTools("Tom", "Assistant", retriever, tools, llm, null);
        autoGPTAgent.setMaxLimit(3);

        String agentJson = autoGPTAgent.serialize();
        AutoGPTAgent newAutoGPTAgent = JacksonUtils.MAPPER.readValue(agentJson, AutoGPTAgent.class);

        String response = autoGPTAgent.run(Arrays.asList(new String[] { "write a weather report for SF tomorrow" }));
        System.out.println("response1:" + response);

        response = newAutoGPTAgent.run(Arrays.asList(new String[] { "write a weather report for SF tomorrow" }));
        System.out.println("response2:" + response);
    }

//    @Test
//    public void test_run_callbackManager() {
//        CallbackManager callbackManager = new CallbackManager();
//        callbackManager.addHandler(new StdOutCallbackHandler());
//
//        ChatOpenAI llm = new ChatOpenAI();
//        llm.setMaxTokens(1024);
//        llm.setTemperature(0.0d);
//        VectorStoreRetriever retriever = (VectorStoreRetriever) hologresDB.asRetriever();
//        List<BaseTool> tools = new ArrayList<>();
//        tools.add(new SearchAPITool());
//        WriteFileTool writeFileTool = new WriteFileTool();
//        writeFileTool.setRootDir("/Users/xiaoxuan.lp/works/files");
//        tools.add(writeFileTool);
//        tools.add(new ReadFileTool());
//        AutoGPTAgent autoGPTAgent = AutoGPTAgent.fromLlmAndTools("Tom", "Assistant", retriever, tools, llm, callbackManager);
//        autoGPTAgent.setMaxLimit(3);
//
//        List<String> goals = Arrays.asList(new String[] { "write a weather report for SF tomorrow" });
//
//        AutoGPTExecutionContext executionContext = new AutoGPTExecutionContext();
//        executionContext.setAutoGPTAgent(autoGPTAgent);
//        executionContext.setGoals(goals);
//
//        String response = autoGPTAgent.run(goals, executionContext);
//
//        System.out.println(response);
//    }
}

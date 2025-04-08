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
//package com.alibaba.langengine.demo.callback;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.langengine.core.agent.AgentExecutor;
//import com.alibaba.langengine.core.agent.expert.ExpertChain;
//import com.alibaba.langengine.core.chain.LLMChain;
//import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
//import com.alibaba.langengine.core.tool.BaseTool;
//import com.alibaba.langengine.demo.callback.support.MockLLM;
//import com.alibaba.langengine.demo.callback.support.MyLLMMathAdvanceTool;
//import com.alibaba.langengine.demo.callback.support.MyLLMMathTool;
//import com.alibaba.langengine.openai.model.ChatModelOpenAI;
//import com.alibaba.langengine.openai.model.ChatOpenAI;
//import com.alibaba.langengine.openai.model.OpenAIModelConstants;
//import com.alibaba.langengine.tool.CreateAppTool;
//import com.alibaba.langengine.tool.ToolLoaders;
//import com.alibaba.langengine.tool.google.SerpapiTool;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 借鉴于工作流引擎相关设计理念，为了解决日志记录、监控、流式传输、异常/中断持久化、稳定性相关的其他任务，
// * 对于AI-Chain是有必要进行持久化存储的。我们的AI-Chain就好比于工作流中的各类节点，而AI-Chain中的节点
// * 相对比较确定的就是，没有过多的流程分支，将打造一个更加轻量级的链式持久化方式，而前提就是我们需要每个AI
// * 模型需要实现可序列化。
// *
// * https://dms.aliyun.com/?regionId=cn-zhangjiakou&dbType=mysql&instanceId=rm-8vbv48sx104hhsuk6&instanceSource=RDS
// *
// * @author xiaoxuan.lp
// */
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {StorageBeanConfiguration.class, DatabaseConfig.class, ChainInstanceManager.class})
//public class ChainInstanceManagerTest {
//
//    @Resource
//    private ChainInstanceManager chainInstanceManager;
//
//    @Test
//    public void test_OpenAI_LLMChain_startChainInstance() {
//        ChatModelOpenAI llm = new ChatModelOpenAI();
//        llm.setModelName(OpenAIModelConstants.GPT_4_TURBO);
//        llm.setTemperature(0d);
//
//        PromptTemplate prompt = new PromptTemplate();
//        prompt.setTemplate("生产{product}的公司起什么名字好？");
//
//        LLMChain chain = new LLMChain();
//        chain.setLlm(llm);
//        chain.setPrompt(prompt);
//
//        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("product", "球鞋");
//
//        ChainRequest chainRequest = new ChainRequest();
//        chainRequest.setChain(chain);
//        chainRequest.setInputs(inputs);
//        ExecutionResult executionResult = chainInstanceManager.startChainInstance(chainRequest);
//        System.out.println(JSON.toJSONString("response1:" + executionResult));
//
//        String chainInstanceId = executionResult.getChainInstanceId();
//
//        executionResult = chainInstanceManager.signalChainInstance(chainInstanceId);
//        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//
//    @Test
//    public void test_MockLLM_LLMChain_startChainInstance() {
//        MockLLM llm = new MockLLM();
//        llm.setTemperature(0d);
//        llm.setMockSwitch(true);
//
//        PromptTemplate prompt = new PromptTemplate();
//        prompt.setTemplate("生产{product}的公司起什么名字好？");
//
//        LLMChain chain = new LLMChain();
//        chain.setLlm(llm);
//        chain.setPrompt(prompt);
//
//        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("product", "球鞋");
//
//        ChainRequest chainRequest = new ChainRequest();
//        chainRequest.setChain(chain);
//        chainRequest.setInputs(inputs);
//        ExecutionResult executionResult = chainInstanceManager.startChainInstance(chainRequest);
//        System.out.println(JSON.toJSONString("response1:" + executionResult));
//
//        String chainInstanceId = executionResult.getChainInstanceId();
//
//        ChainInstanceDO chainInstanceDO = chainInstanceManager.getByChainInstId(chainInstanceId);
//        System.out.println("chainInstance:" + JSON.toJSONString(chainInstanceDO));
//
//        //如果注释，请使用test_MockLLM_LLMChain_signalChainInstance执行
////        executionResult = chainInstanceManager.signalChainInstance(chainInstanceDO.getChainInstId());
////        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//
//    @Test
//    public void test_MockLLM_LLMChain_signalChainInstance() {
//        ExecutionResult executionResult = chainInstanceManager.signalChainInstance("2124f1a8-6937-4b00-8249-7ecd163a4b51");
//        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//
//    @Test
//    public void test_MockLLM_ExpertLLMChain_startChainInstance() {
//        MockLLM llm = new MockLLM();
//        llm.setTemperature(0d);
//        llm.setMaxTokens(1024);
//        llm.setMockSwitch(true);
//
//        ChatOpenAI chatOpenAI = new ChatOpenAI();
//        chatOpenAI.setTemperature(0d);
//        chatOpenAI.setMaxTokens(1024);
//
//        ExpertChain chain = new ExpertChain();
//        chain.setLlm(llm);
//        chain.setExpertLlm(chatOpenAI);
//
//        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("question", "描述一下惯性定律。");
//
//        ChainRequest chainRequest = new ChainRequest();
//        chainRequest.setChain(chain);
//        chainRequest.setInputs(inputs);
//        ExecutionResult executionResult = chainInstanceManager.startChainInstance(chainRequest);
//        System.out.println(JSON.toJSONString("response1:" + executionResult));
//
//        String chainInstanceId = executionResult.getChainInstanceId();
//
//        ChainInstanceDO chainInstanceDO = chainInstanceManager.getByChainInstId(chainInstanceId);
//        System.out.println("chainInstance:" + JSON.toJSONString(chainInstanceDO));
//
//        //如果注释，请使用test_MockLLM_ExpertLLMChain_signalChainInstance执行
////        executionResult = chainInstanceManager.signalChainInstance(chainInstanceDO.getChainInstId());
////        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//
//    @Test
//    public void test_MockLLM_ExpertLLMChain_signalChainInstance() {
//        ExecutionResult executionResult = chainInstanceManager.signalChainInstance("67c5feac-e3a0-435e-8e6f-8ee4e03bd993");
//        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//
//    @Test
//    public void test_OpenAI_ZeroShotAgent_startChainInstance() {
//        //发起之前，请把MyLLMMathTool的throw new RuntimeException("LLMMathTool error.");开出来，模拟异常
//        ChatOpenAI llm = new ChatOpenAI();
//        llm.setTemperature(0d);
//        llm.setMaxTokens(1024);
//
//        List<BaseTool> baseTools = new ArrayList<>();
//        MyLLMMathTool llmMathTool = new MyLLMMathTool();
//        baseTools.add(llmMathTool);
//        SerpapiTool serpapiTool = new SerpapiTool();
//        baseTools.add(serpapiTool);
////        CvUnetImageMattingTool cvUnetImageMattingTool = new CvUnetImageMattingTool();
////        baseTools.add(cvUnetImageMattingTool);
//
//        AgentExecutor agentExecutor = ToolLoaders.initializeAgent(baseTools, llm);
//        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "Who is Leo DiCaprio's girlfriend? What is her current age raised to the 0.43 power?");
//
//        ChainRequest chainRequest = new ChainRequest();
//        chainRequest.setChain(agentExecutor);
//        chainRequest.setInputs(inputs);
//        ExecutionResult executionResult = chainInstanceManager.startChainInstance(chainRequest);
//        System.out.println(JSON.toJSONString("response1:" + executionResult));
//    }
//
//    @Test
//    public void test_OpenAI_ZeroShotAgent_signalChainInstance() {
//        //恢复之前，请把MyLLMMathTool的throw new RuntimeException("LLMMathTool error.");注释掉，模拟正常
//        ExecutionResult executionResult = chainInstanceManager.signalChainInstance("33a5e697-9cde-4ce6-bf55-0313e63e0ae1");
//        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//
//    @Test
//    public void test_OpenAI_StructuredChatAgent_startChainInstance() {
//        //发起之前，请把MyLLMMathAdvanceTool的throw new RuntimeException("LLMMathAdvanceTool error.");开出来，模拟异常
//        ChatOpenAI llm = new ChatOpenAI();
//        llm.setTemperature(0d);
//        llm.setMaxTokens(2048);
//
//        List<BaseTool> baseTools = new ArrayList<>();
//        CreateAppTool createAppTool = new CreateAppTool();
//        baseTools.add(createAppTool);
//        MyLLMMathAdvanceTool myLLMMathAdvanceTool = new MyLLMMathAdvanceTool();
//        baseTools.add(myLLMMathAdvanceTool);
//
//        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm, null);
//        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "我想要一个退货登记的应用程序，里面包括有用户信息，订单号，用户地址这些字段？然后计算有多少个字段？以及字段数量的0.43次方是多少？");
//
//        ChainRequest chainRequest = new ChainRequest();
//        chainRequest.setChain(agentExecutor);
//        chainRequest.setInputs(inputs);
//        ExecutionResult executionResult = chainInstanceManager.startChainInstance(chainRequest);
//        System.out.println(JSON.toJSONString("response1:" + executionResult));
//    }
//
//    @Test
//    public void test_OpenAI_StructuredChatAgent_signalChainInstance() {
//        //恢复之前，请把MyLLMMathAdvanceTool的throw new RuntimeException("MyLLMMathAdvanceTool error.");注释掉，模拟正常
//        ExecutionResult executionResult = chainInstanceManager.signalChainInstance("6d48af55-24d7-4b14-9b0b-d3b9ee53c127");
//        System.out.println(JSON.toJSONString("response2:" + executionResult));
//    }
//}

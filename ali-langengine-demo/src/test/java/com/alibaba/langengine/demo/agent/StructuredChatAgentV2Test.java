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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.business.tool.ApiLogTool;
import com.alibaba.langengine.business.tool.AppMonitorTool;
import com.alibaba.langengine.business.tool.ClearAccessCountTool;
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.structured.StructuredChatGlmOutputParser;
import com.alibaba.langengine.demo.agent.tool.*;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.StdOutCallbackHandler;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.DashScopeLLM;
import com.alibaba.langengine.dashscope.model.agent.DashScopeStructuredChatAgentV2;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.sqlite.memory.ConversationSqliteMemory;
import com.alibaba.langengine.sqlite.memory.cache.SqliteCache;
import com.alibaba.langengine.sqlite.memory.cache.SqliteConfig;
import com.alibaba.langengine.tool.ToolLoaders;
import com.alibaba.langengine.xinghuo.ChatXingHuo;
import com.alibaba.langengine.xinghuo.agent.ChatXingHuoStructuredChatAgentV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.*;

/**
 * StructuredChatAgentV2升级，支持memory
 *
 * @author xiaoxuan.lp
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SqliteConfig.class})
public class StructuredChatAgentV2Test {

    @Resource
    private SqliteCache sqliteCache;

    @Test
    public void test_RealTimeCongestIdxTool() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(2048);

        List<BaseTool> baseTools = new ArrayList<>();
        RealTimeCongestIdxTool realTimeCongestIdxTool = new RealTimeCongestIdxTool();
        baseTools.add(realTimeCongestIdxTool);
        ReasonTrafficTool reasonTrafficTool = new ReasonTrafficTool();
        baseTools.add(reasonTrafficTool);
        SuggestTrafficTool suggestTrafficTool = new SuggestTrafficTool();
        baseTools.add(suggestTrafficTool);

        ConversationSqliteMemory memory = new ConversationSqliteMemory(sqliteCache, "30");
//        ConversationRedisMemory memory = new ConversationRedisMemory(redisCache, "3");
        memory.setHumanPrefix("Question");
        memory.setAiPrefix("Thought");
        memory.setToolPrefix("Observation");
        memory.setMemoryKey("chat_history");
        memory.setIgnoreHuman(true);

        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm, null, true);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "北京6月1日的拥堵指数是多少？并造成拥堵的原因是什么？并且给出适当的出行建议？");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_dashscope_ApiLogTool_V1() {
        DashScopeLLM llm = new DashScopeLLM();
        llm.setModel(DashScopeModelName.QWEN_MAX);
        llm.setMaxTokens(1024);
        llm.setTemperature(0d);

        List<BaseTool> baseTools = new ArrayList<>();
        ApiLogTool apiLogTool = new ApiLogTool();
        baseTools.add(apiLogTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeStructredAgentWithParser(baseTools, llm, new StructuredChatGlmOutputParser());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", "我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_openai_ApiLogTool() {
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.addHandler(new StdOutCallbackHandler());

        ChatOpenAI llm = new ChatOpenAI();

        List<BaseTool> baseTools = new ArrayList<>();
        ApiLogTool apiLogTool = new ApiLogTool();
        baseTools.add(apiLogTool);
        ClearAccessCountTool clearAccessCountTool = new ClearAccessCountTool();
        baseTools.add(clearAccessCountTool);
        AppMonitorTool appMonitorTool = new AppMonitorTool();
        baseTools.add(appMonitorTool);

//        ConversationSqliteMemory memory = new ConversationSqliteMemory(sqliteCache, "22");
//        ConversationRedisMemory memory = new ConversationRedisMemory(redisCache, "6");
//        memory.setHumanPrefix("Question");
//        memory.setAiPrefix("Thought");
//        memory.setToolPrefix("Observation");
//        memory.setMemoryKey("chat_history");
//        memory.setIgnoreHuman(true);

        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm,
                null,
                false, null, callbackManager);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_xinghuo_RealTimeCongestIdxTool() {
        ChatXingHuo llm = new ChatXingHuo();

        List<BaseTool> baseTools = new ArrayList<>();
        RealTimeCongestIdxTool realTimeCongestIdxTool = new RealTimeCongestIdxTool();
        baseTools.add(realTimeCongestIdxTool);
        ReasonTrafficTool reasonTrafficTool = new ReasonTrafficTool();
        baseTools.add(reasonTrafficTool);
        SuggestTrafficTool suggestTrafficTool = new SuggestTrafficTool();
        baseTools.add(suggestTrafficTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm,
                null,
                true);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "北京6月1日的拥堵指数是多少？并造成拥堵的原因是什么？并且给出适当的出行建议？");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_xinghuo_ApiLogTool() {
        ChatXingHuo llm = new ChatXingHuo();

        List<BaseTool> baseTools = new ArrayList<>();
        ApiLogTool apiLogTool = new ApiLogTool();
        baseTools.add(apiLogTool);
        ClearAccessCountTool clearAccessCountTool = new ClearAccessCountTool();
        baseTools.add(clearAccessCountTool);
        AppMonitorTool appMonitorTool = new AppMonitorTool();
        baseTools.add(appMonitorTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm,
                null,
                true);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？");
        inputs.put("input", "我有一个应用请求被流控了,appkey是12345678,可以清除限制不？");
//        inputs.put("input", "我想看应用的健康情况,appkey是12345678");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_xinghuo_RealTimeCongestIdxTool2() {
        ChatXingHuo llm = new ChatXingHuo();

        List<BaseTool> baseTools = new ArrayList<>();
        RealTimeCongestIdxTool realTimeCongestIdxTool = new RealTimeCongestIdxTool();
        baseTools.add(realTimeCongestIdxTool);
        ReasonTrafficTool reasonTrafficTool = new ReasonTrafficTool();
        baseTools.add(reasonTrafficTool);
        SuggestTrafficTool suggestTrafficTool = new SuggestTrafficTool();
        baseTools.add(suggestTrafficTool);

//        String example = "Example #1\n" +
//                "Question: 上海思明区拥堵指标?\n" +
//                "Thought: 使用 historyCongestTool 获取数据\n" +
//                "Action:\n" +
//                "```\n" +
//                "{{{{\n" +
//                "  \"action\": \"historyCongestTool\",\n" +
//                "  \"action_input\": {\n" +
//                "    \"city\": \"上海思明区\",\n" +
//                "    \"level\": \"0\"\n" +
//                "  }\n" +
//                "}}}}\n" +
//                "```\n" +
//                "\n" +
//                "Example #2\n" +
//                "Question: 浙江余杭区拥堵指数是多少?\n" +
//                "Thought: 使用 historyCongestTool 获取数据\n" +
//                "Action:\n" +
//                "```\n" +
//                "{{{{\n" +
//                "  \"action\": \"historyCongestTool\",\n" +
//                "  \"action_input\": {\n" +
//                "    \"city\": \"杭州余杭区\",\n" +
//                "    \"level\": \"0\"\n" +
//                "  }\n" +
//                "}}}}\n" +
//                "```\n" +
//                "Observation: [{\"拥堵指数\":\"1.66\",\"路况状态\":\"路况缓行\",\"速度\":\"26.60公⾥/⼩时\",\"城市\":\"北京市\"}]\n" +
//                "Thought:我已经知道答案了\n" +
//                "Action:\n" +
//                "```\n" +
//                "{{{{\n" +
//                "  \"action\": \"Final Answer\",\n" +
//                "  \"action_input\": \"拥堵指数1.66，路况十分缓慢并且拥堵\"\n" +
//                "}}}}";

        String example = "以下是该工具的示例\n" +
                        "#示例开始\n" +
                        "示例1\n" +
                        "Question: 浙江余杭区拥堵指数是多少?\n" +
                        "Thought: 使用 historyCongestTool 获取数据\n" +
                        "Action:\n" +
                        "```\n" +
                        "{{{{\n" +
                        "  \"action\": \"historyCongestTool\",\n" +
                        "  \"action_input\": {\n" +
                        "    \"city\": \"杭州余杭区\",\n" +
                        "    \"level\": \"4\"\n" +
                        "  }\n" +
                        "}}}}\n" +
                        "```\n" +
                        "Observation: [{\"拥堵指数\":\"1.66\",\"路况状态\":\"路况缓行\",\"速度\":\"26.60公⾥/⼩时\",\"城市\":\"北京市\"}]\n" +
                        "Thought:我已经知道答案了\n" +
                        "Action:\n" +
                        "```\n" +
                        "{{{{\n" +
                        "  \"action\": \"Final Answer\",\n" +
                        "  \"action_input\": \"拥堵指数1.66，路况十分缓慢并且拥堵\"\n" +
                        "}}}}\n"+
                        "```\n" +
                        "#示例结束";

        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm,
                null,
                true,
                new ChatXingHuoStructuredChatAgentV2(),
                example, null);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "北京海淀区拥堵指数是多少?");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_openai_RealTimeCongestIdxTool2() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        List<BaseTool> baseTools = new ArrayList<>();
        RealTimeCongestIdxTool realTimeCongestIdxTool = new RealTimeCongestIdxTool();
        baseTools.add(realTimeCongestIdxTool);
        ReasonTrafficTool reasonTrafficTool = new ReasonTrafficTool();
        baseTools.add(reasonTrafficTool);
        SuggestTrafficTool suggestTrafficTool = new SuggestTrafficTool();
        baseTools.add(suggestTrafficTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeStructuredChatAgentV2(baseTools, llm,
                null,
                true);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "北京海淀区拥堵指数是多少?");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }
}

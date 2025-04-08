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
import com.alibaba.langengine.core.agent.planexecute.PlanAndExecute;
import com.alibaba.langengine.core.agent.planexecute.Utils;
import com.alibaba.langengine.core.agent.planexecute.executors.ChainExecutor;
import com.alibaba.langengine.core.agent.planexecute.planners.LLMPlanner;
import com.alibaba.langengine.demo.agent.tool.*;
import com.alibaba.langengine.core.chain.llmmath.LLMMathChain;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.google.GoogleSearchAPITool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计划和执行agent 计划和执行agent通过首先计划要做什么，然后执行子任务来完成目标
 *
 * @author xiaoxuan.lp
 */
public class PlanAndExecuteTest {

    @Test
    public void test() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        LLMMathChain llmMathChain = LLMMathChain.fromLlm(llm, null);

        List<BaseTool> tools = new ArrayList<>();
        LLMMathTool calculator = new LLMMathTool();
        calculator.setName("Calculator");
        calculator.setDescription("useful for when you need to answer questions about math");
        calculator.setFunc(llmMathChain::call);
        tools.add(calculator);

        GoogleSearchAPITool search = new GoogleSearchAPITool();
        search.setName("Search");
        search.setDescription("useful for when you need to answer questions about current events");
        tools.add(search);

        LLMPlanner planner = Utils.loadChatPlanner(llm, null);
        ChainExecutor executor = Utils.loadAgentExecutor(llm, tools, null);

        PlanAndExecute agent = new PlanAndExecute();
        agent.setPlanner(planner);
        agent.setExecutor(executor);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Who is Leo DiCaprio's girlfriend? What is her current age raised to the 0.43 power?");
        Map<String, Object> response = agent.run(inputs);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_2() {
        List<BaseTool> tools = new ArrayList<>();
        DayOfMonthYongduTool dayOfMonthYongduTool = new DayOfMonthYongduTool();
        tools.add(dayOfMonthYongduTool);
        QuotaYongduTool quotaYongduTool = new QuotaYongduTool();
        tools.add(quotaYongduTool);

        ChatModelOpenAI llm = new ChatModelOpenAI();

        LLMPlanner planner = Utils.loadChatPlanner(llm, null);
        ChainExecutor executor = Utils.loadAgentExecutor(llm, tools, null);

        PlanAndExecute agent = new PlanAndExecute();
        agent.setPlanner(planner);
        agent.setExecutor(executor);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "杭州市8月份哪一天交通最拥堵？并分析这一天拥堵的原因");
        Map<String, Object> response = agent.run(inputs);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_3() {
        List<BaseTool> tools = new ArrayList<>();
        LLMMathTool llmMathTool = new LLMMathTool();
        tools.add(llmMathTool);
        SearchAPITool searchAPITool = new SearchAPITool();
        tools.add(searchAPITool);

        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        LLMPlanner planner = Utils.loadChatPlanner(llm, null);
        ChainExecutor executor = Utils.loadAgentExecutor(llm, tools, null);

        PlanAndExecute agent = new PlanAndExecute();
        agent.setPlanner(planner);
        agent.setExecutor(executor);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "计算3+5结果，并且将结果作为月份，计算该月份是什么星座");
        Map<String, Object> response = agent.run(inputs);
        System.out.println("response:" + JSON.toJSONString(response));
    }
}

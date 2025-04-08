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
import com.alibaba.langengine.core.agent.semantickernel.planning.ActionPlanner;
import com.alibaba.langengine.core.agent.semantickernel.planning.BasicPlanner;
import com.alibaba.langengine.core.agent.semantickernel.planning.SequentialPlanner;
import com.alibaba.langengine.core.agent.semantickernel.planning.StepwisePlanner;
import com.alibaba.langengine.demo.agent.skills.*;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ToolLoaders;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticKernelAgentTest {

    @Test
    public void test_BasicPlanner() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        List<BaseTool> baseTools = new ArrayList<>();
        WriterBrainstormSkill writerBrainstormSkill = new WriterBrainstormSkill();
        baseTools.add(writerBrainstormSkill);
        WriterTranslateSkill writerTranslateSkill = new WriterTranslateSkill();
        baseTools.add(writerTranslateSkill);

        AgentExecutor agentExecutor = ToolLoaders.initializeSemanticKernelAgent(baseTools, llm, new BasicPlanner(false));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Tomorrow is Valentine's day. I need to come up with a few date ideas. She speaks Chinese so write it in Chinese.");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_SequentialPlanner() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        List<BaseTool> baseTools = new ArrayList<>();
        SummarizeSkill summarizeSkill = new SummarizeSkill();
        baseTools.add(summarizeSkill);
        WriterBrainstormSkill writerBrainstormSkill = new WriterBrainstormSkill();
        baseTools.add(writerBrainstormSkill);
        WriterTranslateSkill writerTranslateSkill = new WriterTranslateSkill();
        baseTools.add(writerTranslateSkill);
        ShakespeareSkill shakespeareSkill = new ShakespeareSkill();
        baseTools.add(shakespeareSkill);

        AgentExecutor agentExecutor = ToolLoaders.initializeSemanticKernelAgent(baseTools, llm, new SequentialPlanner(true));
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "Tomorrow is Valentine's day. I need to come up with a few date ideas.\n" +
//                "She likes Shakespeare so write using his style.\n" +
//                "She speaks Chinese so write it in Chinese.");
//        inputs.put("input", "Tomorrow is Valentine's day. I need to come up with a few date ideas.\n" +
//                "She likes Shakespeare so write using his style. She speaks Chinese so write it in Chinese.");
        inputs.put("input", "Write a poem about Edson Arantes do Nasciment, then translate it into Chinese.");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_StepwisePlanner() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        List<BaseTool> baseTools = new ArrayList<>();
        WriterBrainstormSkill writerBrainstormSkill = new WriterBrainstormSkill();
        baseTools.add(writerBrainstormSkill);
        WriterTranslateSkill writerTranslateSkill = new WriterTranslateSkill();
        baseTools.add(writerTranslateSkill);

        AgentExecutor agentExecutor = ToolLoaders.initializeSemanticKernelAgent(baseTools, llm, new StepwisePlanner(true));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Tomorrow is my girlfriend's birthday. I need to come up with some date ideas. She speaks Chinese, so she writes in Chinese.");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_ActionPlanner() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        List<BaseTool> baseTools = new ArrayList<>();
        MathAddSkill mathAddSkill = new MathAddSkill();
        baseTools.add(mathAddSkill);

        AgentExecutor agentExecutor = ToolLoaders.initializeSemanticKernelAgent(baseTools, llm, new ActionPlanner(true));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "What is the sum of 110.34 and 990?");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_openai_BasicPlanner() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        List<BaseTool> baseTools = new ArrayList<>();
        ApiLogTool apiLogTool = new ApiLogTool();
        baseTools.add(apiLogTool);
        ClearAccessCountTool clearAccessCountTool = new ClearAccessCountTool();
        baseTools.add(clearAccessCountTool);
        AppMonitorTool appMonitorTool = new AppMonitorTool();
        baseTools.add(appMonitorTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeSemanticKernelAgent(baseTools, llm, new BasicPlanner(true));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？并根据requestId找到对应的appkey帮我清除流量吧");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }

    @Test
    public void test_openai_SequentialPlanner() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        List<BaseTool> baseTools = new ArrayList<>();
        ApiLogTool apiLogTool = new ApiLogTool();
        baseTools.add(apiLogTool);
        ClearAccessCountTool clearAccessCountTool = new ClearAccessCountTool();
        baseTools.add(clearAccessCountTool);
        AppMonitorTool appMonitorTool = new AppMonitorTool();
        baseTools.add(appMonitorTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeSemanticKernelAgent(baseTools, llm, new SequentialPlanner());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "我有一个请求,requestId是16lxqklu2vlaj,请问具体的调用详情能告诉我么？");
        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(response.get("output"));
    }
}

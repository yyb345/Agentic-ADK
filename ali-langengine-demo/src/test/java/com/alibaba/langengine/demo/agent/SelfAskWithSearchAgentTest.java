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
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ToolLoaders;
import com.alibaba.langengine.tool.google.SerpapiTool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自问自答，此代理只使用一个工具: Intermediate Answer,
 * 它会为问题寻找事实答案(指的非 gpt 生成的答案, 而是在网络中,文本中已存在的), 如 Google search API 工具
 *
 * @author xiaoxuan.lp
 */
public class SelfAskWithSearchAgentTest {

    @Test
    public void test_run() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        List<BaseTool> tools = new ArrayList<>();
        SerpapiTool search = new SerpapiTool();
        search.setName("Intermediate Answer");
        search.setDescription("useful for when you need to ask with search");
        tools.add(search);

        AgentExecutor agentExecutor = ToolLoaders.initializeSelfAskWithSearchAgent(tools, llm, false);

        Map<String, Object> response;
        Map<String, Object> inputs = new HashMap<>();

        inputs.put("input", "阿里巴巴的创始人的家乡是哪里？");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));
    }
}

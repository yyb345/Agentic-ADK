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
package com.alibaba.langengine.core.tool;

import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.mrkl.ZeroShotAgent;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;

import java.util.List;

/**
 * 工具加载器
 *
 * @author xiaoxuan.lp
 */
public class ToolCoreLoaders {

    public static AgentExecutor initializeAgent(List<BaseTool> tools, BaseLanguageModel llm) {
        return initializeAgent(tools, llm, false);
    }

    public static AgentExecutor initializeAgent(List<BaseTool> tools, BaseLanguageModel llm, boolean isCH) {
        AgentExecutor agentExecutor = new AgentExecutor();
        ZeroShotAgent agent = new ZeroShotAgent();
        agent.setLlm(llm);
        agent.setTools(tools);
        agent.init(isCH);
        agentExecutor.setAgent(agent);
        agentExecutor.setTools(tools);
        return agentExecutor;
    }
}

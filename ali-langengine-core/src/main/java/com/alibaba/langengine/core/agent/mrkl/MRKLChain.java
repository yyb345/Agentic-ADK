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
package com.alibaba.langengine.core.agent.mrkl;

import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.DefaultTool;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain that implements the MRKL system
 *
 * @author xiaoxuan.lp
 */
@Data
public class MRKLChain extends AgentExecutor {

    private BaseLanguageModel llm;
    private List<ChainConfig> chains;

    public MRKLChain() {
        List<BaseTool> tools = new ArrayList<>();
        for (ChainConfig chain : chains) {
            DefaultTool tool = new DefaultTool();
            tool.setName(chain.getActionName());
            tool.setDescription(chain.getActionDescription());
            tools.add(tool);
        }
        ZeroShotAgent agent = new ZeroShotAgent();
        agent.setLlm(llm);
        agent.setTools(tools);
        agent.init(false);
        setAgent(agent);
    }
}

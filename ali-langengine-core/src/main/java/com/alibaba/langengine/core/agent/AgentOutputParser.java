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
package com.alibaba.langengine.core.agent;

import com.alibaba.langengine.core.outputparser.BaseOutputParser;

import java.util.HashMap;
import java.util.Map;

/**
 * 将文本解析为代理操作/完成
 *
 * @author xiaoxuan.lp
 */
public abstract class AgentOutputParser<T> extends BaseOutputParser {

    public abstract T parse(String text);

    public AgentFinish getAgentFinish(String text) {
        Map<String, Object> returnValues = new HashMap<>();
        returnValues.put("output", text);
        AgentFinish agentFinish = new AgentFinish();
        agentFinish.setReturnValues(returnValues);
        agentFinish.setLog(text);
        return agentFinish;
    }
}

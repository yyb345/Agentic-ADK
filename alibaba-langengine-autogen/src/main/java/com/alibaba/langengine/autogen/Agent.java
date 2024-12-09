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
package com.alibaba.langengine.autogen;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.tool.StructuredTool;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * AI代理的抽象类。
 * 代理可以与其他代理进行通信并执行操作。
 * 不同的代理在“receive”方法中执行的操作可能有所不同。
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class Agent {

    /**
     * name of the agent.
     */
    private String name;

    private String systemMessage;

    private BaseLanguageModel llm;

    private List<StructuredTool> tools;

    public Agent(String name, BaseLanguageModel llm) {
        setName(name);
        setLlm(llm);
    }

    public abstract boolean canExecuteFunction(String functionName);

    public abstract Map<String, Function<Object, String>> getFunctionMap();

    /**
     * Send a message to another agent.
     *
     * @param message
     * @param recipient
     * @param requestReply
     * @param silent
     */
    public abstract boolean send(Object message, Agent recipient, Boolean requestReply, Boolean silent);

    /**
     * Receive a message from another agent.
     *
     * @param message
     * @param sender
     * @param requestReply
     * @param silent
     */
    public abstract void receive(Object message, Agent sender, Boolean requestReply, Boolean silent);

    /**
     * Reset the agent.
     */
    public abstract void reset();

    /**
     * Generate a reply based on the received messages.
     *
     * @param messages
     * @param sender
     * @return
     */
    public abstract Object generateReply(List<Map<String, Object>> messages, Agent sender);

    /**
     * 清空历史对话
     * @param agent
     */
    public abstract void clearHistory(Agent agent);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        // 比较对应字段是否相等
        return Objects.equals(getName(), agent.getName());
    }
}

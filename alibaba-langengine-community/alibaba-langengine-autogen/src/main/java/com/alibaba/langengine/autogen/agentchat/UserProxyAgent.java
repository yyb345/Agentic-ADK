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
package com.alibaba.langengine.autogen.agentchat;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;

import java.util.Map;

/**
 * A proxy agent for the user, that can execute code and provide feedback to the other agents.
 *
 * UserProxyAgent is a subclass of ConversableAgent configured with `human_input_mode` to ALWAYS
 * and `llm_config` to False. By default, the agent will prompt for human input every time a message is received.
 * Code execution is enabled by default. LLM-based auto reply is disabled by default.
 * To modify auto reply, register a method with [`register_reply`](conversable_agent#register_reply).
 * To modify the way to get human input, override `get_human_input` method.
 * To modify the way to execute code blocks, single code block, or function call, override `execute_code_blocks`,
 * `run_code`, and `execute_function` methods respectively.
 * To customize the initial message when a conversation starts, override `generate_init_message` method.
 */
public class UserProxyAgent extends ConversableAgent {

    public UserProxyAgent(String name, BaseLanguageModel llm) {
        this(name, llm, null);
    }

    public UserProxyAgent(String name, BaseLanguageModel llm, String systemMessage, Map<String, Object> codeExecutionConfig) {
        super(name,
                llm,
                systemMessage,
                null,
                "ALWAYS",
                null,
                codeExecutionConfig,
                null,
                ""
        );
    }

    public UserProxyAgent(String name, BaseLanguageModel llm, Map<String, Object> codeExecutionConfig) {
        super(name,
                llm,
                "",
                null,
                "ALWAYS",
                null,
                codeExecutionConfig,
                null,
                ""
        );
    }
}

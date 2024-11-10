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
import com.alibaba.langengine.core.tool.StructuredTool;

import java.util.List;

/**
 * Assistant agent, designed to solve a task with LLM.
 *
 * AssistantAgent is a subclass of ConversableAgent configured with a default system message.
 * The default system message is designed to solve a task with LLM,
 * including suggesting python code blocks and debugging.
 * `human_input_mode` is default to "NEVER"
 * and `code_execution_config` is default to False.
 * This agent doesn't execute code by default, and expects the user to execute the code.
 *
 * @author xiaoxuan.lp
 */
public class AssistantAgent extends ConversableAgent {

    private static final String DEFAULT_SYSTEM_MESSAGE = "You are a helpful AI assistant.\n" +
            "Solve tasks using your coding and language skills.\n" +
            "In the following cases, suggest python code (in a python coding block) or shell script (in a sh coding block) for the user to execute.\n" +
            "    1. When you need to collect info, use the code to output the info you need, for example, browse or search the web, download/read a file, print the content of a webpage or a file, get the current date/time, check the operating system. After sufficient info is printed and the task is ready to be solved based on your language skill, you can solve the task by yourself.\n" +
            "    2. When you need to perform some task with code, use the code to perform the task and output the result. Finish the task smartly.\n" +
            "Solve the task step by step if you need to. If a plan is not provided, explain your plan first. Be clear which step uses code, and which step uses your language skill.\n" +
            "When using code, you must indicate the script type in the code block. The user cannot provide any other feedback or perform any other action beyond executing the code you suggest. The user can't modify your code. So do not suggest incomplete code which requires users to modify. Don't use a code block if it's not intended to be executed by the user.\n" +
            "If you want the user to save the code in a file before executing it, put # filename: <filename> inside the code block as the first line. Don't include multiple code blocks in one response. Do not ask users to copy and paste the result. Instead, use 'print' function for the output when relevant. Check the execution result returned by the user.\n" +
            "If the result indicates there is an error, fix the error and output the code again. Suggest the full code instead of partial code or code changes. If the error can't be fixed or if the task is not solved even after the code is executed successfully, analyze the problem, revisit your assumption, collect additional info you need, and think of a different approach to try.\n" +
            "When you find an answer, verify the answer carefully. Include verifiable evidence in your response if possible.\n" +
            "Reply \"TERMINATE\" in the end when everything is done.";

    public AssistantAgent(String name, BaseLanguageModel llm) {
        super(name,
                llm,
                DEFAULT_SYSTEM_MESSAGE,
                null,
                "NEVER",
                null,
                null);
    }

    public AssistantAgent(String name, BaseLanguageModel llm, List<StructuredTool> tools) {
        super(name,
                llm,
                DEFAULT_SYSTEM_MESSAGE,
                null,
                "NEVER",
                null,
                null);
        if(tools != null) {
            this.setTools(tools);
        }
    }

    public AssistantAgent(String name, BaseLanguageModel llm, String systemMessage, List<StructuredTool> tools) {
        super(name,
                llm,
                systemMessage,
                null,
                "NEVER",
                null,
                null);
        if(tools != null) {
            this.setTools(tools);
        }
    }
}

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
package com.alibaba.langengine.dashscope.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.outputparser.StrOutputParser;
import com.alibaba.langengine.core.prompt.ChatPromptValue;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableInterface;
import com.alibaba.langengine.core.runnables.Runnable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashScopeLLMTest {

    @Test
    public void test_branching_and_merging() {
        //model
        DashScopeLLM model = new DashScopeLLM();
        model.setModel("qwen-max");

        RunnableInterface planner = Runnable.sequence(
                ChatPromptTemplate.fromTemplate("Generate an argument about: {input}, please use chinese"),
                model,
                new StrOutputParser(),
                new RunnableHashMap() {{
                    put("base_response", Runnable.passthrough());
                }}
        );

        RunnableInterface arguments_for = Runnable.sequence(
                ChatPromptTemplate.fromTemplate("List the pros or positive aspects of {base_response}, please use chinese"),
                model,
                new StrOutputParser()
        );

        RunnableInterface arguments_against = Runnable.sequence(
                ChatPromptTemplate.fromTemplate("List the cons or negative aspects of {base_response}, please use chinese"),
                model,
                new StrOutputParser()
        );

        List<Object> messages = new ArrayList<>();
        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent("{original_response}");
        messages.add(aiMessage);
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("Pros:\n{results_1}\n\nCons:\n{results_2}");
        messages.add(humanMessage);
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("Generate a final response given the critique, please use chinese");
        messages.add(systemMessage);

        RunnableInterface final_responder = Runnable.sequence(
                ChatPromptTemplate.fromMessages(messages),
                model,
                new StrOutputParser()
        );

        RunnableInterface chain = Runnable.sequence(
                planner,
                Runnable.parallel(
                        new RunnableHashMap() {{
                            put("results_1", arguments_for);
                        }},
                        new RunnableHashMap() {{
                            put("results_2", arguments_against);
                        }}
                ),
                Runnable.assign(new HashMap<String, Object>() {{
                    put("original_response", "base_response");
                }}),
                final_responder
        );

        Object runnableOutput = chain.invoke(new RunnableHashMap() {{
            put("input", "scrum");
        }});
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_predict() {
        // success
        DashScopeLLM llm = new DashScopeLLM();
        System.out.println("response:" + llm.predict("你是谁？"));
    }

    @Test
    public void test_predict_stream() {
        // success
        DashScopeLLM llm = new DashScopeLLM();
        llm.setStream(true);
        System.out.println("response:" + llm.predict("你是谁？"));
    }

    @Test
    public void test_predict_token_init() {
        // success
        String token = System.getenv("DASH_SCOPE_API");
        DashScopeLLM llm = new DashScopeLLM(token);
        System.out.println("response:" + llm.predict("你是谁？"));
    }

    @Test
    public void test_generate_prompt() {
        // success
        String token = System.getenv("DASH_SCOPE_API");
        DashScopeChatModel llm = new DashScopeChatModel(token);
        llm.setModel("qwen-72b-chat");

        List<PromptValue> promptValueList = new ArrayList<>();
        ChatPromptValue promptValue = new ChatPromptValue();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("You are a helpful AI assistant.\nSolve tasks using your coding and language skills.\nIn the following cases, suggest python code (in a python coding block) or shell script (in a sh coding block) for the user to execute.\n    1. When you need to collect info, use the code to output the info you need, for example, browse or search the web, download/read a file, print the content of a webpage or a file, get the current date/time, check the operating system. After sufficient info is printed and the task is ready to be solved based on your language skill, you can solve the task by yourself.\n    2. When you need to perform some task with code, use the code to perform the task and output the result. Finish the task smartly.\nSolve the task step by step if you need to. If a plan is not provided, explain your plan first. Be clear which step uses code, and which step uses your language skill.\nWhen using code, you must indicate the script type in the code block. The user cannot provide any other feedback or perform any other action beyond executing the code you suggest. The user can\'t modify your code. So do not suggest incomplete code which requires users to modify. Don\'t use a code block if it\'s not intended to be executed by the user.\nIf you want the user to save the code in a file before executing it, put # filename: <filename> inside the code block as the first line. Don\'t include multiple code blocks in one response. Do not ask users to copy and paste the result. Instead, use \'print\' function for the output when relevant. Check the execution result returned by the user.\nIf the result indicates there is an error, fix the error and output the code again. Suggest the full code instead of partial code or code changes. If the error can\'t be fixed or if the task is not solved even after the code is executed successfully, analyze the problem, revisit your assumption, collect additional info you need, and think of a different approach to try.\nWhen you find an answer, verify the answer carefully. Include verifiable evidence in your response if possible.\nReply \"TERMINATE\" in the end when everything is done.\n");
        promptValue.getMessages().add(systemMessage);
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("1+1等于几");
        promptValue.getMessages().add(humanMessage);
        promptValueList.add(promptValue);

        System.out.println("response:" + JSON.toJSONString(llm.generatePrompt(promptValueList, null)));
    }
}

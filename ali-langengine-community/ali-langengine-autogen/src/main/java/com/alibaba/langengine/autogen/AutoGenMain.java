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

import com.alibaba.langengine.autogen.agentchat.AssistantAgent;
import com.alibaba.langengine.autogen.agentchat.GroupChat;
import com.alibaba.langengine.autogen.agentchat.GroupChatManager;
import com.alibaba.langengine.autogen.agentchat.UserProxyAgent;
import com.alibaba.langengine.autogen.tools.CircumferenceTool;
import com.alibaba.langengine.autogen.tools.ExpertServiceTool;
import com.alibaba.langengine.autogen.tools.ReadFileTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoGenMain {

    public static void main(String[] args) {
//        getting_started();
//        agentchat_auto_feedback_from_code_execution();
//        agentchat_human_feedback();
//        agentchat_two_users();
//        agentchat_groupchat();
        agentchat_langchain();
    }

    /**
     * https://microsoft.github.io/autogen/docs/Getting-Started
     */
    private static void getting_started() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        AssistantAgent assistant = new AssistantAgent("assistant", llm);
        UserProxyAgent userProxy = new UserProxyAgent("user_proxy", llm, new HashMap<String, Object>() {{
            put("work_dir", "coding");
        }});
//        userProxy.initiateChat(assistant, "Plot a chart of NVDA and TESLA stock price change YTD.");
//        userProxy.initiateChat(assistant, "帮我随机画一个饼状图");
        userProxy.initiateChat(assistant, "根据斐波那契数列生成柱状图，只生成前20个");
//        userProxy.initiateChat(assistant, "What date is today? 并且今天月份的2次方是多少");
//        userProxy.initiateChat(assistant, "绘制一个小房子");
    }

    /**
     * https://github.com/microsoft/autogen/blob/main/notebook/agentchat_auto_feedback_from_code_execution.ipynb
     */
    private static void agentchat_auto_feedback_from_code_execution() {
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        AssistantAgent assistant = new AssistantAgent("assistant", llm);
        UserProxyAgent userProxy = new UserProxyAgent("user_proxy", llm, new HashMap<String, Object>() {{
            put("work_dir", "coding");
        }});
        userProxy.setHumanInputMode("NEVER");
        userProxy.setMaxConsecutiveAutoReply(10);
        userProxy.initiateChat(assistant, "What date is today? Compare the year-to-date gain for META and TESLA.");
    }

    /**
     * https://github.com/microsoft/autogen/blob/main/notebook/agentchat_human_feedback.ipynb
     */
    private static void agentchat_human_feedback() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        AssistantAgent assistant = new AssistantAgent("assistant", llm);
        UserProxyAgent userProxy = new UserProxyAgent("user_proxy", llm, new HashMap<String, Object>() {{
            put("work_dir", "coding");
        }});

//        String math_problem_to_solve = "Find $a + b + c$, given that $x+y \\neq -1$ and \n" +
//                "\\begin{align}\n" +
//                "   ax + by + c & = x + 7,\\\\\n" +
//                "   a + bx + cy & = 2x + 6y,\\\\\n" +
//                "   ay + b + cx & = 4x + y.\n" +
//                "\\end{align}.";

        String math_problem_to_solve = "x * y = 8\n" +
                "x / y = 2\n" +
                "\n" +
                "x和y是正整数，求解x和y的值";

        userProxy.initiateChat(assistant, math_problem_to_solve);
    }

    /**
     * https://github.com/microsoft/autogen/blob/main/notebook/agentchat_RetrieveChat.ipynb
     */
    private static void agentchat_retrieveChat() {
        // TODO ...
    }

    /**
     * https://github.com/microsoft/autogen/blob/main/notebook/agentchat_two_users.ipynb
     */
    private static void agentchat_two_users() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        List<StructuredTool> tools = new ArrayList<>();
        ExpertServiceTool expertServiceTool = new ExpertServiceTool();
        tools.add(expertServiceTool);
        AssistantAgent assistant_for_student = new AssistantAgent("assistant_for_student",
                llm,
                "You are a helpful assistant. 你只会做小学一年级加减的题目，不会做的可以咨询专家。Reply TERMINATE when the task is done.",
                tools);

        UserProxyAgent student = new UserProxyAgent("student", llm, new HashMap<String, Object>() {{
            put("work_dir", "student");
        }});
        student.setHumanInputMode("TERMINATE");
        student.setMaxConsecutiveAutoReply(10);
        student.setToolMap(new HashMap<String, StructuredTool>() {{ put("ask_expert", expertServiceTool); }});

        String question = "x * y = 8\n" +
                "x / y = 2\n" +
                "\n" +
                "x和y是正整数，求解x和y的值";

        student.initiateChat(assistant_for_student, question);
    }

    /**
     * https://github.com/microsoft/autogen/blob/main/notebook/agentchat_groupchat.ipynb
     */
    private static void agentchat_groupchat() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        UserProxyAgent user_proxy = new UserProxyAgent("User_proxy",  llm, "A human admin.", new HashMap<String, Object>() {{
            put("work_dir", "groupchat");
            put("last_n_messages", 2);
        }});
        user_proxy.setHumanInputMode("TERMINATE");

        AssistantAgent coder = new AssistantAgent("Coder", llm);
        AssistantAgent pm = new AssistantAgent("Product_manager", llm, "Creative in software product ideas.", null);

        List<Agent> agents = new ArrayList<>();
        agents.add(user_proxy);
        agents.add(coder);
        agents.add(pm);
        GroupChat groupChat = new GroupChat();
        groupChat.setAgents(agents);
        groupChat.setMessages(new ArrayList<>());
        groupChat.setMaxRound(12);

        GroupChatManager manager = new GroupChatManager("chat_manager", llm, groupChat);

//        String question = "Find a latest paper about gpt-4 on arxiv and find its potential applications in software.";
//        String question = "我想实现一个python语言打印\"hello world\"的控制台应用程序，该怎么做";
//        String question = "我想实现一个图书管理系统，该怎么做";
        String question = "帮我执行获取当前目录的上一级目录的所有文件，通过shell指令";
        user_proxy.initiateChat(manager, question);
    }

    /**
     * https://github.com/microsoft/autogen/blob/main/notebook/agentchat_langchain.ipynb
     */
    private static void agentchat_langchain() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        List<StructuredTool> tools = new ArrayList<>();
        CircumferenceTool circumferenceTool = new CircumferenceTool();
        tools.add(circumferenceTool);
        ReadFileTool readFileTool = new ReadFileTool();
        tools.add(readFileTool);

        UserProxyAgent user_proxy = new UserProxyAgent("user_proxy", llm, new HashMap<String, Object>() {{
            put("work_dir", "coding");
        }});
        user_proxy.setHumanInputMode("NEVER");
        user_proxy.setMaxConsecutiveAutoReply(10);
        user_proxy.setToolMap(new HashMap<String, StructuredTool>() {{
            put(circumferenceTool.getName(), circumferenceTool);
            put(readFileTool.getName(), readFileTool);
        }});

        AssistantAgent chatbot = new AssistantAgent("chatbot",
                llm,
                "For coding tasks, only use the functions you have been provided with. Reply TERMINATE when the task is done.",
                tools);

        String question = "Read the file with the path 'radius.txt', then calculate the circumference of a circle that has a radius of that files contents.";

        user_proxy.initiateChat(chatbot, question);
    }
}

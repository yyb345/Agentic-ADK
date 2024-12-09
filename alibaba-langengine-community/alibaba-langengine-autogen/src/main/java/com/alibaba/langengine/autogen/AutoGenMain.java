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

import com.alibaba.langengine.autogen.agentchat.*;
import com.alibaba.langengine.autogen.tools.CircumferenceTool;
import com.alibaba.langengine.autogen.tools.ExpertServiceTool;
import com.alibaba.langengine.autogen.tools.ReadFileTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AutoGenMain {

    public static void main(String[] args) {
        example1();
//        getting_started();
//        agentchat_auto_feedback_from_code_execution();
//        agentchat_human_feedback();
//        agentchat_two_users();
//        agentchat_groupchat();
//        agentchat_langchain();
    }

    private static void example1() {
        // quesition
        String question = "I have a number between 1 and 100. Guess it!";
        System.out.println("Question: " + question);

        // llm
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        // check result
        Predicate<Map<String, Object>> terminiationFunc = message -> {
            if(message.get("content") != null) {
                if(message.get("content").toString().contains("53")) {
                    return true;
                }
                return false;
            }
            return false;
        };

        ConversableAgent agentWithNumber = new ConversableAgent(
                "agent_with_number",
                llm,
                "You are playing a game of guess-my-number. You have the number 53 in your mind.",
                null, "NEVER");
        agentWithNumber.setIsTermination(terminiationFunc);

        ConversableAgent agentGuessNumber = new ConversableAgent(
                "agent_guess_number",
                llm,
                "I have a number in my mind, and you will try to guess it.",
                null, "NEVER");

        agentWithNumber.initiateChat(agentGuessNumber, question);
    }

    private static void example2() {
        // quesition
        String question = "今天杭州的天气如何，再帮我查下杭州的交通情况";
        System.out.println("Question: " + question);

        // llm
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        // check result
        Predicate<Map<String, Object>> terminiationFunc = message -> {
            if(message.get("content") != null) {
                if(message.get("content").toString().contains("2")) {
                    return true;
                }
                return false;
            }
            return false;
        };

        UserProxyAgent user_proxy = new UserProxyAgent("user_proxy",
                llm, "你是一个人类代理", null);
//        user_proxy.setIsTermination(terminiationFunc);
        user_proxy.setHumanInputMode("NEVER");

        AssistantAgent getWeatherAgent = new AssistantAgent(
                "getWeather",
                llm,
                "你是一个天气预报助理，只会搜索城市的天气，你知道今天杭州天气是10摄氏度",
                null);
        AssistantAgent transportationAgent = new AssistantAgent(
                "transportation",
                llm,
                "你是一个交通查询小助手，你知道杭州今天的交通是比较拥堵，注意错峰",
                null);

        List<Agent> agents = new ArrayList<>();
        agents.add(user_proxy);
        agents.add(getWeatherAgent);
        agents.add(transportationAgent);

        GroupChat groupChat = new GroupChat();
        groupChat.setAgents(agents);
        groupChat.setMessages(new ArrayList<>());
        groupChat.setMaxRound(5);

        GroupChatManager manager = new GroupChatManager("chat_manager", llm, groupChat);
//        manager.setIsTermination(terminiationFunc);
//        manager.setHumanInputMode("NEVER");
//        manager.setSystemMessage("如果得到想要的答案，就立即结束");
        user_proxy.initiateChat(manager, question);
    }

    private static void example3() {
        // quesition
        String question = "帮我计算 1+1 等于几";
        System.out.println("Question: " + question);

        // llm
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4);

        // check result
        Predicate<Map<String, Object>> terminiationFunc = message -> {
            if(message.get("content") != null) {
                if(message.get("content").toString().contains("2")) {
                    return true;
                }
                return false;
            }
            return false;
        };

        UserProxyAgent user_proxy = new UserProxyAgent("user_proxy",
                llm,
                "你是一名学生",
                null);
//        user_proxy.setIsTermination(terminiationFunc);
        user_proxy.setHumanInputMode("NEVER");

//        AssistantAgent stepAgent = new AssistantAgent(
//                "step",
//                llm,
//                "你擅长把数学公式拆解成加减乘除的步骤，不要告诉最终答案",
//                null);
        AssistantAgent additionAgent = new AssistantAgent(
                "addition",
                llm,
                "You are an adding calculator. ",
                null);
        AssistantAgent subtractionAgent = new AssistantAgent(
                "subtraction",
                llm,
                "You are a subtraction calculator. ",
                null);
        AssistantAgent multiplicationAgent = new AssistantAgent(
                "multiplication",
                llm,
                "You are a multiplication calculator. ",
                null);
        AssistantAgent divisionAgent = new AssistantAgent(
                "division",
                llm,
                "You are a division calculator. ",
                null);

        List<Agent> agents = new ArrayList<>();
        agents.add(user_proxy);
//        agents.add(stepAgent);
        agents.add(additionAgent);
        agents.add(subtractionAgent);
        agents.add(multiplicationAgent);
        agents.add(divisionAgent);

        GroupChat groupChat = new GroupChat();
        groupChat.setAgents(agents);
        groupChat.setMessages(new ArrayList<>());
        groupChat.setMaxRound(5);

        GroupChatManager manager = new GroupChatManager("chat_manager", llm, groupChat);
//        manager.setIsTermination(terminiationFunc);
//        manager.setHumanInputMode("NEVER");
//        manager.setSystemMessage("如果得到想要的答案，就立即结束");
        user_proxy.initiateChat(manager, question);
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
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

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
        String question = "我想实现一个图书管理系统，该怎么做";
//        String question = "帮我执行获取当前目录的上一级目录的所有文件，通过shell指令";
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

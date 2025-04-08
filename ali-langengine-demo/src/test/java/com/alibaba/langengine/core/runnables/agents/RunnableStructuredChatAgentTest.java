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
package com.alibaba.langengine.core.runnables.agents;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.outputparser.JsonAgentOutputParser;
import com.alibaba.langengine.core.outputparser.XMLAgentOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.tools.*;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RunnableStructuredChatAgentTest extends BaseTest {

    @Test
    public void test_with_xml() {
        //model
        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setModel(OpenAIModelConstants.GPT_4_TURBO);

        //chat_history
        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("16lxqklu2vlaj是什么？");
        messages.add(humanMessage);
        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent("16lxqklu2vlaj是一个requestId");
        messages.add(aiMessage);

        RunnableInterface modelBinding = model.bind(new RunnableHashMap() {{
            put("stop", Arrays.asList(new String[] { "</tool_input>", "</final_answer>" }));
        }});

        List<BaseTool> tools = new ArrayList<>();
        SearchTool searchTool = new SearchTool();
        tools.add(searchTool);
        ApiLogSimpleTool apiLogTool = new ApiLogSimpleTool();
        tools.add(apiLogTool);
        ApiSolutionTool apiSolutionTool = new ApiSolutionTool();
        tools.add(apiSolutionTool);

        String template = "You are a helpful assistant. Help the user answer any questions.\n" +
                "\n" +
                "You have access to the following tools:\n" +
                "\n" +
                "{tools}\n" +
                "\n" +
                "In order to use a tool, you can use <tool></tool> and <tool_input></tool_input> tags. You will then get back a response in the form <observation></observation>\n" +
                "For example, if you have a tool called 'search' that could run a google search, in order to search for the weather in SF you would respond:\n" +
                "\n" +
                "<tool>search</tool><tool_input>weather in SF</tool_input>\n" +
                "<observation>64 degrees</observation>\n" +
                "\n" +
                "When you are done, respond with a final answer between <final_answer></final_answer>. For example:\n" +
                "\n" +
                "<final_answer>The weather in SF is 64 degrees</final_answer>\n" +
                "\n" +
                "Begin!\n" +
                "\n" +
                "Previous Conversation:\n" +
                "{chat_history}\n" +
                "\n" +
                "Question: {input}\n" +
                "{agent_scratchpad}";
        Map<String, Object> args = new HashMap<>();
//        args.put("tools", convertTools(tools));
        args.put("chat_history", MessageConverter.getBufferString(messages));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertXmlIntermediateSteps(intermediateSteps)));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), new XMLAgentOutputParser());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools, t -> toolPromptTransform(t));

        String question = "服务不存在16lxqklu2vlaj错误具体原因是什么？并且如何解决该错误？";
        Object runnableOutput = agentExecutor.invoke(new RunnableHashMap() {{
            put("input", question);
            put("tools", convertTools(tools));
        }});
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_with_json() {
        //model
        ChatModelOpenAI model = new ChatModelOpenAI();

        //chat_history
        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("萧玄是谁？");
        messages.add(humanMessage);
        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent("萧玄负责LangEngine建设");
        messages.add(aiMessage);

        RunnableInterface modelBinding = model.bind(new RunnableHashMap() {{
            put("stop", Arrays.asList(new String[] { "Observation:" }));
        }});

        List<BaseTool> tools = new ArrayList<>();
        ApiLogTool apiLogTool = new ApiLogTool();
        tools.add(apiLogTool);
        ClearAccessCountTool clearAccessCountTool = new ClearAccessCountTool();
        tools.add(clearAccessCountTool);
        AppMonitorTool appMonitorTool = new AppMonitorTool();
        tools.add(appMonitorTool);

        String template = "Respond to the human as helpfully and accurately as possible. You have access to the following tools:\n" +
                "\n" +
                "{tools}\n" +
                "\n" +
                "Use a json blob to specify a tool by providing an action key (tool name) and an action_input key (tool input).\n" +
                "\n" +
                "Valid \"action\" values: \"Final Answer\" or {tool_names}\n" +
                "\n" +
                "Provide only ONE action per $JSON_BLOB, as shown:\n" +
                "\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": $TOOL_NAME,\n" +
                "  \"action_input\": $INPUT\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "Follow this format:\n" +
                "\n" +
                "Question: input question to answer\n" +
                "Thought: consider previous and subsequent steps\n" +
                "Action:\n" +
                "```\n" +
                "$JSON_BLOB\n" +
                "```\n" +
                "Observation: action result\n" +
                "... (repeat Thought/Action/Observation N times)\n" +
                "Thought: I know what to respond\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"Final Answer\",\n" +
                "  \"action_input\": \"Final response to human\"\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "\n" +
                "\n" +
                "Begin! Reminder to ALWAYS respond with a valid json blob of a single action. Use tools if necessary. Respond directly if appropriate. Format is Action:```$JSON_BLOB```then Observation:.\n" +
                "\n" +
                "Previous Conversation:\n" +
                "{chat_history}\n" +
                "\n" +
                "Question: {input}\n" +
                "Thought: {agent_scratchpad}";
        Map<String, Object> args = new HashMap<>();
//        args.put("tools", convertStructuredChatAgentTools(tools));
//        args.put("tool_names", convertToolNames(tools));
        args.put("chat_history", MessageConverter.getBufferString(messages));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertJsonIntermediateSteps(intermediateSteps)));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), new JsonAgentOutputParser());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools, t -> toolPromptStructuredChatTransform(t));
//        agentExecutor.setEarlyStoppingMethod(RunnableAgentExecutor.FORCE_STOPPING_METHOD);
//        agentExecutor.setForceStoppingContent("直接返回内容");

        String question = "刚才我在问谁，然后他负责什么？我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？两个问题请一起回答";
//        String question = "你是谁？";

        // invoke同步请求
//        Object runnableOutput = agentExecutor.invoke(new RunnableHashMap() {{
//            put("input", question);
//            put("tools", convertStructuredChatAgentTools(tools));
//            put("tool_names", convertToolNames(tools));
//        }});
//        System.out.println(JSON.toJSONString(runnableOutput));

        // 带callback log的stream异步请求
        Object runnableOutput = agentExecutor.streamLog(new RunnableHashMap() {{
            put("input", question);
            put("tools", convertStructuredChatAgentTools(tools));
            put("tool_names", convertToolNames(tools));
        }}, chunk -> chunkHandler(chunk));
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    private RunnableHashMap toolPromptTransform(List<BaseTool> tools) {
        RunnableHashMap runnableHashMap = new RunnableHashMap();
        runnableHashMap.put("tools", convertTools(tools));
        return runnableHashMap;
    }

    private RunnableHashMap toolPromptStructuredChatTransform(List<BaseTool> tools) {
        RunnableHashMap runnableHashMap = new RunnableHashMap();
        runnableHashMap.put("tools", convertStructuredChatAgentTools(tools));
        runnableHashMap.put("tool_names", convertToolNames(tools));
        return runnableHashMap;
    }
}

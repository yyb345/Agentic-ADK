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
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.outputparser.ActionLineOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ToolLoaders;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RunnableConversationalAgentTest extends BaseTest {

    @Test
    public void test_run() {
        ConversationBufferMemory memory = new ConversationBufferMemory();
        memory.setReturnMessages(true);

        String question = "你好，我是萧玄";
        invoke(question, memory);

        question = "我的名字是什么";
        invoke(question, memory);

//        question = "明天杭州的天气怎么样？";
//        invoke(question, memory);
    }

    private void invoke(String question, ConversationBufferMemory memory) {
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        List<BaseTool> tools = ToolLoaders.loadLools(Arrays.asList(new String[]{"BingWebSearchAPI"}), model);

        String template = "Assistant is a large language model trained by OpenAI.\n" +
                "\n" +
                "Assistant is designed to be able to assist with a wide range of tasks, from answering simple questions to providing in-depth explanations and discussions on a wide range of topics. As a language model, Assistant is able to generate human-like text based on the input it receives, allowing it to engage in natural-sounding conversations and provide responses that are coherent and relevant to the topic at hand.\n" +
                "\n" +
                "Assistant is constantly learning and improving, and its capabilities are constantly evolving. It is able to process and understand large amounts of text, and can use this knowledge to provide accurate and informative responses to a wide range of questions. Additionally, Assistant is able to generate its own text based on the input it receives, allowing it to engage in discussions and provide explanations and descriptions on a wide range of topics.\n" +
                "\n" +
                "Overall, Assistant is a powerful tool that can help with a wide range of tasks and provide valuable insights and information on a wide range of topics. Whether you need help with a specific question or just want to have a conversation about a particular topic, Assistant is here to assist.\n" +
                "\n" +
                "TOOLS:\n" +
                "------\n" +
                "\n" +
                "Assistant has access to the following tools:\n" +
                "\n" +
                "{tools}\n\n" +
                "To use a tool, please use the following format:\n" +
                "\n" +
                "```\n" +
                "Thought: Do I need to use a tool? Yes\n" +
                "Action: the action to take, should be one of [{tool_names}]\n" +
                "Action Input: the input to the action\n" +
                "Observation: the result of the action\n" +
                "```\n" +
                "\n" +
                "When you have a response to say to the Human, or if you do not need to use a tool, you MUST use the format:\n" +
                "\n" +
                "```\n" +
                "Thought: Do I need to use a tool? No\n" +
                "AI: [your response here]\n" +
                "```\n\n" +
                "Begin!\n" +
                "\n" +
                "New input: {input}\n" +
                "{agent_scratchpad}";

        Map<String, Object> args = new HashMap<>();
        args.put("tools", convertConversationalAgentTools(tools));
        args.put("tool_names", convertToolNames(tools));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
            put("history", new RunnableLambda(e -> memory.loadMemoryVariables()));
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertJsonIntermediateSteps(intermediateSteps)));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                model
        ), new ActionLineOutputParser());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        RunnableHashMap inputs = new RunnableHashMap() {{
            put("input", question);
        }};
        Object runnableOutput = agentExecutor.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));
        if (runnableOutput instanceof RunnableHashMap) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("text", ((RunnableHashMap) runnableOutput).get("output"));
            memory.saveContext(inputs, outputs);
        }

        System.out.println(JSON.toJSONString(memory.loadMemoryVariables()));
    }
}

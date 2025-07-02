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
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionProperty;
import com.alibaba.langengine.core.outputparser.QwenStructuredChatOutputParser;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.tools.*;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RunnableFunctionCallAgentTest extends BaseTest {

    private static final String SYSTEM_PROMPT = "# 角色\n" +
            "你是一个专业的旅行助手，能够依据天气状况精准地推荐游玩地点。\n" +
            "\n" +
            "## 技能\n" +
            "### 技能 1: 根据天气推荐游玩地\n" +
            "1. 当用户询问时，先确认下哪个城市。\n" +
            "2. 根据天气好坏，推荐适合的游玩地点。回复示例：\n" +
            "=====\n" +
            "   -  天气状况: <具体天气描述>\n" +
            "   -  推荐地点: <适合该天气的游玩地点>\n" +
            "   -  推荐理由: <简要说明为什么推荐该地点>\n" +
            "=====\n" +
            "\n" +
            "## 限制:\n" +
            " - 只围绕旅行相关的内容进行推荐和回答。\n" +
            " - 所输出的内容必须按照给定的格式进行组织，不能偏离框架要求。";

    @Test
    public void test_with_qwen_function_call() {
        // model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_35_TURBO);
//        model.setTemperature(0d);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        // prompt
        List<BaseMessage> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent(SYSTEM_PROMPT);
        messages.add(systemMessage);

        HumanMessage humanMessage = new HumanMessage();
//        humanMessage.setContent("今天杭州的天气如何？");
        humanMessage.setContent("今天杭州的天气如何，并以英文回复我");
//        humanMessage.setContent("帮我用谷歌翻译下 马尔代夫");
        messages.add(humanMessage);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromChatMessages(messages);

        List<FunctionDefinition> functions = new ArrayList<>();
        FunctionDefinition functionDefinition = new FunctionDefinition();
        functionDefinition.setName("get_current_weather");
        functionDefinition.setDescription("Get the current weather in a given location.");
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setRequired(Arrays.asList(new String[] { "location" }));

        Map<String, FunctionProperty> propertyMap = new HashMap<>();

        FunctionProperty functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        functionProperty.setDescription("The city and state, e.g. San Francisco, CA");
        propertyMap.put("location", functionProperty);

        functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        List<String> enums = new ArrayList<>();
        enums.add("celsius");
        enums.add("fahrenheit");
        functionProperty.setEnums(enums);
        functionProperty.setDescription("The temperature unit.");
        propertyMap.put("unit", functionProperty);

        functionParameter.setProperties(propertyMap);
        functionDefinition.setParameters(functionParameter);
        functions.add(functionDefinition);

        FunctionDefinition functionDefinition2 = new FunctionDefinition();
        functionDefinition2.setName("char_translate");
        functionDefinition2.setDescription("Translate the input into a language of your choice");
        FunctionParameter functionParameter2 = new FunctionParameter();
        functionParameter2.setRequired(Arrays.asList(new String[] { "input" }));

        Map<String, FunctionProperty> propertyMap2 = new HashMap<>();

        FunctionProperty functionProperty2 = new FunctionProperty();
        functionProperty2.setType("string");
        functionProperty2.setDescription("A topic description or goal.");
        propertyMap2.put("input", functionProperty2);

        functionProperty2 = new FunctionProperty();
        functionProperty2.setType("string");
        functionProperty2.setDescription("A language.");
        propertyMap2.put("language", functionProperty2);

        functionParameter2.setProperties(propertyMap2);
        functionDefinition2.setParameters(functionParameter2);
        functions.add(functionDefinition2);

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("functions", functions);
        }});

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertMessageIntermediateSteps(intermediateSteps)));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), new QwenStructuredChatOutputParser());

        List<BaseTool> tools = new ArrayList<>();
        tools.add(new GetCurrentWeatherTool());
        tools.add(new CharTranslateTool());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

//        String question = "你好，杭州今天天气怎么样";
        Object runnableOutput = agentExecutor.invoke(new RunnableHashMap());
//        Object runnableOutput = agentExecutor.stream(new RunnableHashMap(), chunk -> {
//            if(chunk instanceof BaseMessage) {
//                System.out.println(((BaseMessage) chunk).getContent());
//            } else {
//                System.out.println(chunk);
//            }
//        });
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

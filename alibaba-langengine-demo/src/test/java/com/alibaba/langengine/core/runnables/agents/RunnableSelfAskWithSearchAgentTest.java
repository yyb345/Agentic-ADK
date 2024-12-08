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
import com.alibaba.langengine.core.agent.selfask.SelfAskOutputParser;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.google.SerpapiTool;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RunnableSelfAskWithSearchAgentTest extends BaseTest {

    @Test
    public void test_run() {
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface modelBinding = model.bind(new RunnableHashMap() {{
            put("stop", Arrays.asList(new String[] { "Intermediate answer: " }));
        }});

        List<BaseTool> tools = new ArrayList<>();
        SerpapiTool search = new SerpapiTool();
        search.setName("Intermediate Answer");
        search.setDescription("useful for when you need to ask with search");
        tools.add(search);

        String template = "Question: Who lived longer, Muhammad Ali or Alan Turing?\n" +
                "Are follow up questions needed here: Yes.\n" +
                "Follow up: How old was Muhammad Ali when he died?\n" +
                "Intermediate answer: Muhammad Ali was 74 years old when he died.\n" +
                "Follow up: How old was Alan Turing when he died?\n" +
                "Intermediate answer: Alan Turing was 41 years old when he died.\n" +
                "So the final answer is: Muhammad Ali\n" +
                "\n" +
                "Question: When was the founder of craigslist born?\n" +
                "Are follow up questions needed here: Yes.\n" +
                "Follow up: Who was the founder of craigslist?\n" +
                "Intermediate answer: Craigslist was founded by Craig Newmark.\n" +
                "Follow up: When was Craig Newmark born?\n" +
                "Intermediate answer: Craig Newmark was born on December 6, 1952.\n" +
                "So the final answer is: December 6, 1952\n" +
                "\n" +
                "Question: Who was the maternal grandfather of George Washington?\n" +
                "Are follow up questions needed here: Yes.\n" +
                "Follow up: Who was the mother of George Washington?\n" +
                "Intermediate answer: The mother of George Washington was Mary Ball Washington.\n" +
                "Follow up: Who was the father of Mary Ball Washington?\n" +
                "Intermediate answer: The father of Mary Ball Washington was Joseph Ball.\n" +
                "So the final answer is: Joseph Ball\n" +
                "\n" +
                "Question: Are both the directors of Jaws and Casino Royale from the same country?\n" +
                "Are follow up questions needed here: Yes.\n" +
                "Follow up: Who is the director of Jaws?\n" +
                "Intermediate answer: The director of Jaws is Steven Spielberg.\n" +
                "Follow up: Where is Steven Spielberg from?\n" +
                "Intermediate answer: The United States.\n" +
                "Follow up: Who is the director of Casino Royale?\n" +
                "Intermediate answer: The director of Casino Royale is Martin Campbell.\n" +
                "Follow up: Where is Martin Campbell from?\n" +
                "Intermediate answer: New Zealand.\n" +
                "So the final answer is: No\n" +
                "\n" +
                "Question: {input}\n" +
                "Are followup questions needed here:{agent_scratchpad}";
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertJsonIntermediateSteps(intermediateSteps)));
        }});

        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), new SelfAskOutputParser());

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        String question = "阿里巴巴的创始人的家乡是哪里？";
        Object runnableOutput = agentExecutor.invoke(new RunnableHashMap() {{
            put("input", question);
        }});
        System.out.println(JSON.toJSONString(runnableOutput));
    }
}

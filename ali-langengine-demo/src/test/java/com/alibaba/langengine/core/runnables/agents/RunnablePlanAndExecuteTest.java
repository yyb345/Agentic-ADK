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
import com.alibaba.langengine.core.agent.planexecute.ListStepContainer;
import com.alibaba.langengine.core.agent.planexecute.Plan;
import com.alibaba.langengine.core.agent.planexecute.Step;
import com.alibaba.langengine.core.agent.planexecute.StepResponse;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.demo.agent.tool.LLMMathTool;
import com.alibaba.langengine.demo.agent.tool.SearchAPITool;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.outputparser.JsonAgentOutputParser;
import com.alibaba.langengine.core.outputparser.PlanningOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.prompt.impl.HumanMessagePromptTemplate;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class RunnablePlanAndExecuteTest extends BaseTest {

    @Test
    public void test_run() {
        List<BaseTool> tools = new ArrayList<>();
        LLMMathTool llmMathTool = new LLMMathTool();
        tools.add(llmMathTool);
        SearchAPITool searchAPITool = new SearchAPITool();
        tools.add(searchAPITool);

        String planTemplate = "Let's first understand the problem and devise a plan to solve the problem. Please output the plan starting with the header " +
                "'Plan:' and then followed by a numbered list of steps. Please make the plan the minimum number " +
                "of steps required to accurately complete the task. If the task is a question, " +
                "the final step should almost always be 'Given the above steps taken, please respond to " +
                "the users original question'. At the end of your plan, say '<END_OF_PLAN>'";

        List<Object> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent(planTemplate);
        messages.add(systemMessage);

        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate("{input}");
        humanMessagePromptTemplate.setPrompt(promptTemplate);
        messages.add(humanMessagePromptTemplate);
        // prompt
        ChatPromptTemplate plan_prompt = ChatPromptTemplate.fromMessages(messages);

        // model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();
        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "<END_OF_PLAN>" }));
        }});

        String executeTemplate = "Respond to the human as helpfully and accurately as possible. You have access to the following tools:\n" +
                "\n" +
                "{tools}\n" +
                "\n" +
                "Use a json blob to specify a tool by providing an action key (tool name) and an action_input key (tool input).\n" +
                "\n" +
                "Valid \"action\" values: \"Final Answer\" or Calculator, search\n" +
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
                "Begin! Reminder to ALWAYS respond with a valid json blob of a single action. Use tools if necessary. Respond directly if appropriate. Format is Action:```$JSON_BLOB```then Observation:.\n" +
                "Thought:\n\n" +
                "Previous steps: {previous_steps}\n" +
                "\n" +
                "Current objective: {current_step}\n" +
                "\n" +
                "{agent_scratchpad}";

        // outputParser
        PlanningOutputParser outputParser = new PlanningOutputParser();

        RunnableInterface planChain = Runnable.sequence(
                plan_prompt,
                modelBinding,
                outputParser
//                Runnable.assign(new HashMap<String, Object>() {{
//                    put("plan", new RunnableLambda(e -> {
//                        Plan plan = (Plan) e.get("plan");
//                        if(plan.getSteps().size() == 0) {
//                            return "";
//                        }
//                        return plan.getSteps().get(plan.getSteps().size() - 1).getValue();
//                    }));
//                }})
        );

        RunnableHashMap inputs = new RunnableHashMap() {{
            put("input", "计算3+5结果，并且将结果作为月份，计算该月份是什么星座");
        }};
        Object runnableOutput = planChain.invoke(inputs);

        if(runnableOutput instanceof RunnableHashMap) {
            RunnableHashMap platMap = (RunnableHashMap) runnableOutput;
            Plan plan = (Plan) platMap.get("plan");
            System.out.println(JSON.toJSONString(plan));

            RunnableInterface executeModelBinding =  model.bind(new HashMap<String, Object>() {{
                put("stop", Arrays.asList(new String[] { "Observation:" }));
            }});

            ListStepContainer stepContainer = new ListStepContainer();
            for (Step step : plan.getSteps()) {
                Map<String, Object> args = new HashMap<>();
                args.put("tools", convertPlanAndExecuteTools(tools));
                executeTemplate  = PromptConverter.replacePrompt(executeTemplate, args);

                ChatPromptTemplate executePrompt = ChatPromptTemplate.fromTemplate(executeTemplate);

                RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
                    put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertJsonIntermediateSteps(intermediateSteps)));
                }});

                RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                        assign,
                        executePrompt,
                        executeModelBinding
                ), new JsonAgentOutputParser());

                RunnableHashMap newInputs = new RunnableHashMap() {{
                    putAll(inputs);
                    put("previous_steps", stepContainer.getSteps().stream().map(s -> s.getStepResponse().getResponse())
                            .collect(Collectors.joining("\n")));
                    put("current_step", step.getValue());
                }};

                RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

                runnableOutput = agentExecutor.invoke(newInputs);
                System.out.println("executeChain response:" + JSON.toJSONString(runnableOutput));
                StepResponse stepResponse = new StepResponse();
                stepResponse.setResponse(((RunnableHashMap)runnableOutput).get("output").toString());
                stepContainer.addStep(step, stepResponse);
            }
        }
    }
}

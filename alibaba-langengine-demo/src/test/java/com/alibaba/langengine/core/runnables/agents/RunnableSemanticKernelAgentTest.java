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
import com.alibaba.langengine.core.agent.semantickernel.planning.ActionPlannerOutputParser;
import com.alibaba.langengine.core.agent.semantickernel.planning.BasicPlannerOutputParser;
import com.alibaba.langengine.core.agent.semantickernel.planning.SequentialPlannerOutputParser;
import com.alibaba.langengine.core.agent.semantickernel.planning.StepwisePlannerOutputParser;
import com.alibaba.langengine.core.agent.semantickernel.skill.SemanticKernelSkill;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.demo.agent.skills.MathAddSkill;
import com.alibaba.langengine.demo.agent.skills.WriterBrainstormSkill;
import com.alibaba.langengine.demo.agent.skills.WriterTranslateSkill;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RunnableSemanticKernelAgentTest extends BaseTest {

    @Test
    public void test_BasicPlanner() {
        // success
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\nObservation:" }));
        }});

        //tool
        List<BaseTool> tools = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        WriterBrainstormSkill writerBrainstormSkill = new WriterBrainstormSkill();
        tools.add(writerBrainstormSkill);
        WriterTranslateSkill writerTranslateSkill = new WriterTranslateSkill();
        tools.add(writerTranslateSkill);
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);
            }
        }

        String template = "You are a planner for the Semantic Kernel.\n" +
                "Your job is to create a properly formatted JSON plan step by step, to satisfy the goal given.\n" +
                "Create a list of subtasks based off the [GOAL] provided.\n" +
                "Each subtask must be from within the [AVAILABLE FUNCTIONS] list. Do not use any functions that are not in the list.\n" +
                "Base your decisions on which functions to use from the description and the name of the function.\n" +
                "Sometimes, a function may take arguments. Provide them if necessary.\n" +
                "The plan should be as short as possible.\n" +
                "For example:\n" +
                "\n" +
                "[AVAILABLE FUNCTIONS]\n" +
                "EmailConnector.LookupContactEmail\n" +
                "description: looks up the a contact and retrieves their email address\n" +
                "args:\n" +
                "- name: the name to look up\n" +
                "\n" +
                "WriterSkill.EmailTo\n" +
                "description: email the input text to a recipient\n" +
                "args:\n" +
                "- input: the text to email\n" +
                "- recipient: the recipient's email address. Multiple addresses may be included if separated by ';'.\n" +
                "\n" +
                "WriterSkill.Translate\n" +
                "description: translate the input to another language\n" +
                "args:\n" +
                "- input: the text to translate\n" +
                "- language: the language to translate to\n" +
                "\n" +
                "WriterSkill.Summarize\n" +
                "description: summarize input text\n" +
                "args:\n" +
                "- input: the text to summarize\n" +
                "\n" +
                "FunSkill.Joke\n" +
                "description: Generate a funny joke\n" +
                "args:\n" +
                "- input: the input to generate a joke about\n" +
                "\n" +
                "[GOAL]\n" +
                "\"Tell a joke about cars. Translate it to Spanish\"\n" +
                "\n" +
                "[OUTPUT]\n" +
                "    {\n" +
                "        \"input\": \"cars\",\n" +
                "        \"subtasks\": [\n" +
                "            {\"function\": \"FunSkill.Joke\"},\n" +
                "            {\"function\": \"WriterSkill.Translate\", \"args\": {\"language\": \"Spanish\"}}\n" +
                "        ]\n" +
                "    }\n" +
                "\n" +
                "[AVAILABLE FUNCTIONS]\n" +
                "WriterSkill.Brainstorm\n" +
                "description: Brainstorm ideas\n" +
                "args:\n" +
                "- input: the input to brainstorm about\n" +
                "\n" +
                "EdgarAllenPoeSkill.Poe\n" +
                "description: Write in the style of author Edgar Allen Poe\n" +
                "args:\n" +
                "- input: the input to write about\n" +
                "\n" +
                "WriterSkill.EmailTo\n" +
                "description: Write an email to a recipient\n" +
                "args:\n" +
                "- input: the input to write about\n" +
                "- recipient: the recipient's email address.\n" +
                "\n" +
                "WriterSkill.Translate\n" +
                "description: translate the input to another language\n" +
                "args:\n" +
                "- input: the text to translate\n" +
                "- language: the language to translate to\n" +
                "\n" +
                "[GOAL]\n" +
                "\"Tomorrow is Valentine's day. I need to come up with a few date ideas.\n" +
                "She likes Edgar Allen Poe so write using his style.\n" +
                "E-mail these ideas to my significant other. Translate it to French.\"\n" +
                "\n" +
                "[OUTPUT]\n" +
                "    {\n" +
                "        \"input\": \"Valentine's Day Date Ideas\",\n" +
                "        \"subtasks\": [\n" +
                "            {\"function\": \"WriterSkill.Brainstorm\"},\n" +
                "            {\"function\": \"EdgarAllenPoeSkill.Poe\"},\n" +
                "            {\"function\": \"WriterSkill.EmailTo\", \"args\": {\"recipient\": \"significant_other\"}},\n" +
                "            {\"function\": \"WriterSkill.Translate\", \"args\": {\"language\": \"French\"}}\n" +
                "        ]\n" +
                "    }\n" +
                "\n" +
                "[AVAILABLE FUNCTIONS]\n" +
                "{available_functions}\n" +
                "\n" +
                "[GOAL]\n" +
                "{input}\n" +
                "\n" +
                "[OUTPUT]\n";

        Map<String, Object> args = new HashMap<>();
        args.put("available_functions", convertSemanticKernelAgentBasicPlannerTools(tools));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
        }});

        //outputParser
        BasicPlannerOutputParser outputParser = new BasicPlannerOutputParser();
        outputParser.setToolMap(toolMap);
        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), outputParser);

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        RunnableHashMap inputs = new RunnableHashMap() {{
            put("input", "Tomorrow is Valentine's day. I need to come up with a few date ideas. She speaks Chinese so write it in Chinese.");
        }};
        Object runnableOutput = agentExecutor.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_SequentialPlanner() {
        // success
        //model
        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
//        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\n<!-- END -->" }));
        }});

        //tool
        List<BaseTool> tools = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        WriterBrainstormSkill writerBrainstormSkill = new WriterBrainstormSkill();
        tools.add(writerBrainstormSkill);
        WriterTranslateSkill writerTranslateSkill = new WriterTranslateSkill();
        tools.add(writerTranslateSkill);
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);
            }
        }

        String template = "Create an XML plan step by step, to satisfy the goal given, with the available functions.\n" +
                "\n" +
                "[AVAILABLE FUNCTIONS]\n" +
                "\n" +
                "{available_functions}\n" +
                "\n" +
                "[END AVAILABLE FUNCTIONS]\n" +
                "\n" +
                "To create a plan, follow these steps:\n" +
                "0. The plan should be as short as possible.\n" +
                "1. From a <goal> create a <plan> as a series of <functions>.\n" +
                "2. A plan has 'INPUT' available in context variables by default.\n" +
                "3. Before using any function in a plan, check that it is present in the [AVAILABLE FUNCTIONS] list. If it is not, do not use it.\n" +
                "4. Only use functions that are required for the given goal.\n" +
                "5. Append an \"END\" XML comment at the end of the plan after the final closing </plan> tag.\n" +
                "6. Always output valid XML that can be parsed by an XML parser.\n" +
                "7. If a plan cannot be created with the [AVAILABLE FUNCTIONS], return <plan />.\n" +
                "\n" +
                "All plans take the form of:\n" +
                "<plan>\n" +
                "    <!-- ... reason for taking step ... -->\n" +
                "    <function.{FullyQualifiedFunctionName} ... />\n" +
                "    <!-- ... reason for taking step ... -->\n" +
                "    <function.{FullyQualifiedFunctionName} ... />\n" +
                "    <!-- ... reason for taking step ... -->\n" +
                "    <function.{FullyQualifiedFunctionName} ... />\n" +
                "    (... etc ...)\n" +
                "</plan>\n" +
                "<!-- END -->\n" +
                "\n" +
                "To call a function, follow these steps:\n" +
                "1. A function has one or more named parameters and a single 'output' which are all strings. Parameter values should be xml escaped.\n" +
                "2. To save an 'output' from a <function>, to pass into a future <function>, use <function.{FullyQualifiedFunctionName} ... setContextVariable=\"<UNIQUE_VARIABLE_KEY>\"/>\n" +
                "3. To save an 'output' from a <function>, to return as part of a plan result, use <function.{FullyQualifiedFunctionName} ... appendToResult=\"RESULT__<UNIQUE_RESULT_KEY>\"/>\n" +
                "4. Use a '$' to reference a context variable in a parameter, e.g. when `INPUT='world'` the parameter 'Hello $INPUT' will evaluate to `Hello world`.\n" +
                "5. Functions do not have access to the context variables of other functions. Do not attempt to use context variables as arrays or objects. Instead, use available functions to extract specific elements or properties from context variables.\n" +
                "\n" +
                "DO NOT DO THIS, THE PARAMETER VALUE IS NOT XML ESCAPED:\n" +
                "<function.Name4 input=\"$SOME_PREVIOUS_OUTPUT\" parameter_name=\"some value with a <!-- 'comment' in it-->\"/>\n" +
                "\n" +
                "DO NOT DO THIS, THE PARAMETER VALUE IS ATTEMPTING TO USE A CONTEXT VARIABLE AS AN ARRAY/OBJECT:\n" +
                "<function.CallFunction input=\"$OTHER_OUTPUT[1]\"/>\n" +
                "\n" +
                "Here is a valid example of how to call a function \"_Function_.Name\" with a single input and save its output:\n" +
                "<function._Function__Name input=\"this is my input\" setContextVariable=\"SOME_KEY\"/>\n" +
                "\n" +
                "Here is a valid example of how to call a function \"FunctionName2\" with a single input and return its output as part of the plan result:\n" +
                "<function.FunctionName2 input=\"Hello $INPUT\" appendToResult=\"RESULT__FINAL_ANSWER\"/>\n" +
                "\n" +
                "Here is a valid example of how to call a function \"Name3\" with multiple inputs:\n" +
                "<function.Name3 input=\"$SOME_PREVIOUS_OUTPUT\" parameter_name=\"some value with a &lt;!-- &apos;comment&apos; in it--&gt;\"/>\n" +
                "\n" +
                "Begin!\n" +
                "\n" +
                "<goal>{input}</goal>\n";

        Map<String, Object> args = new HashMap<>();
        args.put("available_functions", convertSemanticKernelAgentSequentialPlannerTools(tools));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
        }});

        //outputParser
        SequentialPlannerOutputParser outputParser = new SequentialPlannerOutputParser();
        outputParser.setToolMap(toolMap);
        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), outputParser);

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        RunnableHashMap inputs = new RunnableHashMap() {{
            put("input", "Write a poem about Edson Arantes do Nasciment, then translate it into Chinese.");
        }};
        Object runnableOutput = agentExecutor.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_StepwisePlanner() {
        // success
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\n[OBSERVATION]" }));
        }});

        //tool
        List<BaseTool> tools = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        WriterBrainstormSkill writerBrainstormSkill = new WriterBrainstormSkill();
        tools.add(writerBrainstormSkill);
        WriterTranslateSkill writerTranslateSkill = new WriterTranslateSkill();
        tools.add(writerTranslateSkill);
        for (BaseTool tool : tools) {
            if(tool instanceof SemanticKernelSkill) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);
            }
        }

        String template = "[INSTRUCTION]\n" +
                "Answer the following questions as accurately as possible using the provided functions.\n" +
                "\n" +
                "[AVAILABLE FUNCTIONS]\n" +
                "The function definitions below are in the following format:\n" +
                "<functionName>: <description>\n" +
                "  inputs:\n" +
                "    - <parameterName>: <parameterDescription>\n" +
                "    - ...\n" +
                "\n" +
                "{available_functions}\n" +
                "[END AVAILABLE FUNCTIONS]\n" +
                "\n" +
                "[USAGE INSTRUCTIONS]\n" +
                "To use the functions, specify a JSON blob representing an action. The JSON blob should contain an \"action\" key with the name of the function to use, and an \"action_variables\" key with a JSON object of string values to use when calling the function.\n" +
                "Do not call functions directly; they must be invoked through an action.\n" +
                "The \"action_variables\" value should always include an \"input\" key, even if the input value is empty. Additional keys in the \"action_variables\" value should match the defined [PARAMETERS] of the named \"action\" in [AVAILABLE FUNCTIONS].\n" +
                "Dictionary values in \"action_variables\" must be strings and represent the actual values to be passed to the function.\n" +
                "Ensure that the $JSON_BLOB contains only a SINGLE action; do NOT return multiple actions.\n" +
                "IMPORTANT: Use only the available functions listed in the [AVAILABLE FUNCTIONS] section. Do not attempt to use any other functions that are not specified.\n" +
                "\n" +
                "Here is an example of a valid $JSON_BLOB:\n" +
                "{\n" +
                "  \"action\": \"functionName\",\n" +
                "  \"action_variables\": {\"parameterName\": \"some value\", ...}\n" +
                "}\n" +
                "[END USAGE INSTRUCTIONS]\n" +
                "[END INSTRUCTION]\n" +
                "\n" +
                "[THOUGHT PROCESS]\n" +
                "[QUESTION]\n" +
                "the input question I must answer\n" +
                "[THOUGHT]\n" +
                "To solve this problem, I should carefully analyze the given question and identify the necessary steps. Any facts I discover earlier in my thought process should be repeated here to keep them readily available.\n" +
                "[ACTION]\n" +
                "{\n" +
                "  \"action\": \"functionName\",\n" +
                "  \"action_variables\": {\"parameterName\": \"some value\", ...}\n" +
                "}\n" +
                "[OBSERVATION]\n" +
                "The result of the action will be provided here.\n" +
                "... (These Thought/Action/Observation can repeat until the final answer is reached.)\n" +
                "[FINAL ANSWER]\n" +
                "Once I have gathered all the necessary observations and performed any required actions, I can provide the final answer in a clear and human-readable format.\n" +
                "[END THOUGHT PROCESS]\n" +
                "\n" +
                "Let's break down the problem step by step and think about the best approach. Questions and observations should be followed by a single thought and an optional single action to take.\n" +
                "\n" +
                "Begin!\n" +
                "\n" +
                "[QUESTION]\n" +
                "{input}\n" +
                "{agent_scratchpad}";

        Map<String, Object> args = new HashMap<>();
        args.put("available_functions", convertSemanticKernelAgentStepwisePlannerTools(tools));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
            put("agent_scratchpad", new RunnableAgentLambda(intermediateSteps -> convertStepwisePlannerIntermediateSteps(intermediateSteps)));
        }});

        //outputParser
        StepwisePlannerOutputParser outputParser = new StepwisePlannerOutputParser();
        outputParser.setToolMap(toolMap);
        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), outputParser);

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        RunnableHashMap inputs = new RunnableHashMap() {{
            put("input", "Tomorrow is my girlfriend's birthday. I need to come up with some date ideas. She speaks Chinese, so she writes in Chinese.");
        }};
        Object runnableOutput = agentExecutor.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_ActionPlanner() {
        // success
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
//        model.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "#END-OF-PLAN" }));
        }});

        //tool
        List<BaseTool> tools = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        MathAddSkill mathAddSkill = new MathAddSkill();
        tools.add(mathAddSkill);
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);
            }
        }

        String template = "A planner takes a list of functions, a goal, and chooses which function to use.\n" +
                "For each function the list includes details about the input parameters.\n" +
                "[START OF EXAMPLES]\n" +
                "[EXAMPLE]\n" +
                "- List of functions:\n" +
                "// Read a file.\n" +
                "FileIOSkill.ReadAsync\n" +
                "Parameter \"path\": Source file.\n" +
                "// Write a file.\n" +
                "FileIOSkill.WriteAsync\n" +
                "Parameter \"path\": Destination file. (default value: sample.txt)\n" +
                "Parameter \"content\": File content.\n" +
                "// Get the current time.\n" +
                "TimeSkill.Time\n" +
                "No parameters.\n" +
                "// Makes a POST request to a uri.\n" +
                "HttpSkill.PostAsync\n" +
                "Parameter \"body\": The body of the request.\n" +
                "- End list of functions.\n" +
                "Goal: create a file called \"something.txt\".\n" +
                "{\"plan\":{\n" +
                "\"rationale\": \"the list contains a function that allows to create files\",\n" +
                "\"function\": \"FileIOSkill.WriteAsync\",\n" +
                "\"parameters\": {\n" +
                "\"path\": \"something.txt\",\n" +
                "\"content\": null\n" +
                "}}}\n" +
                "#END-OF-PLAN" +
                "\n" +
                "[EXAMPLE]\n" +
                "- List of functions:\n" +
                "// Get the current time.\n" +
                "TimeSkill.Time\n" +
                "No parameters.\n" +
                "// Write a file.\n" +
                "FileIOSkill.WriteAsync\n" +
                "Parameter \"path\": Destination file. (default value: sample.txt)\n" +
                "Parameter \"content\": File content.\n" +
                "// Makes a POST request to a uri.\n" +
                "HttpSkill.PostAsync\n" +
                "Parameter \"body\": The body of the request.\n" +
                "// Read a file.\n" +
                "FileIOSkill.ReadAsync\n" +
                "Parameter \"path\": Source file.\n" +
                "- End list of functions.\n" +
                "Goal: tell me a joke.\n" +
                "{\"plan\":{\n" +
                "\"rationale\": \"the list does not contain functions to tell jokes or something funny\",\n" +
                "\"function\": \"\",\n" +
                "\"parameters\": {\n" +
                "}}}\n" +
                "#END-OF-PLAN" +
                "\n" +
                "[END OF EXAMPLES]\n" +
                "[REAL SCENARIO STARTS HERE]\n" +
                "- List of functions:\n" +
                "{available_functions}\n" +
                "- End list of functions.\n" +
                "Goal: {input}\n";

        Map<String, Object> args = new HashMap<>();
        args.put("available_functions", convertSemanticKernelAgentActionPlannerTools(tools));
        template = PromptConverter.replacePrompt(template, args);
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        RunnableInterface assign = Runnable.assign(new RunnableHashMap() {{
            put("input", new RunnableLambda(e -> e.get("input").toString()));
        }});

        //outputParser
        ActionPlannerOutputParser outputParser = new ActionPlannerOutputParser();
        outputParser.setToolMap(toolMap);
        RunnableAgent agent = new RunnableAgent(Runnable.sequence(
                assign,
                prompt,
                modelBinding
        ), outputParser);

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        RunnableHashMap inputs = new RunnableHashMap() {{
            put("input", "What is the sum of 110.34 and 990?");
        }};
        Object runnableOutput = agentExecutor.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));
    }
}

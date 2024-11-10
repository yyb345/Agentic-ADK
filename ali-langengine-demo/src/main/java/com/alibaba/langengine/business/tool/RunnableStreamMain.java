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
package com.alibaba.langengine.business.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.outputparser.JsonAgentOutputParser;
import com.alibaba.langengine.core.outputparser.StrOutputParser;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.*;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.gpt.nl2sql.db.SQLDatabase;
import com.alibaba.langengine.gpt.nl2sql.db.SQLEngine;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RunnableStreamMain extends Base {

    public static void main(String[] args) {
//        test_prompt();
//        test_retriever();
        test_invoke();
//        test_two_chains();
//        test_four_chains();
//        test_branching_and_merging();
//        test_conversational_retrieval_chain();
//        test_SQLDatabase();
//        test_Agents_StructuredChatAgent();
    }

    private static void test_prompt() {
        ChatModelOpenAI model = getModel();

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        RunnableInterface chain = Runnable.sequence(prompt, model);

        Object output = chain.stream(input, chunk -> chunkHandler(chunk));
    }

    private static void test_retriever(){
        VectorStore vectorStore = initVectorStore();
        BaseRetriever retriever = vectorStore.asRetriever();

        RunnableRelevantInput runnableRelevantInput = new RunnableRelevantInput();
        runnableRelevantInput.setQuery("杭州有哪些景点？");
        runnableRelevantInput.setRecommendCount(2);
        RunnableOutput runnableOutput = retriever.stream(runnableRelevantInput, chunk -> chunkHandler(chunk));
    }

    private static void test_invoke() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        BaseOutputParser outputParser = new StrOutputParser();

        ChatModelOpenAI model = getModel();

        RunnableInterface chain = Runnable.sequence(prompt, model, outputParser);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        Object runnableOutput = chain.stream(input, chunk -> chunkHandler(chunk));
    }

    private static void test_two_chains() {
        //prompt1
        ChatPromptTemplate prompt1 = ChatPromptTemplate.fromTemplate("What is the city {person} is from?");

        //prompt2
        ChatPromptTemplate prompt2 = ChatPromptTemplate.fromTemplate("What country is the city {city} in? respond in {language}");

        //model
        ChatModelOpenAI model = getModel();

        //outputParser
        StrOutputParser outputParser = new StrOutputParser();

        RunnableInterface chain1 = Runnable.sequence(prompt1, model, outputParser);
        RunnableInterface chain2 = Runnable.sequence(new RunnableHashMap() {{
            put("city", chain1);
            put("language", "chinese");
        }}, prompt2, model, outputParser);

        //input
        RunnableHashMap input = new RunnableHashMap() {{
            put("person", "比尔盖茨");
        }};

        Object runnableOutput = chain2.stream(input, chunk -> chunkHandler(chunk));
    }

    private static void test_four_chains() {
        //model
        ChatModelOpenAI model = getModel();

        //prompt1
        ChatPromptTemplate prompt1 = ChatPromptTemplate.fromTemplate("generate a {attribute} color. Return the name of the color and nothing else:");

        //prompt2
        ChatPromptTemplate prompt2 = ChatPromptTemplate.fromTemplate("What is a fruit of color: {color}. Return the name of the fruit and nothing else:");

        //prompt3
        ChatPromptTemplate prompt3 = ChatPromptTemplate.fromTemplate("What is a country with a flag that has the color: {color}. Return the name of the country and nothing else:");

        //prompt4
        ChatPromptTemplate prompt4 = ChatPromptTemplate.fromTemplate("What is the color of {fruit} and the flag of {country}?");

        RunnableInterface model_parser = Runnable.sequence(model, new StrOutputParser());

        RunnableInterface color_generator = Runnable.sequence(
                new RunnableHashMap() {{
                    put("attribute", Runnable.passthrough());
                }},
                prompt1,
                new RunnableHashMap() {{
                    put("color", model_parser);
                }}
        );

        RunnableInterface color_to_fruit = Runnable.sequence(prompt2, model_parser);
        RunnableInterface color_to_country = Runnable.sequence(prompt3, model_parser);
        RunnableInterface question_generator = Runnable.sequence(
                color_generator,
                new RunnableHashMap() {{
                    put("fruit", color_to_fruit);
                    put("country", color_to_country);
                }},
                prompt4,
                model
        );

        Object runnableOutput = question_generator.stream("warn", chunk -> chunkHandler(chunk));
    }

    private static void test_branching_and_merging() {
        //model
        ChatModelOpenAI model = getModel();

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

        Object runnableOutput = chain.stream(new RunnableHashMap() {{
            put("input", "scrum");
        }}, chunk -> chunkHandler(chunk));
    }

    private static void test_conversational_retrieval_chain() {
        VectorStore vectorStore = initVectorStore();
        BaseRetriever retriever = vectorStore.asRetriever();
        retriever.setRecommendCount(1);

        //model
        ChatModelOpenAI model = getModel();

        ChatPromptTemplate CONDENSE_QUESTION_PROMPT = ChatPromptTemplate.fromTemplate("Given the following conversation and a follow up question, rephrase the follow up question to be a standalone question, in its original language.\n" +
                "\n" +
                "Chat History:\n" +
                "{chat_history}\n" +
                "Follow Up Input: {question}\n" +
                "Standalone question:");

        ChatPromptTemplate ANSWER_PROMPT = ChatPromptTemplate.fromTemplate("Answer the question based only on the following context:\n" +
                "{context}\n" +
                "\n" +
                "Question: {question}");

        RunnableInterface _inputs = new RunnableHashMap() {{
            put("standalone_question", Runnable.sequence(
                    Runnable.assign(new HashMap<String, Object>() {{
                        put("chat_history", new RunnableLambda(e -> MessageConverter.getBufferString((List<BaseMessage>)e.get("chat_history"))));
                    }}),
                    CONDENSE_QUESTION_PROMPT,
                    model,
                    new StrOutputParser()
            ));
        }};

        RunnableInterface _assign = Runnable.assign(new HashMap<String, Object>() {{
            put("question", "standalone_question");
        }});

        RunnableInterface _context = new RunnableHashMap() {{
            put("context", retriever);
        }};

        RunnableInterface conversational_qa_chain = Runnable.sequence(_inputs, _assign, _context, ANSWER_PROMPT, model);

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("Who wrote this notebook?");
        messages.add(humanMessage);
        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent("Harrison");
        messages.add(aiMessage);

        Object runnableOutput = conversational_qa_chain.stream(new RunnableHashMap() {{
//            put("question", "where did harrison work?");
            put("question", "where did he work?");
            put("chat_history", messages);
        }}, chunk -> chunkHandler(chunk));
    }

    private static void test_SQLDatabase() {
        String question = "How many employees are there?";

        //model
        ChatModelOpenAI model = getModel();

        SQLDatabase db = SQLDatabase.fromUri("jdbc:sqlite:/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/ali-langengine/langengine-docs/db/Chinook.db", "sqlite");

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("Based on the table schema below, write a SQL query that would answer the user's question:\n" +
                "{schema}\n" +
                "\n" +
                "Question: {question}\n" +
                "SQL Query:");

        RunnableInterface get_schema = Runnable.assign(new HashMap<String, Object>() {{
            put("schema", new RunnableLambda(e -> db.getTableInfo()));
        }});

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\nSQLResult:" }));
        }});

        RunnableInterface sql_response = Runnable.sequence(
                get_schema,
                prompt,
                modelBinding,
                new StrOutputParser()
        );

        Object runnableOutput = sql_response.invoke(new RunnableHashMap() {{
            put("question", question);
        }});

        String sqlResponse = "";
        System.out.println(JSON.toJSONString(runnableOutput));
        if(runnableOutput instanceof RunnableStringVar) {
            String sqlQuery = ((RunnableStringVar) runnableOutput).getValue();
            ResultSet resultSet = db.executeQuery(sqlQuery);
            List<List<String>> sqlCmdResult = null;
            try {
                sqlCmdResult = SQLEngine.resultSetToTable(resultSet);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            sqlResponse = JSON.toJSONString(sqlCmdResult);
        }

        ChatPromptTemplate prompt_response = ChatPromptTemplate.fromTemplate("Based on the table schema below, question, sql query, and sql response, write a natural language response:\n" +
                "{schema}\n" +
                "\n" +
                "Question: {question}\n" +
                "SQL Query: {query}\n" +
                "SQL Response: {response}");

        RunnableInterface get_schema_with_response = Runnable.assign(new HashMap<String, Object>() {{
            put("schema", new RunnableLambda(e -> db.getTableInfo()));
        }});

        RunnableInterface full_chain = Runnable.sequence(
                new RunnableHashMap() {{
                    put("query", sql_response);
                }},
                get_schema_with_response,
                prompt_response,
                model
        );

        String finalSqlResponse = sqlResponse;
        runnableOutput = full_chain.stream(new RunnableHashMap() {{
            put("question", question);
            put("response", finalSqlResponse);
        }}, chunk -> chunkHandler(chunk));
    }

    private static void test_Agents_StructuredChatAgent() {
        //model
        ChatModelOpenAI model = getModel();

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
        args.put("tools", convertStructuredChatAgentTools(tools));
        args.put("tool_names", convertToolNames(tools));
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

        RunnableAgentExecutor agentExecutor = new RunnableAgentExecutor(agent, tools);

        String question = "刚才我在问谁，然后他负责什么？我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？两个问题请一起回答";
        Object runnableOutput = agentExecutor.stream(new RunnableHashMap() {{
            put("input", question);
        }}, chunk -> chunkHandler(chunk));
    }
}

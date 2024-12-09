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

package com.alibaba.langengine.core.runnables;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionProperty;
import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.outputparser.StrOutputParser;
import com.alibaba.langengine.core.prompt.impl.*;
import com.alibaba.langengine.core.runnables.tools.*;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.gpt.nl2sql.db.SQLDatabase;
import com.alibaba.langengine.gpt.nl2sql.db.SQLEngine;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.openai.model.OpenAIModerationChain;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CookbookTest extends BaseTest {

    //PromptTemplate + LLM
    @Test
    public void test_prompttemplate_llm() {
        // success
        String question;
//        question = "tell me a joke about {foo}";
        question = "请说一个关于 {foo} 的笑话";

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(question);

//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface chain = Runnable.sequence(prompt, model);

        RunnableHashMap input = new RunnableHashMap() {{
            put("foo", "bears");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_attaching_stop_sequences() {
        // success
        String question;
//        question = "tell me a joke about {foo}";
        question = "请说一个关于 {foo} 的笑话";

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(question);

//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\n" }));
        }});

        RunnableInterface chain = Runnable.sequence(prompt, modelBinding);

        RunnableHashMap input = new RunnableHashMap() {{
            put("foo", "bears");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_attaching_function_call_information() {
        // success
        String question;
//        question = "tell me a joke about {foo}";
        question = "请说一个关于 {foo} 的笑话";

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(question);

//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        List<FunctionDefinition> functionDefinitionList = new ArrayList<>();
        FunctionDefinition functionDefinition = new FunctionDefinition();
        functionDefinitionList.add(functionDefinition);

        functionDefinition.setName("joke");
        functionDefinition.setDescription("A joke");
        FunctionParameter functionParameter = new FunctionParameter();
        functionDefinition.setParameters(functionParameter);
        functionParameter.setType("object");
        functionParameter.setProperties(new HashMap<>());
        FunctionProperty functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        functionProperty.setDescription("The setup for the joke");
        functionParameter.getProperties().put("setup", functionProperty);
        functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        functionProperty.setDescription("The punchline for the joke");
        functionParameter.getProperties().put("punchline", functionProperty);
        functionParameter.setRequired(Arrays.asList(new String[] { "setup", "punchline" }));

        RunnableInterface modelBinding =  model.bind(new HashMap<String, Object>() {{
            put("functions", functionDefinitionList);
//            put("function_call", new HashMap<String, Object>() {{ put("name", "joke"); }});
        }});

        RunnableInterface chain = Runnable.sequence(prompt, modelBinding);

        RunnableHashMap input = new RunnableHashMap() {{
            put("foo", "bears");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_two_chains() {
        //prompt1
        ChatPromptTemplate prompt1 = ChatPromptTemplate.fromTemplate("What is the city {person} is from?");

        //prompt2
        ChatPromptTemplate prompt2 = ChatPromptTemplate.fromTemplate("What country is the city {city} in? respond in {language}");

        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

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

        Object runnableOutput = chain2.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_four_chains() {
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

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

        Object runnableOutput = question_generator.invoke("warn");
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    /**
     *      Input
     *       / \
     *      /   \
     *  Branch1 Branch2
     *      \   /
     *       \ /
     *     Combine
     */
    @Test
    public void test_branching_and_merging() {
        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

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
    public void test_conversational_retrieval_chain() {
        VectorStore vectorStore = initVectorStore();
        BaseRetriever retriever = vectorStore.asRetriever();
        retriever.setRecommendCount(1);

        //model
//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

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

        Object runnableOutput = conversational_qa_chain.invoke(new RunnableHashMap() {{
//            put("question", "where did harrison work?");
            put("question", "where did he work?");
            put("chat_history", messages);
        }});
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_SQLDatabase() throws SQLException {
        String question = "How many employees are there?";

        //model
        ChatModelOpenAI model = new ChatModelOpenAI();

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
            List<List<String>> sqlCmdResult = SQLEngine.resultSetToTable(resultSet);
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
        runnableOutput = full_chain.invoke(new RunnableHashMap() {{
            put("question", question);
            put("response", finalSqlResponse);
        }});
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_Memory() {
        ConversationBufferMemory memory = new ConversationBufferMemory();
        memory.setReturnMessages(true);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        BaseOutputParser outputParser = new StrOutputParser();

//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableHashMap input = new RunnableHashMap() {{
            put("history", new RunnableLambda(e -> memory.loadMemoryVariables()));
        }};

        RunnableInterface chain = Runnable.sequence(input, prompt, model, outputParser);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("topic", "ice cream");
        Object runnableOutput = chain.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));

        //第一次save memory
        if(runnableOutput instanceof RunnableStringVar) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("text", ((RunnableStringVar) runnableOutput).getValue());
            memory.saveContext(inputs, outputs);
        } else if(runnableOutput instanceof BaseMessage) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("text", ((BaseMessage) runnableOutput).getContent());
            memory.saveContext(inputs, outputs);
        }

        inputs.put("topic", "apple");
        runnableOutput = chain.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));

        //第二次save memory
        if(runnableOutput instanceof RunnableStringVar) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("text", ((RunnableStringVar) runnableOutput).getValue());
            memory.saveContext(inputs, outputs);
        } else if(runnableOutput instanceof BaseMessage) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("text", ((BaseMessage) runnableOutput).getContent());
            memory.saveContext(inputs, outputs);
        }

        System.out.println(JSON.toJSONString(memory.loadMemoryVariables()));
    }

    @Test
    public void test_RunnableBranch() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("Given the user question below, classify it as either being about `LangChain`, `OpenAI`, or `Other`.\n" +
                "\n" +
                "Do not respond with more than one word.\n" +
                "\n" +
                "<question>\n" +
                "{question}\n" +
                "</question>\n" +
                "\n" +
                "Classification:");

        BaseOutputParser outputParser = new StrOutputParser();

//        ChatModelOpenAI model = new ChatModelOpenAI();
        DashScopeOpenAIChatModel model = new DashScopeOpenAIChatModel();

        RunnableInterface chain = Runnable.sequence(prompt, model, outputParser);

        RunnableInterface langchain_chain = Runnable.sequence(
                ChatPromptTemplate.fromTemplate("You are an expert in langchain. \n" +
                        "Always answer questions starting with \"As Harrison Chase told me\". \n" +
                        "Respond to the following question:\n" +
                        "\n" +
                        "Question: {question}\n" +
                        "Answer:"),
                model
        );

        RunnableInterface openai_chain = Runnable.sequence(
                ChatPromptTemplate.fromTemplate("You are an expert in openai. \n" +
                        "Always answer questions starting with \"As Dario Amodei told me\". \n" +
                        "Respond to the following question:\n" +
                        "\n" +
                        "Question: {question}\n" +
                        "Answer:"),
                model
        );

        RunnableInterface general_chain = Runnable.sequence(
                ChatPromptTemplate.fromTemplate("Respond to the following question:\n" +
                        "\n" +
                        "Question: {question}\n" +
                        "Answer:"),
                model
        );

        Pair<RunnableLambda, RunnableInterface> openai_pair = Pair.of(
                new RunnableLambda(e ->  "openai".equalsIgnoreCase(e.get("topic").toString())),
                openai_chain
        );

        Pair<RunnableLambda, RunnableInterface> langchain_pair = Pair.of(
                new RunnableLambda(e ->  "langchain".equalsIgnoreCase(e.get("topic").toString())),
                langchain_chain
        );

        RunnableInterface branch = new RunnableBranch(
                general_chain,
                openai_pair,
                langchain_pair
        );

        RunnableInterface full_chain = Runnable.sequence(
                new RunnableHashMap() {{
                    put("topic", chain);
                }},
                Runnable.assign(new RunnableHashMap() {{
                    put("question", new RunnableLambda(e -> e.get("question").toString()));
                }}),
                branch
        );

        RunnableHashMap input = new RunnableHashMap() {{
            put("question", "how do I call OpenAI?");
//            put("question", "how do I use LangChain?");
//            put("question", "whats 2 + 2");
        }};
        Object runnableOutput = full_chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_RunnableWithFallbacks() {
        List<Object> messages = new ArrayList<>();
        SystemMessagePromptTemplate systemMessagePromptTemplate = new SystemMessagePromptTemplate();
        systemMessagePromptTemplate.setPrompt(new PromptTemplate("You're a nice assistant who always includes a compliment in your response"));
        messages.add(systemMessagePromptTemplate);
        HumanMessagePromptTemplate humanMessagePromptTemplate = new HumanMessagePromptTemplate();
        humanMessagePromptTemplate.setPrompt(new PromptTemplate("Why did the {animal} cross the road"));
        messages.add(humanMessagePromptTemplate);

        ChatPromptTemplate chatPrompt = ChatPromptTemplate.fromMessages(messages);

        BaseOutputParser outputParser = new StrOutputParser();

        //badModel
        ChatModelOpenAI badModel = new ChatModelOpenAI();
        badModel.setModel("gpt-fake");

        //badChain
        RunnableInterface badChain = Runnable.sequence(chatPrompt, badModel, outputParser);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("Instructions: You should always include a compliment in your response.\n" +
                "\n" +
                "Question: Why did the {animal} cross the road?");

        //goodModel
        ChatModelOpenAI goodModel = new ChatModelOpenAI();

        //goodChain
        RunnableInterface goodChain = Runnable.sequence(prompt, goodModel);

        //goodChain is a fallback with badChain.
        RunnableInterface chain = badChain.withFallbacks(goodChain);

        RunnableHashMap input = new RunnableHashMap() {{
            put("animal", "turtle");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_adding_moderation() {
        OpenAIModerationChain moderate = new OpenAIModerationChain();

        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setModel(OpenAIModelConstants.GPT_4);

        List<Object> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("repeat after me: {input}");
        messages.add(systemMessage);

        ChatPromptTemplate prompt = ChatPromptTemplate.fromMessages(messages);

        RunnableInterface moderated_chain = Runnable.sequence(prompt, model, moderate);

        RunnableHashMap input = new RunnableHashMap() {{
            put("input", "you are stupid");
        }};

        Object runnableOutput = moderated_chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_using_tools() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("turn the following user input into a search query for a search engine:\n" +
                "\n" +
                "{input}");

        //model
        ChatModelOpenAI model = new ChatModelOpenAI();

        //outputParser
        StrOutputParser outputParser = new StrOutputParser();

        //tool
        DuckDuckGoSearchTool tool = new DuckDuckGoSearchTool();

        RunnableInterface chain = Runnable.sequence(prompt, model, outputParser, tool);

        //input
        RunnableHashMap input = new RunnableHashMap() {{
            put("input", "I'd like to figure out what games are tonight");
        }};

        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_RunnableEach() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("Tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();

        StrOutputParser outputParser = new StrOutputParser();

        RunnableInterface runnable = Runnable.sequence(prompt, model, outputParser);
        RunnableEach runnableEach = new RunnableEach();
        runnableEach.setBound(runnable);

        List<RunnableHashMap> inputs = new ArrayList<>();
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "Computer Science");
        }};
        inputs.add(input);
        input = new RunnableHashMap() {{
            put("topic", "Art");
        }};
        inputs.add(input);
        input = new RunnableHashMap() {{
            put("topic", "Biology");
        }};
        inputs.add(input);

        Object runnableOutput = runnableEach.invoke(inputs);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_RunnableRetry() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a joke about {foo}");

        //badModel
        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setModel("gpt-fake");

        RunnableInterface modelBinding =  model.withRetry(2, new HashMap<String, Object>() {{
            put("stop", Arrays.asList(new String[] { "\n" }));
        }});

        RunnableInterface chain = Runnable.sequence(prompt, modelBinding);

        RunnableHashMap input = new RunnableHashMap() {{
            put("foo", "bears");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }
}

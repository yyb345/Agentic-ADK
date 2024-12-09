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
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;
import com.alibaba.langengine.core.outputparser.StrOutputParser;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.prompt.StringPromptValue;
import com.alibaba.langengine.core.prompt.impl.ChatPromptTemplate;
import com.alibaba.langengine.core.runnables.tools.TmpCommonTool;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.dashscope.model.DashScopeLLM;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunnableTest {

    @Test
    public void test_prompt() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        RunnableOutput runnableOutput = prompt.invoke(input);
        System.out.println(runnableOutput);
    }

    @Test
    public void test_model() {
        FakeAI llm = new FakeAI();
        StringPromptValue promptValue = new StringPromptValue();
        promptValue.setText("hi");
        RunnableOutput runnableOutput = llm.invoke(promptValue);
        System.out.println(runnableOutput);
    }

    @Test
    public void test_outputParser() {
        BaseOutputParser<String> fakeOutputParser = new BaseOutputParser<String>() {
            @Override
            public String parse(String text) {
                return "FakeOutputParser mockResponse:" + text;
            }
        };

        RunnableStringVar stringVar = new RunnableStringVar();
        stringVar.setValue("goodbye");
        String runnableOutput = fakeOutputParser.invoke(stringVar);
        System.out.println(runnableOutput);
    }

    @Test
    public void test_retriever(){
        VectorStore vectorStore = initVectorStore();
        BaseRetriever retriever = vectorStore.asRetriever();

        RunnableRelevantInput runnableRelevantInput = new RunnableRelevantInput();
        runnableRelevantInput.setQuery("杭州有哪些景点？");
        runnableRelevantInput.setRecommendCount(2);
        RunnableOutput runnableOutput = retriever.invoke(runnableRelevantInput);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    private VectorStore initVectorStore() {
        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(new OpenAIEmbeddings());

        List<String> texts = Arrays.asList(new String[] {
                "杭州西湖，位于中国浙江省杭州市，是世界文化遗产、国家5A级旅游景区。这个风景如画的湖泊以其“西湖十景”闻名，包括“苏堤春晓”、“断桥残雪”等，围绕湖泊有古典园林、历史寺庙与亭台楼阁，文化底蕴丰富。四季更迭，西湖总以不同的美景吸引着世界各地的游客。",
                "北京故宫，位于中国首都北京市中心，是明朝到清朝（1420年至1912年）的皇宫，现称为故宫博物院。作为世界上最大的古代木结构建筑群，它拥有超过九千个房间，展示了中国悠久的历史和独特的文化艺术。故宫不仅是中国的象征，也是世界文化遗产之一，以其宏伟的建筑规模、精美的建筑艺术和丰富的历史藏品著称于世。每年吸引着数以百万计的游客前来参观。",
                "上海外滩，是上海的标志性景观之一，位于黄浦江畔，与浦东新区的陆家嘴金融区隔江相望。它以极具特色的欧式建筑群著称，这些建筑曾是19世纪末至20世纪初外国银行和贸易公司的所在地，现如今成为了餐厅、公司办公室和高档酒店。外滩不仅是上海的经济象征，也是游客欣赏黄浦江美景和感受上海历史风貌的热门去处。",
                "杭州西溪湿地是中国东部著名的城市湿地公园，以其丰富的自然景观和生态资源闻名，是集生态保护、旅游观光、休闲娱乐于一体的国家级湿地公园，亦是西湖风景名胜区的重要组成部分。",
                "杭州灵隐寺，位于浙江省杭州市西湖区飞来峰下，是江南著名的古刹之一，也是全国重点文物保护单位。该寺始建于东晋时期，历经多次重建，拥有多座古建筑和佛教文物。灵隐寺以其幽静的山林环境、丰富的历史文化和深厚的佛教氛围吸引了无数信众和游客前来朝拜和游览。",
        });
        List<Document> documents =  texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        vectorStore.addDocuments(documents);
        return vectorStore;
    }

    @Test
    public void test_prompt_or_model_outputParser() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        RunnableOutput runnableOutput = prompt.invoke(input);

        FakeAI llm = new FakeAI();
        runnableOutput = llm.invoke((PromptValue)runnableOutput);

        BaseOutputParser<String> fakeOutputParser = new BaseOutputParser<String>() {

            @Override
            public String parse(String text) {
                return "FakeOutputParser mockResponse:" + text;
            }
        };

        if(runnableOutput instanceof RunnableStringVar) {
            RunnableStringVar stringVar = (RunnableStringVar) runnableOutput;
            String result = fakeOutputParser.invoke(stringVar);
            System.out.println(result);
        }
    }

    @Test
    public void test_retriever_prompt_or_model_outputParser() {
        String question = "杭州有哪些景点？";
        VectorStore vectorStore = initVectorStore();
        BaseRetriever retriever = vectorStore.asRetriever();

        RunnableRelevantInput runnableRelevantInput = new RunnableRelevantInput();
        runnableRelevantInput.setQuery(question);
        runnableRelevantInput.setRecommendCount(2);
        RunnableOutput runnableOutput = retriever.invoke(runnableRelevantInput);
        String context = "";
        if(runnableOutput instanceof RunnableRelevantOutput) {
            RunnableRelevantOutput relevantOutput = (RunnableRelevantOutput) runnableOutput;
            context = relevantOutput.getDocuments().stream().map(Document::getPageContent).collect(Collectors.joining("\n"));
        }

        String template = "Answer the question based only on the following context:\n" +
                "{context}\n" +
                "\n" +
                "Question: {question}";
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);
        String finalContext = context;
        RunnableHashMap input = new RunnableHashMap() {{
            put("question", question);
            put("context", finalContext);
        }};

        runnableOutput = prompt.invoke(input);

        ChatModelOpenAI llm = new ChatModelOpenAI();
        runnableOutput = llm.invoke((PromptValue)runnableOutput);

        StrOutputParser outputParser = new StrOutputParser();

        if(runnableOutput instanceof RunnableStringVar) {
            RunnableStringVar stringVar = (RunnableStringVar) runnableOutput;
            String result = outputParser.invoke(stringVar);
            System.out.println(result);
        }
    }

    @Test
    public void test_inputFormatter(){
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        BaseOutputParser outputParser = new StrOutputParser();

        FakeAI model = new FakeAI();

        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", Runnable.passthrough());
        }};

        RunnableInterface chain = Runnable.sequence(input,
            Runnable.inputFormatter().add("topic", (Object topic) -> {
                return topic.toString()+"topic1";
            }), prompt
            ,model,
            outputParser);

        Object runnableOutput = chain.invoke("ice cream");
        System.out.println(runnableOutput);
    }

    @Test
    public void test_invoke() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        BaseOutputParser outputParser = new StrOutputParser();

        ChatModelOpenAI model = new ChatModelOpenAI();

        RunnableInterface chain = Runnable.sequence(prompt, model, outputParser);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        Object runnableOutput = chain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_stream() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        ChatModelOpenAI model = new ChatModelOpenAI();

        RunnableInterface chain = Runnable.sequence(prompt, model);
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};
        chain.stream(input, chunk -> System.out.println(((BaseMessage) chunk).getContent()));
    }

    @Test
    public void test_invoke_2() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a short joke about {topic}");

        BaseOutputParser outputParser = new StrOutputParser();

        ChatModelOpenAI model = new ChatModelOpenAI();

        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", Runnable.passthrough());
        }};

        RunnableInterface chain = Runnable.sequence(input, prompt, model, outputParser);

        Object runnableOutput = chain.invoke("ice cream");
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_invoke_3() {
        VectorStore vectorStore = initVectorStore();

        //retriever
        BaseRetriever retriever = vectorStore.asRetriever();

        //input
        String question = "杭州有哪些景点？";
        RunnableHashMap input = new RunnableHashMap() {{
            put("question", Runnable.passthrough());
            put("context", retriever);
        }};

        //prompt
        String template = "Answer the question based only on the following context:\n" +
                "{context}\n" +
                "\n" +
                "Question: {question}";
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate(template);

        //model
        ChatModelOpenAI model = new ChatModelOpenAI();

        //outputParser
        StrOutputParser outputParser = new StrOutputParser();

        //sequence
        RunnableInterface chain = Runnable.sequence(input, prompt, model, outputParser);
        Object runnableOutput = chain.invoke(question);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_invoke_4() {
        //prompt1
        ChatPromptTemplate prompt1 = ChatPromptTemplate.fromTemplate("tell me a joke about {topic}");

        //prompt2
        ChatPromptTemplate prompt2 = ChatPromptTemplate.fromTemplate("write a 2-line poem about {topic}");

        //model
        ChatModelOpenAI model = new ChatModelOpenAI();

        //sequenceChain1,2
        RunnableInterface jokeChain = Runnable.sequence("joke", prompt1, model);
        RunnableInterface poemChain = Runnable.sequence("poem", prompt2, model);

        //parallelChain
        RunnableInterface mapChain = Runnable.parallel(jokeChain, poemChain);

        //input
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
        }};

        Object runnableOutput = mapChain.invoke(input);
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_invoke_5() {
        VectorStore vectorStore = initVectorStore();

        //retriever
        BaseRetriever retriever = vectorStore.asRetriever();

        //prompt1
        ChatPromptTemplate prompt1 = ChatPromptTemplate.fromTemplate("tell me a joke about {topic}");

        //prompt2
        ChatPromptTemplate prompt2 = ChatPromptTemplate.fromTemplate("write a 2-line poem about {topic}");

        //prompt3
        ChatPromptTemplate prompt3 = ChatPromptTemplate.fromTemplate("Answer the question based only on the following context:\n" +
                "{context}\n" +
                "\n" +
                "Question: {question}");

        //model
        ChatModelOpenAI model = new ChatModelOpenAI();

        //input
        RunnableHashMap question = new RunnableHashMap() {{
//            put("question", Runnable.passthrough());
            put("context", retriever);
        }};

        //sequenceChain1,2,3
        RunnableInterface jokeChain = Runnable.sequence("joke", prompt1, model);
        RunnableInterface poemChain = Runnable.sequence("poem", prompt2, model);
        RunnableInterface retrieverChain = Runnable.sequence("retriever", question, prompt3, model);


        //parallelChain
        RunnableInterface mapChain = Runnable.parallel(jokeChain, poemChain, retrieverChain);

        //input
        RunnableHashMap input = new RunnableHashMap() {{
            put("topic", "ice cream");
            put("question", "杭州有哪些景点？");
        }};

        long start = System.currentTimeMillis();
        Object runnableOutput = mapChain.invoke(input);
        System.out.println((System.currentTimeMillis() - start) + "ms");
        System.out.println(JSON.toJSONString(runnableOutput));
    }

    @Test
    public void test_getSchema() {
        ChatPromptTemplate prompt = ChatPromptTemplate.fromTemplate("tell me a joke about {topic}");
        System.out.println(prompt.getInputSchema());
    }

    @Test
    public void test_tool() {
        Map<String, Object> schemaMap =new HashMap<>();
        schemaMap.put("city", "省/市/区县名称，可以识别多个城市");
        TmpCommonTool realTimeCongestIdxTool = new TmpCommonTool("realTimeCongestIdxTool","用于查询省、市、区县的拥堵指数或者速度,拥堵指数越大，代表该城市越拥堵",schemaMap);

        RunnableInterface apiParams = Runnable.assign(new HashMap<String, Object>() {{
            put("city", new RunnableLambda(e->e.get("inputValue")));
        }});

        DashScopeLLM model = new DashScopeLLM();
        model.setModel("qwen-14b-chat");


        RunnableInterface finalChain = Runnable.sequence(apiParams,realTimeCongestIdxTool);

        Object runnableOutput = finalChain.invoke(new RunnableHashMap() {{
            put("inputValue","北京");
        }});

        System.out.println(JSONObject.toJSON(runnableOutput));
    }
}

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
package com.alibaba.langengine.core.chain.router;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * 概念介绍：一种创建动态选择给定输入使用的下一个链，和前面LLMRouterChain类似，只是它是通过计算相似度去做基础路由，根据路由匹配到对应的Prompt模版，再进行LLM调用。
 * 适用场景：通过问答先经过一轮意图识别（Embedding相似度计算），识别出不同的业务场景，匹配对应的业务Prompt，再进行下一轮的大模型推理。
 *
 * @author xiaoxuan.lp
 */
public class EmbeddingRouterChainTest {

    private static final List<String> ROUTING_KEYS = Arrays.asList(new String[]{ "input" });

    @Test
    public void test_openai_run() {
        Embeddings embedding = new OpenAIEmbeddings();
        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(embedding);

        String physicsTemplate = "You are a very smart physics professor. " +
                "You are great at answering questions about physics in a concise and easy to understand manner. " +
                "When you don't know the answer to a question you admit that you don't know.\n" +
                "\n" +
                "Here is a question:\n" +
                "{input}";
        String mathTemplate = "You are a very good mathematician. You are great at answering math questions. " +
                "You are so good because you are able to break down hard problems into their component parts, " +
                "answer the component parts, and then put them together to answer the broader question.\n" +
                "\n" +
                "Here is a question:\n" +
                "{input}";
        String chineseTemplate = "You are a very good Chinese teacher. You are very good at answering Chinese questions.\n" +
                "\n" +
                "Here is a question:\n" +
                "{input}";
        List<Map<String, Object>> promptInfos = new ArrayList<>();
        Map<String, Object> pInfo;
        pInfo = new HashMap<>();
        pInfo.put("name", "physics");
        pInfo.put("description", "Good for answering questions about physics");
        pInfo.put("prompt_template", physicsTemplate);
        promptInfos.add(pInfo);
        pInfo = new HashMap<>();
        pInfo.put("name", "math");
        pInfo.put("description", "Good for answering questions about math");
        pInfo.put("prompt_template", mathTemplate);
        promptInfos.add(pInfo);
        pInfo = new HashMap<>();
        pInfo.put("name", "chinese");
        pInfo.put("description", "Good for answering questions about chinese");
        pInfo.put("prompt_template", chineseTemplate);
        promptInfos.add(pInfo);

        ChatModelOpenAI llm = new ChatModelOpenAI();

        Map<String, List<String>> namesAndDescriptions = new HashMap<>();
        namesAndDescriptions.put("physics", Arrays.asList(new String[]{ "for questions about physics" }));
        namesAndDescriptions.put("math", Arrays.asList(new String[]{ "for questions about math" }));
        namesAndDescriptions.put("chinese", Arrays.asList(new String[]{ "for questions about chinese" }));

        EmbeddingRouterChain routerChain = EmbeddingRouterChain.fromNamesAndDescriptions(namesAndDescriptions, vectorStore, ROUTING_KEYS);

        MultiPromptChain multiPromptChain = MultiPromptChain.fromPrompts(llm, promptInfos, routerChain);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("input", "介绍一下什么是惯性定律");
//        inputs.put("input", "3的2次方是多少");
        inputs.put("input", "\"Nice to meet you.\"的中文是怎么说？");

        Map<String, Object> response = multiPromptChain.run(inputs);
        System.out.println("response:" + JSON.toJSONString(response));
    }
}

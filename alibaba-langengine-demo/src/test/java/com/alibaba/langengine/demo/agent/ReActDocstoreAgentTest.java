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
package com.alibaba.langengine.demo.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.reactdoc.DocstoreExplorer;
import com.alibaba.langengine.core.docstore.InMemoryDocstore;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.demo.agent.tool.MySerpapiTool;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.tool.ToolLoaders;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用 ReAct 框架和 docstore 交互, 使用Search 和Lookup 工具, 前者用来搜, 后者寻找term, 举例: Wipipedia 工具
 *
 * @author xiaoxuan.lp
 */
public class ReActDocstoreAgentTest {

    @Test
    public void test() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        InMemoryDocstore docstore = new InMemoryDocstore();
        Document document = new Document();
        document.setPageContent("David Chanoff is a noted author of non-fiction work. His work has typically involved collaborations with the principal protagonist of the work concerned. His collaborators have included; Augustus A. White, Joycelyn Elders, Đoàn Văn Toại, William J. Crowe, Ariel Sharon, Kenneth Good and Felix Zandman. He has also written about a wide range of subjects including literary history, education and foreign for The Washington Post, The New Republic and The New York Times Magazine. He has published more than twelve books.");
        docstore.getDocInfo().put("David Chanoff", document);
        document = new Document();
        document.setPageContent("U.S. Navy admiral. (January 2, 1925 – October 18, 2007) was a United States Navy admiral and diplomat who served as the 11th chairman of the Joint Chiefs of Staff under Presidents Ronald Reagan and George H. W. Bush, and as the ambassador to the United Kingdom and Chair of the Intelligence Oversight Board under President Bill Clinton.");
        docstore.getDocInfo().put("U.S. Navy admiral", document);
        DocstoreExplorer docstoreExplorer = new DocstoreExplorer(docstore);

        List<BaseTool> tools = new ArrayList<>();

        DefaultTool tool;
        tool = new DefaultTool();
        tool.setName("Search");
        tool.setDescription("useful for when you need to ask with search");
        tool.setBasicFunc(docstoreExplorer::search);
        tools.add(tool);

        tool = new DefaultTool();
        tool.setName("Lookup");
        tool.setDescription("useful for when you need to ask with lookup");
        tool.setBasicFunc(docstoreExplorer::lookup);
        tools.add(tool);

        AgentExecutor agentExecutor = ToolLoaders.initializeReActDocstoreAgent(tools, llm);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Author David Chanoff has collaborated with a U.S. Navy admiral who served as the ambassador to the United Kingdom under which President?");

        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));
    }

    @Test
    public void test_openai_run() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        List<BaseTool> tools = new ArrayList<>();

        MySerpapiTool tool = new MySerpapiTool();
        tool.setName("Search");
        tool.setDescription("useful for when you need to ask with search");
        tools.add(tool);

        MySerpapiTool lookupTool = new MySerpapiTool();
        lookupTool.setName("Lookup");
        lookupTool.setDescription("useful for when you need to ask with lookup");
        tools.add(lookupTool);

        AgentExecutor agentExecutor = ToolLoaders.initializeReActDocstoreAgent(tools, llm, false);

        Map<String, Object> response;
        Map<String, Object> inputs = new HashMap<>();

        inputs.put("input", "微软创始人是来自哪里？");
        response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));
    }

    @Test
    public void testCH() {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        InMemoryDocstore docstore = new InMemoryDocstore();
        Document document = new Document();
        document.setPageContent("大卫·查诺夫 (David Chanoff) 是一位著名的非小说类作家。 他的作品通常涉及与相关作品的主要主角的合作。 他的合作者包括： Augustus A. White、Joycelyn Elders、Đoàn Văn Toại、William J. Crowe、Ariel Sharon、Kenneth Good 和 Felix Zandman。 他还为《华盛顿邮报》、《新共和》和《纽约时报杂志》撰写过有关文学史、教育和外国等广泛主题的文章。 他出版了十二本书以上。");
        docstore.getDocInfo().put("大卫·查诺夫", document);
        document = new Document();
        document.setPageContent("美国海军上将。 （1925年1月2日－2007年10月18日）美国海军上将和外交官，曾担任罗纳德·里根总统和乔治·H·W·布什总统领导下的第十一任参谋长联席会议主席，并担任驻英国大使和主席 比尔·克林顿总统领导下的情报监督委员会。");
        docstore.getDocInfo().put("美国海军上将", document);
        DocstoreExplorer docstoreExplorer = new DocstoreExplorer(docstore);

        List<BaseTool> tools = new ArrayList<>();

        DefaultTool tool;
        tool = new DefaultTool();
        tool.setName("搜索");
        tool.setDescription("当您需要通过搜索询问时很有用");
        tool.setBasicFunc(docstoreExplorer::search);
        tools.add(tool);

        tool = new DefaultTool();
        tool.setName("查找");
        tool.setDescription("当您需要通过查找询问时很有用");
        tool.setBasicFunc(docstoreExplorer::lookup);
        tools.add(tool);

        AgentExecutor agentExecutor = ToolLoaders.initializeReActDocstoreAgent(tools, llm, true);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "作者大卫·查诺夫曾与一位美国海军上将合作，他曾在哪任总统的领导下担任驻英国大使？");

        Map<String, Object> response = agentExecutor.run(inputs);
        System.out.println(JSON.toJSONString(response.get("output")));
    }
}

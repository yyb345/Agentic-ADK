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
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Base {

    public static ChatModelOpenAI getModel() {
        ChatModelOpenAI model = new ChatModelOpenAI();
        model.setModel(OpenAIModelConstants.GPT_4);
        model.setStream(true);
        return model;
    }

    public static void chunkHandler(Object chunk) {
        if(chunk instanceof BaseMessage) {
            System.out.println(((BaseMessage) chunk).getContent());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(JSON.toJSONString(chunk));
        }
    }

    public static VectorStore initVectorStore() {
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

    public static String convertJsonIntermediateSteps(List<AgentAction> intermediateSteps) {
        String thoughts = "";
        for (AgentAction action : intermediateSteps) {
            thoughts += action.getLog();
            if(thoughts.endsWith("\n")) {
                thoughts = thoughts.substring(0, thoughts.length() - 1);
            }
            thoughts += "\nObservation: " + action.getObservation() + "\nThought:";
        }
        return thoughts;
    }

    public static String convertStructuredChatAgentTools(List<BaseTool> tools) {
        String toolDesc = "{name_for_model}: {description_for_model}, args: {parameters}";
        List<String> toolStrings = new ArrayList<>();
        for (BaseTool tool : tools) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("name_for_model", tool.getName());
            inputs.put("name_for_human", tool.getHumanName());
            inputs.put("description_for_model", tool.getDescription());
            String structSchema;
            if(tool instanceof StructuredTool) {
                StructuredTool structuredTool = (StructuredTool) tool;
                structSchema = structuredTool.formatStructSchema();
            } else {
                structSchema = Pattern.compile("\\}").matcher(Pattern.compile("\\{").matcher(tool.getArgs().toString()).replaceAll("{{")).replaceAll("}}");
            }
            inputs.put("parameters", !StringUtils.isEmpty(structSchema) ? structSchema : "{}");
            String toolString = PromptConverter.replacePrompt(toolDesc, inputs);
            toolStrings.add(toolString);
        }
        return String.join("\n", toolStrings);
    }

    public static String convertToolNames(List<BaseTool> tools) {
        return String.join(", ", tools.stream().map(BaseTool::getName).toArray(String[]::new));
    }
}

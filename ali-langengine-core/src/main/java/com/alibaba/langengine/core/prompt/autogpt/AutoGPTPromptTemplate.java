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
package com.alibaba.langengine.core.prompt.autogpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.prompt.impl.BaseChatPromptTemplate;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.util.DateUtils;
import com.alibaba.langengine.core.prompt.PromptConverter;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AutoGPTPromptTemplate
 *
 * @author xiaoxuan.lp
 */
@Data
public class AutoGPTPromptTemplate extends BaseChatPromptTemplate {

    private String aiName;

    private String aiRole;

    private List<BaseTool> tools;

    private Integer sendTokenLimit = 4196;

    public String constructFullPrompt(List<String> goals) {
        String promptStart = "Your decisions must always be made independently " +
            "without seeking user assistance.\n" +
            "Play to your strengths as an LLM and pursue simple " +
            "strategies with no legal complications.\n" +
            "If you have completed all your tasks, make sure to " +
            "use the \"finish\" command.";
        String fullPrompt = String.format("You are %s, %s\n%s\n\nGOALS:\n\n", aiName, aiRole, promptStart);

        for(int i = 0; i < goals.size(); i++) {
            String goal = goals.get(i);
            fullPrompt += String.format("%d. %s\n", i + 1, goal);
        }

        fullPrompt += String.format("\n\n%s", PromptGenerator.getPrompt(tools));
        return fullPrompt;
    }

    @Override
    public List<BaseMessage> formatMessages(Map<String, Object> args) {
        SystemMessage basePrompt = new SystemMessage();
        basePrompt.setContent(constructFullPrompt((List<String>)args.get("goals")));

        SystemMessage timePrompt = new SystemMessage();
        timePrompt.setContent("The current time and date is " + DateUtils.formatDate(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS));

        // TODO 需要解决token长度问题，可以采用tiktoken，https://github.com/openai/tiktoken
        int usedTokens = PromptConverter.tokenCounter(basePrompt.getContent()) + PromptConverter.tokenCounter(timePrompt.getContent());

        VectorStoreRetriever vectorStoreRetrieverMemory = null;
        if(args.containsKey("memory")) {
            vectorStoreRetrieverMemory = (VectorStoreRetriever) args.get("memory");
        }

        List<BaseMessage> previousMessages = null;
        if(args.containsKey("messages")) {
            previousMessages = (List<BaseMessage>) args.get("messages");
        }

        if(previousMessages.size() > 10) {
            previousMessages = previousMessages.stream()
                    .skip(previousMessages.size() - 10)
                    .collect(Collectors.toList());
        }
        List<Document> relevantDocs;
        if(previousMessages.size() > 0) {
            relevantDocs = vectorStoreRetrieverMemory.getRelevantDocuments(previousMessages.get(0).getContent());
        } else {
            relevantDocs = new ArrayList<>();
        }
        List<String> relevantMemory = relevantDocs.stream()
                .map(doc -> doc.getPageContent())
                .collect(Collectors.toList());

        String contentFormat = String.format("This reminds you of these events from your past:\n%s\n\n", JSON.toJSONString(relevantMemory));
        SystemMessage memoryMessage = new SystemMessage();
        memoryMessage.setContent(contentFormat);

        HumanMessage inputMessage = new HumanMessage();
        inputMessage.setContent((String) args.get("user_input"));
        List<BaseMessage> messages = new ArrayList<>();
        messages.add(basePrompt);
        messages.add(timePrompt);
        messages.add(memoryMessage);
        messages.add(inputMessage);

        return messages;
    }

    @Override
    public String getPromptType() {
        return "autogpt";
    }
}

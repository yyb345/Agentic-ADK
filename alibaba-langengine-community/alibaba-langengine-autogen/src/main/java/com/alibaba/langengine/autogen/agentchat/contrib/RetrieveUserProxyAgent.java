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
package com.alibaba.langengine.autogen.agentchat.contrib;

import com.alibaba.langengine.autogen.Agent;
import com.alibaba.langengine.autogen.agentchat.UserProxyAgent;
import com.alibaba.langengine.autogen.agentchat.support.ReplyResult;
import com.alibaba.langengine.autogen.tools.CheckUpdateContext;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RetrieveUserProxyAgent extends UserProxyAgent {
    private static final String PROMPT_DEFAULT = "You're a retrieve augmented chatbot. You answer user's questions based on your own knowledge and the context provided by the user. You should follow the following steps to answer a question:\n" +
            "Step 1, you estimate the user's intent based on the question and context. The intent can be a code generation task or\n" +
            "a question answering task.\n" +
            "Step 2, you reply based on the intent.\n" +
            "If you can't answer the question with or without the current context, you should reply exactly `UPDATE CONTEXT`.\n" +
            "If user's intent is code generation, you must obey the following rules:\n" +
            "Rule 1. You MUST NOT install any packages because all the packages needed are already installed.\n" +
            "Rule 2. You must follow the formats below to write your code:\n" +
            "```language\n" +
            "# your code\n" +
            "```\n" +
            "\n" +
            "If user's intent is question answering, you must give as short an answer as possible.\n" +
            "\n" +
            "User's question is: {input_question}\n" +
            "\n" +
            "Context is: {input_context}";

    private static final String PROMPT_CODE = "You're a retrieve augmented coding assistant. You answer user's questions based on your own knowledge and the context provided by the user.\n" +
            "If you can't answer the question with or without the current context, you should reply exactly `UPDATE CONTEXT`.\n" +
            "For code generation, you must obey the following rules:\n" +
            "Rule 1. You MUST NOT install any packages because all the packages needed are already installed.\n" +
            "Rule 2. You must follow the formats below to write your code:\n" +
            "```language\n" +
            "# your code\n" +
            "```\n" +
            "\n" +
            "User's question is: {input_question}\n" +
            "\n" +
            "Context is: {input_context}";

    private static final String PROMPT_QA = "You're a retrieve augmented chatbot. You answer user's questions based on your own knowledge and the context provided by the user.\n" +
            "If you can't answer the question with or without the current context, you should reply exactly `UPDATE CONTEXT`.\n" +
            "You must give as short an answer as possible.\n" +
            "\n" +
            "User's question is: {input_question}\n" +
            "\n" +
            "Context is: {input_context}";
    private static final String TASK_TYPE_DEFAULT = "DEFAULT";
    private static final String TASK_TYPE_CODE = "CODE";
    private static final String TASK_TYPE_QA = "QA";
    private RetrieveConfig retrieveConfig;
    private VectorStore vectorStore;
    private String problem;
    private String searchString;
    private Integer nResults;
    protected Map<String, Object> results;
    // 当前处理文档索引
    private int docIdx = -1;
    // 已处理文档ID
    private List<String> docIds = new ArrayList<>();
    // 已处理文档内容
    private List<String> docContents = new ArrayList<>();

    public RetrieveUserProxyAgent(String name, BaseLanguageModel llm, Map<String, Object> codeExecutionConfig) {
        super(name, llm, codeExecutionConfig);
        setHumanInputMode("ALWAYS");
        this.retrieveConfig = retrieveConfig;
    }

    @Override
    public Object generateReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null && sender == null) {
            String errorMsg = "Either messages or sender must be provided.";
            log.error(errorMsg);
            throw new AssertionError(errorMsg);
        }

        if (messages == null) {
            messages = getOaiMessages().get(sender);
        }

        ReplyResult replyResult = generateRetrieveUserReply(messages, sender);
        if (replyResult.isFinalFlag()) {
            return replyResult.getReply() != null ? replyResult.getReply() : replyResult.getOutput();
        }

        return super.generateReply(messages, sender);
    }

    private ReplyResult generateRetrieveUserReply(List<Map<String, Object>> messages, Agent sender) {
        if (messages == null) {
            messages = getOaiMessages().get(sender);
        }
        Map<String, Object> message = messages.get(messages.size() - 1);
        String content = (String) message.get("content");
        if (!retrieveConfig.isUpdateContext()) {
            return new ReplyResult(false, null);
        }
        if (!CheckUpdateContext.isUpdateContext(content)) {
            return new ReplyResult(false, null);
        }

        String docContents = getContext(this.results);
        if (docContents == null || docContents.isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                retrieveDocs(this.problem, nResults * (2 * i + 1), searchString);
                docContents = getContext(this.results);
                if (docContents != null && !"".equals(docContents)) {
                    break;
                }
            }
        }
        sender.clearHistory(null);
        this.clearHistory(null);
        String newmessage = generateMessage(docContents, this.retrieveConfig.getTask());
        return new ReplyResult(true, newmessage);
    }

    private String generateMessage(String docContents, String task) {
        if (docContents == null || docContents.isEmpty()) {
            log.error("No more context, will terminate.");
            return "TERMINATE";
        }
        String message = "";
        if (this.retrieveConfig.getCustomizedPrompt() != null) {
            message = this.retrieveConfig.getCustomizedPrompt().replace("{input_question}", this.problem).replace("{input_context}", docContents);
        } else if (task.equalsIgnoreCase(TASK_TYPE_CODE)) {
            message = PROMPT_CODE.replace("{input_question}", this.problem).replace("{input_context}", docContents);
        } else if (task.equalsIgnoreCase(TASK_TYPE_QA)) {
            message = PROMPT_QA.replace("{input_question}", this.problem).replace("{input_context}", docContents);
        } else if (task.equalsIgnoreCase(TASK_TYPE_DEFAULT)) {
            message = PROMPT_DEFAULT.replace("{input_question}", this.problem).replace("{input_context}", docContents);
        } else {
            throw new UnsupportedOperationException("task " + task + " is not implemented.");
        }
        return message;
    }

    @Override
    public String generateInitMessage(Map<String, Object> context) {
        // 初始化
        Integer nResults = (Integer) context.get("nResults");
        String searchString = (String) context.get("searchString");
        String problem = (String) context.get("message");
        retrieveDocs(problem, nResults, searchString);
        this.problem = problem;
        this.nResults = nResults;
        this.searchString = searchString;
        String docContents = getContext(this.results);
        String message = generateMessage(docContents, this.retrieveConfig.getTask());
        return message;
    }

    public void retrieveDocs(String problem, int nResults, String searchString) {
        this.results = new HashMap<>();
        List<Document> documentList = vectorStore.similaritySearch(problem, nResults);// 查询向量数据库
        List<String> ids = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        for (Document document : documentList) {
            ids.add(document.getUniqueId());
            documents.add(document.getPageContent());
        }
        this.results.put("ids", ids);
        this.results.put("documents", documents);
    }

    public String getContext(Map<String, Object> results) {
        String docContents = "";
        int currentTokens = 0;
        int docIdx = this.docIdx;
        int tmpRetrieveCount = 0;

        List<String> documents = (List<String>) results.get("documents");
        List<String> ids = (List<String>) results.get("ids");
        if (documents == null || documents.isEmpty()) {
            return docContents;
        }
        if (ids == null || ids.isEmpty()) {
            return docContents;
        }

        for (int idx = 0; idx < documents.size(); idx++) {
            if (idx <= docIdx) {
                continue;
            }
            if (this.docIds.contains(ids.get(idx))) {
                continue;
            }
            int docTokens = customTokenCountFunction(documents.get(idx), this.getLlm());
            if (docTokens > this.retrieveConfig.getContextMaxTokens()) {
                String funcPrint = "Skip doc_id " + ids.get(idx) + " as it is too long to fit in the context.";
                log.info(funcPrint);
                this.docIdx = idx;
                continue;
            }
            if (currentTokens + docTokens > this.retrieveConfig.getContextMaxTokens()) {
                break;
            }
            String funcPrint = "Adding doc_id " + ids.get(idx) + " to context.";
            log.info(funcPrint);
            currentTokens += docTokens;
            docContents += documents.get(idx) + "\n";
            this.docIdx = idx;
            this.docIds.add(ids.get(idx));
            this.docContents.add(documents.get(idx));
            tmpRetrieveCount++;
            if (tmpRetrieveCount >= this.nResults) {
                break;
            }
        }
        return docContents;
    }

    private int customTokenCountFunction(String doc, BaseLanguageModel llm) {
        return doc.length();
    }

    public void setRetrieveConfig(RetrieveConfig retrieveConfig) {
        this.retrieveConfig = retrieveConfig;
    }

    public void setVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
}

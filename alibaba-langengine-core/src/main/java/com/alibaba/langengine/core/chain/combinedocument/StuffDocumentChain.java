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
package com.alibaba.langengine.core.chain.combinedocument;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通过填充到上下文来组合文档的链
 *
 * @author xiaoxuan.lp
 */
@Data
public class StuffDocumentChain extends BaseCombineDocumentChain {

    private LLMChain llmChain;

    private BasePromptTemplate documentPrompt;

    private String documentVariableName;

    private String documentSeparator = "\n\n";

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        if (this.llmChain != null) {
            this.llmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
    }

    @Override
    public Map<String, Object> combineDocs(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        Map<String, Object> inputs = getInputs(docs, question);
        return llmChain.predict(inputs);
    }

    public int getPromptLength(List<Document> docs) {
        Map<String, Object> inputs = getInputs(docs, null);
        String prompt = llmChain.getPrompt().format(inputs);
        return llmChain.getLlm().getNumTokens(prompt);
    }

    private Map<String, Object> getInputs(List<Document> documents, String question) {
        List<String> docStrings = new ArrayList<>();
        documents.stream().forEach(document -> {
            String docString = formatDocument(document);
            docStrings.add(docString);
        });
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(documentVariableName, docStrings.stream().collect(Collectors.joining(documentSeparator)));
        inputs.put("question", question);
        inputs.put("documents", documents);
        return inputs;
    }

    private String formatDocument(Document document) {
        Map<String, Object> baseInfo = new HashMap<>();
        baseInfo.put("page_content", document.getPageContent());
        return documentPrompt.format(baseInfo);
    }
}

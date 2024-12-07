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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.alibaba.langengine.core.util.Constants.CALLBACK_ERROR_KEY;

/**
 * 链组合文档的基本接口
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseCombineDocumentChain extends Chain {
    private String inputKey = "input_documents";
    private String outputKey = "output_text";

    private BasePromptTemplate documentPrompt;

    private String documentVariableName;

    private String documentSeparator = "\n\n";

    @JsonIgnore
    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[]{inputKey});
    }

    @JsonIgnore
    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(new String[]{outputKey});
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        try {
            onChainStart(this, inputs, executionContext);

            List<Document> documents = (List<Document>) inputs.get("input_documents");
            String question = (String) inputs.get("question");
            Map<String, Object> outputs = combineDocs(documents, question, extraAttributes);

            onChainEnd(this, inputs, outputs, executionContext);

            return outputs;
        } catch (Throwable e) {
            onChainError(this, inputs, e, executionContext);

            if (getCallbackManager() != null) {
                Map<String, Object> outputs = new HashMap<>();
                outputs.put(CALLBACK_ERROR_KEY, executionContext);
                return outputs;
            }
            throw e;
        }
    }

    @Override
    public CompletableFuture<Map<String, Object>> callAsync(Map<String, Object> inputs, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> call(inputs, executionContext, null, extraAttributes));
    }

    public abstract Map<String, Object> combineDocs(List<Document> docs, String question, Map<String, Object> extraAttributes);
}

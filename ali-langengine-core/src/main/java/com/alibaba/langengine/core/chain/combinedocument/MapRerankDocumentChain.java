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
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Combining documents by mapping a chain over them, then reranking results.
 * 通过在文档上映射链来组合文档，然后对结果进行重新排名。
 *
 * @author xiaoxuan.lp
 */
@Data
public class MapRerankDocumentChain extends BaseCombineDocumentChain {

    /**
     * Chain to apply to each document individually.
     */
    private LLMChain llmChain;

    /**
     * The variable name in the llm_chain to put the documents in.
     * If only one variable in the llm_chain, this need not be provided.
     */
    private String documentVariableName;

    /**
     * Key in output of llm_chain to rank on.
     * 输入 llm_chain 的输出进行排名。
     */
    private String rankKey = "score";

    /**
     * Key in output of llm_chain to return as answer.
     */
    private String answerKey = "answer";

    /**
     * Return intermediate steps.
     * Intermediate steps include the results of calling llm_chain on each document.
     */
    private Boolean returnIntermediateSteps = false;

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        super.setCallbackManager(callbackManager);
        if (this.llmChain != null) {
            this.llmChain.setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
        }
    }

    @Override
    public List<String> getOutputKeys() {
        List<String> outputKeys = super.getOutputKeys();
        if(returnIntermediateSteps && !outputKeys.contains("intermediate_steps")) {
            outputKeys.add("intermediate_steps");
        }
        return outputKeys;
    }

    @Override
    public Map<String, Object> combineDocs(List<Document> docs, String question, Map<String, Object> extraAttributes) {
        List<Map<String, Object>> results = docs.stream().map(doc -> {
            Map<String, Object> inputs = getInputs(doc, question);
            Map<String, Object> outputs = (Map<String, Object>) llmChain.predictAndParse(inputs, extraAttributes);
            return outputs;
        }).collect(Collectors.toList());

        BigDecimal highScore = new BigDecimal(-1.0);
        Map<String, Object> highResult = null;
        for (Map<String, Object> result : results) {
            String scoreStr = (String) result.get(rankKey);
            BigDecimal score = new BigDecimal(scoreStr);
            if(score.compareTo(highScore) > 0) {
                highResult = new HashMap<>();
                highResult.putAll(result);
                highResult.put("text", result.get(answerKey));
                highScore = score;
            }
        }
        return highResult;
    }

    private Map<String, Object> getInputs(Document document, String question) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(documentVariableName, document.getPageContent());
        inputs.put("question", question);
        return inputs;
    }
}

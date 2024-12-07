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
package com.alibaba.langengine.core.chain.llmmath;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import com.alibaba.langengine.core.textsplitter.py.PythonCodeConstants;
import com.alibaba.langengine.core.util.PythonUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.langengine.core.chain.llmmath.PromptConstants.PROMPT;

/**
 * Chain that interprets a prompt and executes python code to do math.
 *
 * @author xiaoxuan.lp
 */
@Data
public class LLMMathChain extends Chain {

    private LLMChain llmChain;

    private String inputKey = "question";

    private String outputKey = "answer";

    public static LLMMathChain fromLlm(BaseLanguageModel llm) {
        return fromLlm(llm, null);
    }

    public static LLMMathChain fromLlm(BaseLanguageModel llm, BasePromptTemplate prompt) {
        if(prompt == null) {
            prompt = PROMPT;
        }
        LLMChain llmChain = new LLMChain();
        llmChain.setLlm(llm);
        llmChain.setPrompt(prompt);

        LLMMathChain llmMathChain = new LLMMathChain();
        llmMathChain.setLlmChain(llmChain);
        return llmMathChain;
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        inputs.put("stop", Arrays.asList(new Object[] { "```output" }));
        Map<String, Object> outputs = llmChain.predict(inputs);
        return processLlmResult(outputs.get("text").toString());
    }

    private Map<String, Object> processLlmResult(String llmOutput) {
        llmOutput = llmOutput.trim();
        Pattern pattern = Pattern.compile("```text(.*?)```?", Pattern.DOTALL);
        Matcher textMatch = pattern.matcher(llmOutput);
        if (textMatch.find()) {
            String expression = textMatch.group(1).replaceAll("\n", "");
            String output = evaluateExpression(expression);
            String answer = "Answer: " + output;
            return Collections.singletonMap(outputKey, answer);
        } else if (llmOutput.startsWith("Answer:")) {
            return Collections.singletonMap(outputKey, llmOutput);
        } else if (llmOutput.contains("Answer:")) {
            String answer = "Answer: " + llmOutput.split("Answer:")[1];
            return Collections.singletonMap(outputKey, answer);
        } else {
            throw new IllegalArgumentException("unknown format from LLM: " + llmOutput);
        }
    }

    private String evaluateExpression(String expression) {
        expression = expression.replaceAll("\\^", "**");
        String returnString = PythonUtils.invokePythonCode(PythonCodeConstants.LLMMATH_PYTHON_CODE, expression);
        return returnString;
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[]{ inputKey });
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(new String[]{ outputKey });
    }
}

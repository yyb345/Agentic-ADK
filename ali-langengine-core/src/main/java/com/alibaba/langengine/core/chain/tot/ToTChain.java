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
package com.alibaba.langengine.core.chain.tot;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.tot.strategy.BaseThoughtGenerationStrategy;
import com.alibaba.langengine.core.chain.tot.strategy.ProposePromptStrategy;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * A Chain implementing the Tree of Thought (ToT).
 * 实现思想树（ToT）的链。
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ToTChain extends Chain {


    /**
     * Language model to use. It must be set to produce different variations for the same prompt.
     * 要使用的语言模型。必须将其设置为针对同一提示产生不同的变体。
     */
    private BaseLanguageModel llm;

    /**
     * ToT Checker to use.
     * 要使用的 ToT 检查器。
     */
    private ToTChecker checker;

    private String outputKey = "response";

    /**
     * The maximum number of conversation rounds
     * 最大对话轮数
     */
    private int k = 10;

    /**
     * The number of children to explore at each node
     * 每个节点要探索的子节点数量
     */
    private int c = 3;

    private ToTDFSMemory totMemory = new ToTDFSMemory();

    private ToTController totController = new ToTController();

    BaseThoughtGenerationStrategy thoughtGenerator = new ProposePromptStrategy();


    private boolean verboseLlm = false;

    public ToTChain(BaseLanguageModel llm, ToTChecker checker, Integer c) {
        setLlm(llm);
        setChecker(checker);
        totController.setC(c);
        thoughtGenerator.setC(c);
        thoughtGenerator.setLlm(llm);
    }

    private ToTChain(BaseLanguageModel llm) {
        setLlm(llm);
        totController.setC(c);
        thoughtGenerator.setC(c);
        thoughtGenerator.setLlm(llm);
    }

    public static ToTChain fromLlm(BaseLanguageModel llm) {
        return new ToTChain(llm);
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String problemDescription = (String) inputs.get("problem_description");
        Map<String, Object> checkerInputs = new HashMap<>();
        checkerInputs.put("problem_description", problemDescription);
        List<String> thoughtsPath = new ArrayList<>();
        int level = 0;
        for (int i = 0; i < this.k; i++) {
            level = totMemory.getLevel();
            String thoughtText = thoughtGenerator.nextThought(problemDescription, thoughtsPath, extraAttributes);
            thoughtsPath.add(thoughtText);
            checkerInputs.put("thoughts", thoughtsPath);
            String thoughtValidityStr= checker.run(checkerInputs).get("validity").toString();
            ThoughtValidity thoughtValidity = ThoughtValidity.valueOf(thoughtValidityStr);
            Thought thought = new Thought(thoughtText, thoughtValidity);
            if (thought.getValidity().equals(ThoughtValidity.VALID_FINAL)) {
                logThought(thought, level);
                return Collections.singletonMap(outputKey, thought.getText());
            }
            this.totMemory.store(thought);
            logThought(thought, level);
            thoughtsPath = totController.call(totMemory);
        }
        return Collections.singletonMap(this.outputKey, "No solution found");
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[] { "problem_description" });
    }

    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList(outputKey);
    }

    private void logThought(Thought thought, int level) {
        String text = indent(thought.getValidity() + " Thought: " + thought.getText() + "\n", "    ", level);
        log.warn(text);
    }

    private String indent(String text, String prefix, int level) {
        StringBuilder indentedText = new StringBuilder();
        String indentation = repeat(prefix, level);
        String[] lines = text.split("\n");
        for (String line : lines) {
            indentedText.append(indentation).append(line).append("\n");
        }
        return indentedText.toString();
    }

    private String repeat(String prefix, int level) {
        StringBuilder repeatedString = new StringBuilder();
        for (int i = 0; i < level; i++) {
            repeatedString.append(prefix);
        }
        return repeatedString.toString();
    }
}

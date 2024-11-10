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
package com.alibaba.langengine.demo.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chain.tot.ToTChain;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.demo.chain.support.MyChecker;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ToTChainTest {

    @Test
    public void test_run() {
        // success
        String sudokuSolution = "3,4,1,2|1,2,3,4|2,1,4,3|4,3,2,1";
        String sudokuPuzzle = "3,*,*,2|1,*,3,*|*,1,*,3|4,*,*,1";
        String problemDescriptionPrompt = "{sudoku_puzzle}\n" +
                "\n" +
                "- This is a 4x4 Sudoku puzzle.\n" +
                "- The * represents a cell to be filled.\n" +
                "- The | character separates rows.\n" +
                "- At each step, replace one or more * with digits 1-4.\n" +
                "- There must be no duplicate digits in any row, column or 2x2 subgrid.\n" +
                "- Keep the known digits from previous valid thoughts in place.\n" +
                "- Each thought can be a partial or the final solution.";
        Map<String, Object> params = new HashMap<>();
        params.put("sudoku_puzzle", sudokuPuzzle);
        String problemDescription = PromptConverter.replacePrompt(problemDescriptionPrompt, params);

        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(1d);
        llm.setMaxTokens(512);
//        llm.setModelName("text-davinci-003");

        ToTChain chain = new ToTChain(llm, new MyChecker(sudokuSolution), 5);
        chain.setK(30);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("problem_description", problemDescription);
        Map<String, Object> result = chain.run(inputs);
        System.out.println(JSON.toJSONString(result));
    }
}

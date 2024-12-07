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
package com.alibaba.langengine.metagpt.actions;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.metagpt.Message;

import java.util.List;

public class WriteTest extends Action {

    private static final String PROMPT_TEMPLATE = "NOTICE\n" +
            "1. Role: You are a QA engineer; the main goal is to design, develop, and execute PEP8 compliant, well-structured, maintainable test cases and scripts for Python 3.9. Your focus should be on ensuring the product quality of the entire project through systematic testing.\n" +
            "2. Requirement: Based on the context, develop a comprehensive test suite that adequately covers all relevant aspects of the code file under review. Your test suite will be part of the overall project QA, so please develop complete, robust, and reusable test cases.\n" +
            "3. Attention1: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the test case or script.\n" +
            "4. Attention2: If there are any settings in your tests, ALWAYS SET A DEFAULT VALUE, ALWAYS USE STRONG TYPE AND EXPLICIT VARIABLE.\n" +
            "5. Attention3: YOU MUST FOLLOW \"Data structures and interface definitions\". DO NOT CHANGE ANY DESIGN. Make sure your tests respect the existing design and ensure its validity.\n" +
            "6. Think before writing: What should be tested and validated in this document? What edge cases could exist? What might fail?\n" +
            "7. CAREFULLY CHECK THAT YOU DON'T MISS ANY NECESSARY TEST CASES/SCRIPTS IN THIS FILE.\n" +
            "Attention: Use '##' to split sections, not '#', and '## <SECTION_NAME>' SHOULD WRITE BEFORE the test case or script and triple quotes.\n" +
            "-----\n" +
            "## Given the following code, please write appropriate test cases using Python's unittest framework to verify the correctness and robustness of this code:\n" +
            "```python\n" +
            "{code_to_test}\n" +
            "```\n" +
            "Note that the code to test is at {source_file_path}, we will put your test code at {workspace}/tests/{test_file_name}, and run your test code from {workspace},\n" +
            "you should correctly import the necessary classes based on these file locations!\n" +
            "## {test_file_name}: Write test code with triple quoto. Do your best to implement THIS ONLY ONE FILE.";

    public WriteTest(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}

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

public class RunCode extends Action {

    private static final String PROMPT_TEMPLATE = "Role: You are a senior development and qa engineer, your role is summarize the code running result.\n" +
            "If the running result does not include an error, you should explicitly approve the result.\n" +
            "On the other hand, if the running result indicates some error, you should point out which part, the development code or the test code, produces the error,\n" +
            "and give specific instructions on fixing the errors. Here is the code info:\n" +
            "{context}\n" +
            "Now you should begin your analysis\n" +
            "---\n" +
            "## instruction:\n" +
            "Please summarize the cause of the errors and give correction instruction\n" +
            "## File To Rewrite:\n" +
            "Determine the ONE file to rewrite in order to fix the error, for example, xyz.py, or test_xyz.py\n" +
            "## Status:\n" +
            "Determine if all of the code works fine, if so write PASS, else FAIL,\n" +
            "WRITE ONLY ONE WORD, PASS OR FAIL, IN THIS SECTION\n" +
            "## Send To:\n" +
            "Please write Engineer if the errors are due to problematic development codes, and QaEngineer to problematic test codes, and NoOne if there are no errors,\n" +
            "WRITE ONLY ONE WORD, Engineer OR QaEngineer OR NoOne, IN THIS SECTION.\n" +
            "---\n" +
            "You should fill in necessary instruction, status, send to, and finally return all content between the --- segment line.";

    public static final String CONTEXT = "## Development Code File Name\n" +
            "{code_file_name}\n" +
            "## Development Code\n" +
            "```python\n" +
            "{code}\n" +
            "```\n" +
            "## Test File Name\n" +
            "{test_file_name}\n" +
            "## Test Code\n" +
            "```python\n" +
            "{test_code}\n" +
            "```\n" +
            "## Running Command\n" +
            "{command}\n" +
            "## Running Output\n" +
            "standard output: {outs};\n" +
            "standard errors: {errs};";

    public RunCode(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}

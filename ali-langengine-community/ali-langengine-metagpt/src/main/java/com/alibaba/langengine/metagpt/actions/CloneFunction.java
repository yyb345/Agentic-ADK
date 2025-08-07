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

public class CloneFunction extends Action {

    private static final String CLONE_PROMPT = "*context*\n" +
            "Please convert the function code ```{source_code}``` into the the function format: ```{template_func}```.\n" +
            "*Please Write code based on the following list and context*\n" +
            "1. Write code start with ```, and end with ```.\n" +
            "2. Please implement it in one function if possible, except for import statements. for example:\n" +
            "```python\n" +
            "import pandas as pd\n" +
            "def run(*args) -> pd.DataFrame:\n" +
            "    ...\n" +
            "```\n" +
            "3. Do not use public member functions that do not exist in your design.\n" +
            "4. The output function name, input parameters and return value must be the same as ```{template_func}```.\n" +
            "5. Make sure the results before and after the code conversion are required to be exactly the same.\n" +
            "6. Don't repeat my context in your replies.\n" +
            "7. Return full results, for example, if the return value has df.head(), please return df.\n" +
            "8. If you must use a third-party package, use the most popular ones, for example: pandas, numpy, ta, ...";

    public CloneFunction(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}

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

public class WriteDocstring extends Action {

    private String PYTHON_DOCSTRING_SYSTEM = "### Requirements\n" +
            "1. Add docstrings to the given code following the {style} style.\n" +
            "2. Replace the function body with an Ellipsis object(...) to reduce output.\n" +
            "3. If the types are already annotated, there is no need to include them in the docstring.\n" +
            "4. Extract only class, function or the docstrings for the module parts from the given Python code, avoiding any other text.\n" +
            "\n" +
            "### Input Example\n" +
            "```python\n" +
            "def function_with_pep484_type_annotations(param1: int) -> bool:\n" +
            "    return isinstance(param1, int)\n" +
            "\n" +
            "class ExampleError(Exception):\n" +
            "    def __init__(self, msg: str):\n" +
            "        self.msg = msg\n" +
            "```\n" +
            "\n" +
            "### Output Example\n" +
            "```python\n" +
            "{example}\n" +
            "```";

    private static final String PYTHON_DOCSTRING_EXAMPLE_GOOGLE = "def function_with_pep484_type_annotations(param1: int) -> bool:\n" +
            "    \"\"\"Example function with PEP 484 type annotations.\n" +
            "\n" +
            "    Extended description of function.\n" +
            "\n" +
            "    Args:\n" +
            "        param1: The first parameter.\n" +
            "\n" +
            "    Returns:\n" +
            "        The return value. True for success, False otherwise.\n" +
            "    \"\"\"\n" +
            "    ...\n" +
            "\n" +
            "class ExampleError(Exception):\n" +
            "    \"\"\"Exceptions are documented in the same way as classes.\n" +
            "\n" +
            "    The __init__ method was documented in the class level docstring.\n" +
            "\n" +
            "    Args:\n" +
            "        msg: Human readable string describing the exception.\n" +
            "\n" +
            "    Attributes:\n" +
            "        msg: Human readable string describing the exception.\n" +
            "    \"\"\"\n" +
            "    ...";

    private static final String PYTHON_DOCSTRING_EXAMPLE_NUMPY = "def function_with_pep484_type_annotations(param1: int) -> bool:\n" +
            "    \"\"\"\n" +
            "    Example function with PEP 484 type annotations.\n" +
            "\n" +
            "    Extended description of function.\n" +
            "\n" +
            "    Parameters\n" +
            "    ----------\n" +
            "    param1\n" +
            "        The first parameter.\n" +
            "\n" +
            "    Returns\n" +
            "    -------\n" +
            "    bool\n" +
            "        The return value. True for success, False otherwise.\n" +
            "    \"\"\"\n" +
            "    ...\n" +
            "\n" +
            "class ExampleError(Exception):\n" +
            "    \"\"\"\n" +
            "    Exceptions are documented in the same way as classes.\n" +
            "\n" +
            "    The __init__ method was documented in the class level docstring.\n" +
            "\n" +
            "    Parameters\n" +
            "    ----------\n" +
            "    msg\n" +
            "        Human readable string describing the exception.\n" +
            "\n" +
            "    Attributes\n" +
            "    ----------\n" +
            "    msg\n" +
            "        Human readable string describing the exception.\n" +
            "    \"\"\"\n" +
            "    ...";

    private static final String PYTHON_DOCSTRING_EXAMPLE_SPHINX = "def function_with_pep484_type_annotations(param1: int) -> bool:\n" +
            "    \"\"\"Example function with PEP 484 type annotations.\n" +
            "\n" +
            "    Extended description of function.\n" +
            "\n" +
            "    :param param1: The first parameter.\n" +
            "    :type param1: int\n" +
            "\n" +
            "    :return: The return value. True for success, False otherwise.\n" +
            "    :rtype: bool\n" +
            "    \"\"\"\n" +
            "    ...\n" +
            "\n" +
            "class ExampleError(Exception):\n" +
            "    \"\"\"Exceptions are documented in the same way as classes.\n" +
            "\n" +
            "    The __init__ method was documented in the class level docstring.\n" +
            "\n" +
            "    :param msg: Human-readable string describing the exception.\n" +
            "    :type msg: str\n" +
            "    \"\"\"\n" +
            "    ...";

    public WriteDocstring(String name, Object context, BaseLanguageModel llm) {
        super(name, context, llm);
    }

    @Override
    public ActionOutput run(List<Message> messages) {
        return null;
    }
}

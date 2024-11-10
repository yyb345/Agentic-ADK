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
package com.alibaba.langengine.core.textsplitter.py;

/**
 * python code常量
 *
 * @author xiaoxuan.lp
 */
public class PythonCodeConstants {

     public static final String HTML_TEXT_SPLITTER_PYTHON_CODE = "import sys; import json; from langchain.text_splitter import RecursiveCharacterTextSplitter, Language; text = '%s'; textArray = json.loads(text); html_splitter = RecursiveCharacterTextSplitter.from_language(language=Language.HTML, chunk_size=60, chunk_overlap=0); html_docs = html_splitter.create_documents(textArray); print(json.dumps(html_docs, default=lambda html_docs: html_docs.__dict__))";

     public static final String PYTHON_TEXT_SPLITTER_PYTHON_CODE = "import sys; import json; from langchain.text_splitter import RecursiveCharacterTextSplitter, Language; text = '%s'; textArray = json.loads(text); python_splitter = RecursiveCharacterTextSplitter.from_language(language=Language.PYTHON, chunk_size=50, chunk_overlap=0); python_docs = python_splitter.create_documents(textArray); print(json.dumps(python_docs, default=lambda python_docs: python_docs.__dict__))";

     public static final String JS_TEXT_SPLITTER_PYTHON_CODE = "import sys; import json; from langchain.text_splitter import RecursiveCharacterTextSplitter, Language; text = '%s'; textArray = json.loads(text); js_splitter = RecursiveCharacterTextSplitter.from_language(language=Language.JS, chunk_size=60, chunk_overlap=0); js_docs = js_splitter.create_documents(textArray); print(json.dumps(js_docs, default=lambda js_docs: js_docs.__dict__))";

     public static final String MARKDOWN_TEXT_SPLITTER_PYTHON_CODE = "import sys; import json; from langchain.text_splitter import RecursiveCharacterTextSplitter, Language; text = '%s'; textArray = json.loads(text); md_splitter = RecursiveCharacterTextSplitter.from_language(language=Language.MARKDOWN, chunk_size=60, chunk_overlap=0); md_docs = md_splitter.create_documents(textArray); print(json.dumps(md_docs, default=lambda md_docs: md_docs.__dict__))";

     public static final String LATEX_TEXT_SPLITTER_PYTHON_CODE = "import sys; import json; from langchain.text_splitter import RecursiveCharacterTextSplitter, Language; text = '%s'; textArray = json.loads(text); latex_splitter = RecursiveCharacterTextSplitter.from_language(language=Language.LATEX, chunk_size=60, chunk_overlap=0); latex_docs = latex_splitter.create_documents(textArray); print(json.dumps(latex_docs, default=lambda latex_docs: latex_docs.__dict__))";

     public static final String JAVA_TEXT_SPLITTER_PYTHON_CODE = "import sys; import json; from langchain.text_splitter import RecursiveCharacterTextSplitter, Language; text = '%s'; textArray = json.loads(text); java_splitter = RecursiveCharacterTextSplitter.from_language(language=Language.JAVA, chunk_size=60, chunk_overlap=0); java_docs = java_splitter.create_documents(textArray); print(json.dumps(java_docs, default=lambda java_docs: java_docs.__dict__))";

     /**
      * llmmath运行脚本
      */
     public static final String LLMMATH_PYTHON_CODE = "import sys; import math; import numpy as np; import numexpr as ne; input = '%s'; res = ne.evaluate(input); print(res)";
}

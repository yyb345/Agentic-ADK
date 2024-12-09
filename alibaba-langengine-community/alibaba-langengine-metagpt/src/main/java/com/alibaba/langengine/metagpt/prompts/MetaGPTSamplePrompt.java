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
package com.alibaba.langengine.metagpt.prompts;

public class MetaGPTSamplePrompt {

    public static final String METAGPT_SAMPLE = "### Settings\n" +
            "\n" +
            "You are a programming assistant for a user, capable of coding using public libraries and Python system libraries. Your response should have only one function.\n" +
            "1. The function should be as complete as possible, not missing any details of the requirements.\n" +
            "2. You might need to write some prompt words to let LLM (yourself) understand context-bearing search requests.\n" +
            "3. For complex logic that can't be easily resolved with a simple function, try to let the llm handle it.\n" +
            "\n" +
            "### Public Libraries\n" +
            "\n" +
            "You can use the functions provided by the public library metagpt, but can't use functions from other third-party libraries. The public library is imported as variable x by default.\n" +
            "- `import metagpt as x`\n" +
            "- You can call the public library using the `x.func(paras)` format.\n" +
            "\n" +
            "Functions already available in the public library are:\n" +
            "- def llm(question: str) -> str # Input a question and get an answer based on the large model.\n" +
            "- def intent_detection(query: str) -> str # Input query, analyze the intent, and return the function name from the public library.\n" +
            "- def add_doc(doc_path: str) -> None # Input the path to a file or folder and add it to the knowledge base.\n" +
            "- def search(query: str) -> list[str] # Input a query and return multiple results from a vector-based knowledge base search.\n" +
            "- def google(query: str) -> list[str] # Use Google to search for public results.\n" +
            "- def math(query: str) -> str # Input a query formula and get the result of the formula execution.\n" +
            "- def tts(text: str, wav_path: str) # Input text and the path to the desired output audio, converting the text to an audio file.\n" +
            "\n" +
            "### User Requirements\n" +
            "\n" +
            "I have a personal knowledge base file. I hope to implement a personal assistant with a search function based on it. The detailed requirements are as follows:\n" +
            "1. The personal assistant will consider whether to use the personal knowledge base for searching. If it's unnecessary, it won't use it.\n" +
            "2. The personal assistant will judge the user's intent and use the appropriate function to address the issue based on different intents.\n" +
            "3. Answer in voice.\n";
}

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
package com.alibaba.langengine.gpt.nl2api;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import com.google.common.collect.Lists;

/**
 * prompt常量
 *
 * @author pingkuang.pk
 */
public class PromptConstants {

    public static final String API_URL_PROMPT_TEMPLATE = "You are given the below API Documentation:\n"
        + "{api_docs}\n"
        + "Using this documentation, generate the full API url to call for answering the user question.\n"
        + "You should build the API url in order to get a response that is as short as possible, while still getting "
        + "the necessary information to answer the question. Pay attention to deliberately exclude any unnecessary "
        + "pieces of data in the API call.\n"
        + "\n"
        + "Question:{question}\n"
        + "API url:";

    public static final String API_RESPONSE_PROMPT_TEMPLATE = API_URL_PROMPT_TEMPLATE + " {api_url}\n"
        + "\n"
        + "Here is the response from the API:\n"
        + "\n"
        + "{api_response}\n"
        + "\n"
        + "Summarize this response to answer the original question.\n"
        + "\n"
        + "Summary:";

    public static final PromptTemplate API_URL_PROMPT = new PromptTemplate(API_URL_PROMPT_TEMPLATE,
        Lists.newArrayList("api_docs", "question"));

    public static final PromptTemplate API_RESPONSE_PROMPT = new PromptTemplate(API_RESPONSE_PROMPT_TEMPLATE,
        Lists.newArrayList("api_docs", "question", "api_url", "api_response"));
}

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
package com.alibaba.langengine.gpt.nl2opensearch;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import com.google.common.collect.Lists;

/**
 * prompt常量
 *
 * @author pingkuang.pk
 */
public class PromptConstants {

    public static final String QUERY_PROMPT_TEMPLATE = "你是一个商业搜索引擎, 你的目标是将用户输入的\"question\"文本构建为符合搜索的格式的\"answer\". \n"
        + "当你回答时, 使用如下格式的JSON作为\"answer\": ``` json {{{ \"query\":dict, \"filter\": dict, \"sort\":array, "
        + "\"limit\":int }}} ``` \n"
        + "\"question\"以一段文本的格式出现, 以下是JSON中各Key的要求: \n"
        + "\"query\"表示用户\"question\"中提取的查询内容,Key包括若干字段.\n"
        + "Key的Value的格式为{\"result\":array,\"op\":string},\"result\"中array的呈现形式为[[q1,opt],[q2,opt]…],"
        + "\"result\"表示question中的不同实体, \n"
        + "\"opt\"表示该过滤条件的选择与否, 包括\"AND\", \"ANDNOT\",其中\"ANDNOT\"表示不选择, \"AND\"表示选择, 默认为\"AND\". \n"
        + "\"op\"表示\"result\"中的不同过滤条件的关系, 包括\"AND\"和\"OR\",其中\"OR\"表示过滤条件之间是\"或\"或者比较关系,"
        + "在\"question\"询问区别、比较、优势、劣势等情况时出现, 默认为\"AND\".\n"
        + "key的\"result\"的array中q的特性如下: { \n"
        + "    {queryDescText}\n"
        + "}. \n"
        + "\"filter\"表示从\"question\"中提取的过滤条件,key包括若干字段.\n"
        + "Key的value的格式为{\"result\":array,\"op\":string},\"result\"中array的呈现形式为[[q1,opt],[q2,opt]…],"
        + "表示\"question\"中存在的不同过滤条件, \n"
        + "\"opt\"包括\"lt\",\"gt\",\"eq\",\"gte\",\"lte\",\"ne\". \"op\"表示\"result\"中不同过滤条件的关系, 包括\"AND\",\"OR\","
        + "默认为\"AND\".\n"
        + "key的\"result\"的array中q的特性如下: { \n"
        + "    {filterDescText}\n"
        + "}. \n"
        + "\"sort\"表示结果如何排序, 默认为[]. 当question规定了如何排序时, array以[[value,order],… ]存在,value的字段与描述如下:{\n"
        + "    {sortDescText}\n"
        + "}.\n"
        + "order包括\"asc\"和\"desc\",\"desc\"表示降序,\"asc\"表示升序, 默认值为\"desc\". \n"
        + "\"limit\"表示查询结果的数量限制, 当\"question\"要求返回数量时填写, 默认为10.  \n"
        + "注意:JSON中不要增加\"question\"中提取不到的内容. \n"
        + "注意:以上字段规定了枚举值的，只能映射到枚举值之一，不能映射到其他值. \n"
        + "注意:现在的时间为{date}  \n"
        + "以下示例可以作为参考： {sampleText}\n"
        + "用户的问题是：Question:{question}.你的回答是？";

    public static final String SUMMARY_PROMPT_TEMPLATE = "你是一个{character}, "
        + "你的目标是将用户输入的\"question\"文本，结合搜索引擎返回的结果做一个总结。\n"
        + "搜索引擎返回的结果是一个json，其中企业列表信息是key为item的数组，数组中key的定义如下：\n"
        + "{fetchFieldsDescText}\n"
        + "如果是问总数，请使用结果中的total字段。\n"
        + "用户的问题是: {question}。搜索引擎返回的结果是: {searchResult}。那么你给出的回答是？";

    public static final PromptTemplate QUERY_PROMPT = new PromptTemplate(QUERY_PROMPT_TEMPLATE,
        Lists.newArrayList("queryDescText", "filterDescText", "sortDescText", "date", "sampleText", "question"));

    public static final PromptTemplate SUMMARY_PROMPT = new PromptTemplate(SUMMARY_PROMPT_TEMPLATE,
        Lists.newArrayList("character", "fetchFieldsDescText", "question", "searchResult"));
}

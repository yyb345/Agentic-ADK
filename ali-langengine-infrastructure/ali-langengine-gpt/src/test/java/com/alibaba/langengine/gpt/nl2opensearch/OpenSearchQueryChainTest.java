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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.langengine.core.model.FakeAI;
import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchConfig;
import com.alibaba.langengine.gpt.nl2opensearch.domain.PromptConfig;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

public class OpenSearchQueryChainTest {

    @Test
    public void testRun() {
        // success
        ChatOpenAI llm = new ChatOpenAI();

        OpenSearchConfig openSearchConfig = new OpenSearchConfig();
        openSearchConfig
            .setAppName("qidian_inner_main_search_llm")
            .setAccessKey("")
            .setSecret("")
            .setFetchFields(
                Lists.newArrayList("onecomp_id", "ent_name", "industry_name_lv1", "industry_name_lv2", "reg_org_city"));

        PromptConfig promptConfig = new PromptConfig();
        promptConfig.setQueryDescText(QUERY_DESC_TEXT)
            .setFilterDescText(FILTER_DESC_TEXT)
            .setSortDectText(SORT_DESC_TEXT)
            .setSampleText(SAMPLE_TEXT)
            .setCharacter("熟知全域企业商业信息的市场分析师")
            .setFetchFieldsDescText(FETCH_FIELDS_DESC_TEXT);

        OpenSearchQueryChain openSearchQueryChain = OpenSearchQueryChain.fromLlmAndConfig(llm, openSearchConfig,
            promptConfig);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(OpenSearchQueryChain.QUESTION_KEY, "广州注册资本大于一千万的工业或零售业企业TOP3");
        // returnQueryParam为true，仅返回查询参数，不执行查询
        inputs.put(OpenSearchQueryChain.RETURN_QUERY_PARAM, false);
        // needSummary为false，仅返回查询结果，不做总结润色
        inputs.put(OpenSearchQueryChain.NEED_SUMMARY_KEY, true);
        inputs.put(OpenSearchQueryChain.MOCK_DATA, true);
        Map<String, Object> result = openSearchQueryChain.run(inputs);
        System.out.println("==========================================");
        System.out.println(result.get(OpenSearchQueryChain.QUERY_PARAM));
        System.out.println("==========================================");
        System.out.println(result.get(OpenSearchQueryChain.RAW_RESULT));
        System.out.println("==========================================");
        System.out.println(result.get(OpenSearchQueryChain.OUTPUT_KEY));
    }

    private final static String QUERY_DESC_TEXT = "\"reg_org_province\":string,//企业所在的省份,你需要识别并转化为标准省份名称,"
        + "如'浙江省'、'新疆维吾尔自治区'.注意：其中如果是上海、北京、重庆、天津四个直辖市，城市和省份都带上“市”字. \n"
        + "\"reg_org_city\":string,//企业所在的城市. \n"
        + "\"reg_org_district\":string,//企业所在的区县"
        + ".请把区县对应的城市reg_org_city也识别出来作为query条件。注意：其中如果是上海、北京、重庆、天津四个直辖市，城市和省份都带上“市”字. \n"
        + "\"default\":string,//包含企业以下任一关键信息：企业名称、企业简称、英文名称、曾用名、法人名称、统一社会信用编码、股东名称、主要人员名称. \n"
        + "\"ind_index\":string,//包含以下任一关键信息：所属行业、产业链、经营范围.例如：制造业、批发业、零售业、餐饮业、信息业、农业、教育业. \n"
        + "\"rel_index\":string,//包含以下任一关键信息：企业拥有的品牌、商标、店铺、商品或产品名称. \n"
        + "\"is_above_scale\":string,//是否规模以上企业，需要转化为枚举值：Y、N. \n"
        + "\"have_webshop\":string,//是否有网店，需要转化为枚举值：Y、N. \n"
        + "\"is_legal_comp\":string,//是否法人企业，需要转化为枚举值：Y、N. 注意：如果用户query包含'个体户'，则令is_legal_comp='N'. \n"
        + "\"is_amazon\":string,//是否亚马逊商家，需要转化为枚举值：Y、N. \n"
        + "\"is_have_mobile_phone\":string,//是否有移动电话，需要转化为枚举值：Y、N. \n"
        + "\"is_factory\":string,//是否是工厂，需要转化为枚举值：Y、N. 注意：当用户提及\"工业\"时，请忽略该条件. \n"
        + "\"ebiz_service_level\":string,//电商规模，需要转化为以下枚举值：AAAAA、AAAA、AAA、AA、A。其中AAAAA代表规模最大，A"
        + "代表规模最小。可以按照你的理解进行归类，比如提问“大规模”，可以筛选出AAAAA与AAAA. \n"
        + "\"is_ebusiness\":string,//是否经营电商，需要转化为枚举值：Y、N. \n"
        + "\"is_kp_recognized\":string,//是否能识别出企业核心人员，需要转化为枚举值：Y、N. \n"
        + "\"is_special\":string,//是否专精特新企业，需要转化为枚举值：Y、N. \n"
        + "\"ent_address\":string,//企业的注册地址. \n"
        + "\"tm_is_foreigntrade\":string,//是否外贸企业，需要转化为枚举值：Y、N. \n"
        + "\"alg_score\":string,//企业电话预测接通率，需要转化为枚举值：高、较高、中、低. ";

    private final static String FILTER_DESC_TEXT = "\"employee_num_sort\":int,//企业的员工人数. \n"
        + "\"es_date_timestamp\":int,//企业成立时间，转化为UTC时间戳. \n"
        + "\"ebiz_year\":int,//企业电商开展年限. \n"
        + "\"std_reg_cap\":double,//企业注册资本（单位：元）. ";

    private final static String SORT_DESC_TEXT = "\"std_reg_cap\":int,//注册资本.\n"
        + "\"es_date_timestamp\":int,//注册时间.\n"
        + "\"ebiz_year\":int,/电商开始年份.\n"
        + "\"employee_num_sort\":int,//员工数量";

    private final static String SAMPLE_TEXT = "question: 杭州市名称中带阿里巴巴的企业，按照注册资本从高到低的前三家  answer:{\"query\": "
        + "{\"default\": {\"result\": [[\"阿里巴巴\",\"AND\"]],\"op\": \"AND\"},\"reg_org_city\": {\"result\": [[\"杭州市\","
        + "\"AND\"]],\"op\": \"AND\"}},\"filter\": {},\"sort\": [[\"std_reg_cap\",\"desc\"]], \"limit\": 3} \n"
        + "question: 品牌中带雅诗兰黛的企业  answer:{\"query\": {\"rel_index\": {\"result\": [[\"雅诗兰黛\",\"AND\"]],\"op\": "
        + "\"AND\"}},\"filter\": {},\"sort\": [[\"std_reg_cap\",\"desc\"]], \"limit\": 10} \n"
        + "question: 广州电商企业，并且电商年份在10年以上  answer:{\"query\": {\"reg_org_city\": {\"result\": [[\"广州市\", \"AND\"]],"
        + "\"op\": \"AND\"},\"is_ebusiness\": {\"result\": [[\"Y\", \"AND\"]],\"op\": \"AND\"}},\"filter\": "
        + "{\"ebiz_year\": {\"result\": [[10,\"gte\"]],\"op\": \"AND\"}},\"sort\": [], \"limit\": 10} \n"
        + "question: 马云控股的大于100万的工业企业，按照成立时间倒序  answer:{\"query\": {\"default\": {\"result\": [[\"马云\", \"AND\"]],"
        + "\"op\": \"AND\"},\"ind_index\": {\"result\": [[\"工业\", \"AND\"]],\"op\": \"AND\"}},\"filter\": "
        + "{\"std_reg_cap\": {\"result\": [[1000000,\"gte\"]],\"op\": \"AND\"}},\"sort\": [[\"es_date_timestamp\","
        + "\"desc\"]],\"limit\": 10} \n"
        + "question: 江苏省南京市外贸个体户，且接通率较高  answer:{\"query\": {\"reg_org_province\": {\"result\": [[\"江苏省\", \"AND\"]],"
        + "\"op\": \"AND\"},\"reg_org_city\": {\"result\": [[\"南京市\", \"AND\"]],\"op\": \"AND\"},"
        + "\"tm_is_foreigntrade\": {\"result\": [[\"Y\", \"AND\"]],\"op\": \"AND\"},\"is_legal_comp\": {\"result\": "
        + "[[\"N\", \"AND\"]],\"op\": \"AND\"},\"alg_score\": {\"result\": [[\"高\", \"AND\"],[\"较高\", \"AND\"]],"
        + "\"op\": \"OR\"}},\"filter\": {},\"sort\": [],\"limit\": 10}";

    private final static String FETCH_FIELDS_DESC_TEXT
        = "onecomp_id:企业id，ent_name:企业名称，industry_name_lv1:主营一节类目，industry_name_lv1:主营二级类目，reg_org_city:注册城市";
}

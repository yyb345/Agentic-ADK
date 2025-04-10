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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchConfig;
import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchQuery;
import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchReturnModel;
import com.alibaba.langengine.gpt.nl2opensearch.domain.OpenSearchSegment;
import com.aliyun.opensearch.OpenSearchClient;
import com.aliyun.opensearch.SearcherClient;
import com.aliyun.opensearch.sdk.generated.OpenSearch;
import com.aliyun.opensearch.sdk.generated.search.Config;
import com.aliyun.opensearch.sdk.generated.search.Order;
import com.aliyun.opensearch.sdk.generated.search.SearchFormat;
import com.aliyun.opensearch.sdk.generated.search.SearchParams;
import com.aliyun.opensearch.sdk.generated.search.Sort;
import com.aliyun.opensearch.sdk.generated.search.SortField;
import com.aliyun.opensearch.sdk.generated.search.general.SearchResult;
import com.aliyun.opensearch.search.SearchParamsBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class OpenSearchWrapper {

    private SearcherClient searcherClient;

    private final OpenSearchConfig openSearchConfig;

    public OpenSearchWrapper(OpenSearchConfig openSearchConfig) {
        if (StringUtils.isNotBlank(openSearchConfig.getAccessKey()) && StringUtils.isNotBlank(
            openSearchConfig.getSecret())) {
            OpenSearch openSearch = new OpenSearch(openSearchConfig.getAccessKey(), openSearchConfig.getSecret(),
                openSearchConfig.getHost());
            OpenSearchClient serviceClient = new OpenSearchClient(openSearch);
            this.searcherClient = new SearcherClient(serviceClient);
        }
        this.openSearchConfig = openSearchConfig;
    }

    /**
     * 查询OpenSearch
     */
    public OpenSearchReturnModel search(SearchParamsBuilder paramsBuilder) {
        Preconditions.checkNotNull(searcherClient, "searcherClient need init");
        try {
            SearchResult result = searcherClient.execute(paramsBuilder);
            return JSON.parseObject(result.getResult(), OpenSearchReturnModel.class);
        } catch (Exception e) {
            log.error("OpenSearchWrapper search error", e);
            return null;
        }
    }

    public OpenSearchReturnModel getMockData() {
        try {
            String text = FileUtils.readFileToString(
                new File(this.getClass().getClassLoader().getResource("data/opensearch_mock_data").getPath()));
            return JSON.parseObject(text, OpenSearchReturnModel.class);
        } catch (Exception e) {
            log.error("getMockData error", e);
            return null;
        }
    }

    /**
     * 根据大模型返回结果生成查询对象
     */
    public SearchParamsBuilder queryGen(String answer) {
        if (StringUtils.isBlank(answer)) {
            return null;
        }
        OpenSearchQuery openSearchQuery = JSON.parseObject(formatJson(answer), OpenSearchQuery.class);
        if (openSearchQuery == null || (MapUtils.isEmpty(openSearchQuery.getQuery()) && MapUtils.isEmpty(
            openSearchQuery.getFilter()))) {
            return null;
        }

        Config config = new Config(Lists.newArrayList(openSearchConfig.getAppName()));
        config.setStart(0);
        // 默认、最多都是返回10条
        config.setHits(
            Math.min(ObjectUtils.firstNonNull(openSearchQuery.getLimit(), openSearchConfig.getDefaultReturnRecords()),
                openSearchConfig.getMaxReturnRecords()));
        config.setSearchFormat(SearchFormat.FULLJSON);
        config.setFetchFields(openSearchConfig.getFetchFields());
        SearchParams searchParams = new SearchParams(config);
        // query
        if (MapUtils.isNotEmpty(openSearchQuery.getQuery())) {
            searchParams.setQuery(concatQueryClause(openSearchQuery.getQuery()));
        }
        // sort
        if (CollectionUtils.isNotEmpty(openSearchQuery.getSort())) {
            List<SortField> sortFieldList = openSearchQuery.getSort().stream().map(OpenSearchWrapper::concatSortClause)
                .collect(Collectors.toList());
            Sort sort = new Sort();
            sort.setSortFields(sortFieldList);
            searchParams.setSort(sort);
        }
        SearchParamsBuilder paramsBuilder = SearchParamsBuilder.create(searchParams);
        // filter
        if (MapUtils.isNotEmpty(openSearchQuery.getFilter())) {
            paramsBuilder.addFilter(concatFilterClause(openSearchQuery.getFilter()));
        }
        return paramsBuilder;
    }

    private static SortField concatSortClause(List<String> sortPair) {
        return new SortField(sortPair.get(0),
            "asc".equalsIgnoreCase(sortPair.get(1)) ? Order.INCREASE : Order.DECREASE);
    }

    private static String concatFilterClause(Map<String, OpenSearchSegment> filterDic) {
        List<String> keys = new ArrayList<>(filterDic.keySet());
        List<String> multiPatternList = new ArrayList<>();
        for (String key : keys) {
            String op = filterDic.get(key).getOp();
            List<List<String>> results = filterDic.get(key).getResult();
            List<String> singlePatternList = new ArrayList<>();
            for (List<String> pair : results) {
                if ("lt".equals(pair.get(1))) {
                    singlePatternList.add(key + " < " + pair.get(0));
                } else if ("gt".equals(pair.get(1))) {
                    singlePatternList.add(key + " > " + pair.get(0));
                } else if ("eq".equals(pair.get(1))) {
                    singlePatternList.add(key + " = " + pair.get(0));
                } else if ("gte".equals(pair.get(1))) {
                    singlePatternList.add(key + " >= " + pair.get(0));
                } else if ("lte".equals(pair.get(1))) {
                    singlePatternList.add(key + " <= " + pair.get(0));
                } else {
                    singlePatternList.add(key + " != " + pair.get(0));
                }
            }
            String singlePatternStr = String.join(String.format(" %s ", op), singlePatternList);
            multiPatternList.add("(" + singlePatternStr + ")");
        }
        return String.join(" AND ", multiPatternList);
    }

    private static String concatQueryClause(Map<String, OpenSearchSegment> queryDic) {
        List<String> keys = new ArrayList<>(queryDic.keySet());
        List<String> multiPatternList = new ArrayList<>();
        List<String> multiPatternAndNotList = new ArrayList<>();

        for (String key : keys) {
            // 外部op，仅支持AND、OR，默认AND
            String op = queryDic.get(key).getOp();
            if (!StringUtils.equalsIgnoreCase("AND", op) && !StringUtils.equalsIgnoreCase("OR", op)) {
                op = "AND";
            }
            List<List<String>> result = queryDic.get(key).getResult();
            List<String> singlePatternList = new ArrayList<>();
            for (List<String> pair : result) {
                // 内部op，仅支持AND、ANDNOT，默认AND
                String innerOp = pair.get(1);
                if (!StringUtils.equalsIgnoreCase("AND", innerOp) && !StringUtils.equalsIgnoreCase("ANDNOT", innerOp)) {
                    innerOp = "AND";
                }
                String singlePattern = String.format("%s %s:'%s'", innerOp, key, pair.get(0));
                singlePatternList.add(singlePattern);
            }
            String singlePatternStr = String.join(String.format(" %s ", op), singlePatternList)
                .replace("AND ANDNOT", "ANDNOT");
            if (StringUtils.startsWith(singlePatternStr, "AND ")) {
                singlePatternStr = singlePatternStr.substring(4);
            }
            // ANDNOT要放在最后面
            if (!StringUtils.contains(singlePatternStr, "ANDNOT")) {
                multiPatternList.add("(" + singlePatternStr + ")");
            } else {
                multiPatternAndNotList.add(singlePatternStr);
            }
        }

        String result = String.join(" AND ", multiPatternList);
        if (CollectionUtils.isNotEmpty(multiPatternAndNotList)) {
            result = String.format("%s %s", result, String.join(" ", multiPatternAndNotList));
        }
        // 不支持仅做ANDNOT的情况
        if (result.startsWith("ANDNOT")) {
            return "";
        }
        return result;
    }

    private static String formatJson(String answer) {
        if (StringUtils.isBlank(answer)) {
            return "";
        }
        int start = answer.indexOf("{");
        int end = answer.lastIndexOf("}");
        return answer.substring(start, end + 1);
    }
}

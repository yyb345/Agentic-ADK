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
package com.alibaba.langengine.tool.tavily;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tool.utils.PostUtils;

import lombok.Data;

/**
 * @author aihe.ah
 * @time 2024/2/28
 * 功能说明：
 * https://app.tavily.com/
 */
@Data
public class TavilySearchTool extends DefaultTool {

    private static final String API_URL = "https://api.tavily.com/search";

    /**
     * 设置API Key和请求的一些信息
     */
    private TavilySearchParameters searchParameters;

    public TavilySearchTool() {
        setName("tavily_search");
        setDescription(
            "Tavily Search 是一个为语言模型代理（LLM Agents）特别定制的强大搜索API。当需要进行内容的搜索时可以考虑使用当前的工具");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        // 认为toolInput就是一个要搜索的内容
        searchParameters.setQuery(toolInput);

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(searchParameters));

        String result = PostUtils.doPostWithJson(API_URL, jsonObject);
        TavilySearchResult tavilySearchResult = JSON.parseObject(result, TavilySearchResult.class);

        // 如果结果成功了，取第一个分数最高的结果
        String answer = tavilySearchResult.getAnswer();
        if (tavilySearchResult.getResults() != null && tavilySearchResult.getResults().size() > 0) {
            answer = tavilySearchResult.getResults().get(0).getContent();
        }

        ToolExecuteResult toolExecuteResult = new ToolExecuteResult(answer, false);
        return toolExecuteResult;
    }
}

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
package com.alibaba.langengine.demo.agent.tool;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tool.bing.service.BingService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

import static com.alibaba.langengine.tool.ToolConfiguration.BING_API_KEY;
import static com.alibaba.langengine.tool.ToolConfiguration.BING_SERVER_URL;

/**
 * Bing Web Search工具
 * 参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-web-search/quickstarts/rest/java
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class MyWebSearchAPITool extends DefaultTool {

    private BingService service;

    private String token = BING_API_KEY;

    private String textFormat = "HTML";

    private Boolean textDecorations = true;

    private Integer count = 1;

    public MyWebSearchAPITool() {
        setName("BingWebSearchAPI");
        setDescription("A search engine. Useful for when you need to answer questions about current events. Input should be a search query.");

        Map<String, Object> queryMap = new TreeMap<>();
        queryMap.put("title", "Query");
        queryMap.put("type", "string");
        getArgs().put("query", queryMap);

        String serverUrl = BING_SERVER_URL;
        service = new BingService(serverUrl, Duration.ofSeconds(100L), true, token, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("MyWebSearchAPITool toolInput:" + toolInput);
        return new ToolExecuteResult("明天杭州温度20摄氏度");
    }
}

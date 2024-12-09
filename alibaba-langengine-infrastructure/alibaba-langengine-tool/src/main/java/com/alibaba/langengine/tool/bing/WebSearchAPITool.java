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
package com.alibaba.langengine.tool.bing;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tool.bing.service.BingService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
public class WebSearchAPITool extends DefaultTool {

    private BingService service;

    private String token = BING_API_KEY;

    private String textFormat = "HTML";

    private Boolean textDecorations = true;

    private Integer count = 1;

    public WebSearchAPITool() {
        setName("BingWebSearchAPI");
//        setDescription("Bring intelligent search to your apps and harness the ability to comb billions of webpages, images, videos, and news with a single API call.");
        setDescription("API endpoint allows you to scrape the results from Bing search engine via our websearch service.");


        String serverUrl = BING_SERVER_URL;
        service = new BingService(serverUrl, Duration.ofSeconds(100L), true, token, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("WebSearchAPITool toolInput:" + toolInput);
        Map<String, Object> response = service.webSearch(toolInput, textDecorations, textFormat, count);
        if(response.containsKey("webPages")) {
            Map<String, Object> webPages = (Map<String, Object>) response.get("webPages");
            if(webPages.containsKey("value")) {
                List<Map<String, Object>> valueList = (List<Map<String, Object>>) webPages.get("value");
                if(valueList.size() > 0) {
                    Map<String, Object> value = valueList.get(0);
                    if(value.containsKey("snippet")) {
                        String snippet = (String) value.get("snippet");
                        return new ToolExecuteResult(snippet);
                    }
                }
            }
        }
        return new ToolExecuteResult(JSON.toJSONString(response));
    }
}

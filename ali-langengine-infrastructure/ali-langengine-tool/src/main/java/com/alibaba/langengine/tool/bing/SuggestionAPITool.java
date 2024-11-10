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
import java.util.Map;

import static com.alibaba.langengine.tool.ToolConfiguration.BING_API_KEY;
import static com.alibaba.langengine.tool.ToolConfiguration.BING_SERVER_URL;

/**
 * Bing AutoSuggestion工具
 * 参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-autosuggest/quickstarts/rest/python
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class SuggestionAPITool extends DefaultTool {

    private BingService service;

    private String token = BING_API_KEY;

    private String mkt = "en-US";

    public SuggestionAPITool() {
        setName("BingAutosuggestAPI");
        setDescription("Empower users to type less and do more with automated and complete search suggestions.");

        String serverUrl = BING_SERVER_URL;
        service = new BingService(serverUrl, Duration.ofSeconds(100L), true, token, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("SuggestionAPITool toolInput:" + toolInput);
        Map<String, Object> response = service.suggestion(toolInput, mkt);
        return new ToolExecuteResult(JSON.toJSONString(response));
    }
}

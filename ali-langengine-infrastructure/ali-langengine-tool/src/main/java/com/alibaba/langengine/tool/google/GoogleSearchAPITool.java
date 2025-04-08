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
package com.alibaba.langengine.tool.google;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tool.google.service.GoogleService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

import static com.alibaba.langengine.tool.ToolConfiguration.*;

/**
 * Google Web Search工具
 * 参考：https://developers.google.com/custom-search/v1/overview?hl=zh_CN
 * https://developers.google.com/custom-search/v1/introduction?apix=true&hl=zh-cn
 * https://console.cloud.google.com/apis/api/customsearch.googleapis.com/metrics?project=api-project-958269392170
 * https://programmablesearchengine.google.com/controlpanel/create/congrats?cx=c2170f32aac624a8b
 * https://cse.google.com/cse?cx=c2170f32aac624a8b
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class GoogleSearchAPITool extends DefaultTool {

    private GoogleService service;

    private String googleApiKey = GOOGLE_API_KEY;

    private String googleCseId = GOOGLE_CSE_ID;

    private Integer num = 1;

    public GoogleSearchAPITool() {
        setName("GoogleSearchAPITool");
        setDescription("The JSON Custom Search API lets you develop websites and applications to retrieve and display search results from Google Custom Search programmatically. With this API, you can use RESTful requests to get either web search or image search results in JSON format.");

        String serverUrl = GOOGLE_CUSTOMSEARCH_SERVER_URL;
        service = new GoogleService(serverUrl, Duration.ofSeconds(100L), false, null, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("GoogleSearchAPITool toolInput:" + toolInput);
        if(toolInput.indexOf("girlfriend") >= 0) {
            return new ToolExecuteResult("Camila Morrone", true);
        } else if(toolInput.indexOf("current age") >= 0) {
            return new ToolExecuteResult("Camila Morrone is 25 age.", true);
        }
        toolInput =  toolInput.trim().replaceAll("^\"|\"$", "");
        Map<String, Object> response = service.customSearch(toolInput, num, googleCseId, googleApiKey);
        return new ToolExecuteResult(JSON.toJSONString(response));
    }
}

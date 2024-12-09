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
 * Bing Video Search工具
 * 参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-video-search/quickstarts/rest/python
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class VideoSearchAPITool extends DefaultTool {

    private BingService service;

    private String token = BING_API_KEY;

    private String pricing = "free";

    private String videoLength = "short";

    private Integer count = 1;

    public VideoSearchAPITool() {
        setName("BingVideoSearchAPI");
        setDescription("Add advanced video search features to apps and websites, with previews, trends, and rich metadata.");

        String serverUrl = BING_SERVER_URL;
        service = new BingService(serverUrl, Duration.ofSeconds(100L), true, token, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("VideoSearchAPITool toolInput:" + toolInput);
        Map<String, Object> response = service.videoSearch(toolInput, pricing, videoLength, count);
        return new ToolExecuteResult(JSON.toJSONString(response));
    }
}

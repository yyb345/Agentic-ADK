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

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tool.google.service.SerpapiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.tool.ToolConfiguration.*;

/**
 * serpapi
 * 参考：https://serpapi.com/search-api
 * https://serpapi.com/manage-api-key
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class SerpapiTool extends DefaultTool {

    private SerpapiService service;

    private String serpapiKey = SERPAPI_KEY;

    private Integer start = 0;

    private Integer num = 2;

    public SerpapiTool() {
        setName("SerpapiTool");
        setDescription("API endpoint allows you to scrape the results from Google search engine via our SerpApi service.");

        String serverUrl = SERPAPI_SERVER_URL;
        service = new SerpapiService(serverUrl, Duration.ofSeconds(100L), false, null, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("SerpapiTool toolInput:" + toolInput);
        toolInput =  toolInput.trim().replaceAll("^\"|\"$", "");
        Map<String, Object> response = service.search(toolInput, start, num, serpapiKey);

        if (response.containsKey("error")) {
            throw new RuntimeException("Got error from SerpAPI: " + response.get("error"));
        }
        if (response.containsKey("answer_box") && response.get("answer_box") instanceof List) {
            response.put("answer_box", ((List) response.get("answer_box")).get(0));
        }

        String toret = "";
        if (response.containsKey("answer_box") && ((Map<String, Object>) response.get("answer_box")).containsKey("answer")) {
            toret = ((Map<String, Object>) response.get("answer_box")).get("answer").toString();
        } else if (response.containsKey("answer_box") && ((Map<String, Object>) response.get("answer_box")).containsKey("snippet")) {
            toret = ((Map<String, Object>) response.get("answer_box")).get("snippet").toString();
        } else if (response.containsKey("answer_box")
                && ((Map<String, Object>) response.get("answer_box")).containsKey("snippet_highlighted_words")
        ) {
            toret = ((List<String>) ((Map<String, Object>) response.get("answer_box")).get("snippet_highlighted_words")).get(0);
        } else if (response.containsKey("sports_results") && ((Map<String, Object>) response.get("sports_results")).containsKey("game_spotlight")) {
            toret = ((Map<String, Object>) response.get("sports_results")).get("game_spotlight").toString();
        } else if (response.containsKey("shopping_results") && ((List<Map<String, Object>>) response.get("shopping_results")).get(0).containsKey("title")) {
            List<Map<String, Object>> shoppingResults = (List<Map<String, Object>>) response.get("shopping_results");
            List<Map<String, Object>> subList = shoppingResults.subList(0, 3);
            toret = subList.toString();
        } else if (response.containsKey("knowledge_graph") && ((Map<String, Object>) response.get("knowledge_graph")).containsKey("description")) {
            toret = ((Map<String, Object>) response.get("knowledge_graph")).get("description").toString();
        } else if ((((List<Map<String, Object>>) response.get("organic_results")).get(0)).containsKey("snippet")) {
            toret = (((List<Map<String, Object>>) response.get("organic_results")).get(0)).get("snippet").toString();
        } else if ((((List<Map<String, Object>>) response.get("organic_results")).get(0)).containsKey("link")) {
            toret = (((List<Map<String, Object>>) response.get("organic_results")).get(0)).get("link").toString();
        } else if (response.containsKey("images_results") && ((Map<String, Object>) ((List<Map<String, Object>>) response.get("images_results")).get(0)).containsKey("thumbnail")) {
            List<String> thumbnails = new ArrayList<>();
            List<Map<String, Object>> imageResults = (List<Map<String, Object>>) response.get("images_results");
            for (Map<String, Object> item : imageResults.subList(0, 10)) {
                thumbnails.add(item.get("thumbnail").toString());
            }
            toret = thumbnails.toString();
        } else {
            toret = "No good search result found";
        }
        log.warn("SerpapiTool result:" + toret);
        return new ToolExecuteResult(toret);
    }
}

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

/**
 * @author aihe.ah
 * @time 2024/2/28
 * 功能说明：
 */

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Data;

import static com.alibaba.langengine.tool.ToolConfiguration.TAVILY_API_KEY;

/**
 * 搜索参数配置类
 *
 * @author aihe
 */
@Data
public class TavilySearchParameters {

    /**
     * 用户唯一的API密钥
     */
    @JSONField(name = "api_key")
    private String apiKey = TAVILY_API_KEY;

    /**
     * 搜索查询字符串
     */
    private String query;

    /**
     * 搜索深度，可以是basic或advanced，默认为basic，快速返回结果；advanced则深入且高质量但响应时间更长，相当于2次请求。
     * 可选字段，默认值为"basic"。
     */
    @JSONField(name = "search_depth")
    private String searchDepth = "basic";

    /**
     * 是否在响应中包含与查询相关的图片列表，默认为false。
     * 可选字段，默认值为false。
     */
    @JSONField(name = "include_images")
    private Boolean includeImages = false;

    /**
     * 是否在搜索结果中包含答案，默认为false。
     * 可选字段，默认值为false。
     */
    @JSONField(name = "include_answer")
    private Boolean includeAnswer = false;

    /**
     * 是否在搜索结果中包含原始内容，默认为false。
     * 可选字段，默认值为false。
     */
    @JSONField(name = "include_raw_content")
    private Boolean includeRawContent = false;

    /**
     * 返回的最大搜索结果数，默认为5。
     * 可选字段，默认值为5。
     */
    @JSONField(name = "max_results")
    private Integer maxResults = 5;

    /**
     * 一个明确包含在搜索结果中的域名列表，默认值为None，包括所有域名。
     * 可选字段，默认值为null。
     */
    @JSONField(name = "include_domains")
    private List<String> includeDomains;

    /**
     * 一个明确从搜索结果中排除的域名列表，默认值为None，即不排除任何域名。
     * 可选字段，默认值为null。
     */
    @JSONField(name = "exclude_domains")
    private List<String> excludeDomains;

}
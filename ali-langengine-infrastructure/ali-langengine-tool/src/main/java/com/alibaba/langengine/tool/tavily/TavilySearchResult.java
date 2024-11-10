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

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author aihe.ah
 * @time 2024/2/28
 * 功能说明：
 */
@Data
public class TavilySearchResult {

    /**
     * 对搜索查询的回答
     */
    private String answer;

    /**
     * 搜索查询
     */
    private String query;

    /**
     * 搜索结果响应时间
     */
    @JSONField(name = "response_time")
    private String responseTime;

    /**
     * 与查询相关的图片URL列表
     */
    private List<String> images;

    /**
     * 与原始查询相关的建议后续研究问题列表
     */
    @JSONField(name = "follow_up_questions")
    private List<String> followUpQuestions;

    /**
     * 按相关性排名的搜索结果列表
     */
    private List<Result> results;

    /**
     * 搜索结果
     */
    @Data
    public static class Result {

        /**
         * 搜索结果URL的标题
         */
        private String title;

        /**
         * 搜索结果的URL
         */
        private String url;

        /**
         * 从被抓取URL中提取的与查询最相关的内容，我们使用专有的AI和算法从每个URL中只提取最相关的内容，以优化上下文质量和大小。
         */
        private String content;

        /**
         * 网站的解析和清理过的HTML内容。目前仅包括解析过的文本。
         */
        @JSONField(name = "raw_content")
        private String rawContent;

        /**
         * 搜索结果的相关性评分
         */
        private String score;
    }
}

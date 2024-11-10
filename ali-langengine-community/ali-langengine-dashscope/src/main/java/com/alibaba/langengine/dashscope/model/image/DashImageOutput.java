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
package com.alibaba.langengine.dashscope.model.image;

import lombok.Data;

import java.util.List;


/**
 * @author chenshuaixin
 * @date 2024/05/17
 */
@Data
public class DashImageOutput {
    /**
     * 任务id
     */
    private String task_id;
    /**
     * 任务状态
     * PENDING：排队中
     *
     * RUNNING：处理中
     *
     * SUCCEEDED：成功
     *
     * FAILED：失败
     *
     * UNKNOWN：作业不存在或状态未知
     */
    private String task_status;
    /**
     * 作业中每个batch任务的状态：
     *
     * TOTAL：总batch数目
     *
     * SUCCEEDED：已经成功的batch数目
     *
     * FAILED：已经失败的batch数目
     */
   // private Map<String,Integer> task_metrics;
    /**
     * 生成图片的url
     */
    private List<DashImageUrlResult> results;
    /**
     * 提交时间
     */
    private String submit_time;
    /**
     * 结束时间
     */
    private String end_time;


}

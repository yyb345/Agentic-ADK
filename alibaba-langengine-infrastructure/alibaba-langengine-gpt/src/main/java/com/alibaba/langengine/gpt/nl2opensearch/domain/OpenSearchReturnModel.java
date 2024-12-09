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
package com.alibaba.langengine.gpt.nl2opensearch.domain;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询OpenSearch的返回对象
 *
 * @author pingkuang 2023/11/20
 */
@Data
@NoArgsConstructor
public class OpenSearchReturnModel {
    private String status;
    private Result result;
    private List<Error> errors;
    private String tracer;
    private String request_id;

    public Boolean isSuccess() {
        return "OK".equals(status);
    }

    @Data
    public static class Error {
        private Integer code;
        private String message;
    }

    @Data
    public static class Result {
        private Double searchtime;
        private Long total;
        private Integer num;
        private Integer viewtotal;
        private List<Item> items;
    }

    @Data
    public static class Item {
        private Map<String, Object> fields;
        private List<Double> sortExprValues;
    }
}
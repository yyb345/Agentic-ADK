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
package com.alibaba.langengine.agentframework.model.service;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultDO<T> implements Serializable {
    private boolean success;
    private T data;
    private Integer code;
    private String message;
    private String detailMessage;
    private String traceId;

    public ResultDO() {
        this.success = false;
        this.data = null;
        this.code = null;
        this.message = null;
        this.detailMessage = null;
        this.traceId = null;
    }

    public ResultDO(boolean success, T data, Integer code, String message) {
        this(success, data, code, message, null);
    }

    public ResultDO(boolean success, T data, Integer code, String message, String detailMessage) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;
//        this.traceId = EagleEye.getTraceId();
    }

    public static <T> ResultDO<T> success() {
        return new ResultDO<T>(true, null, null, null);
    }

    public static <T> ResultDO<T> success(T data) {
        return new ResultDO<T>(true, data, null, null);
    }

    public static <T> ResultDO<T> fail(Integer code, String message){
        return new ResultDO<>(false, null, code, message);
    }

    public static <T> ResultDO<T> fail(String message){
        return new ResultDO<>(false, null, null, message);
    }

    public boolean isSuccess() {
        return this.success;
    }
}

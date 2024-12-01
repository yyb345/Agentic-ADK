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
package com.alibaba.langengine.agentframework.model.domain;

import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * AgentAPIResult
 *
 * @param <T>
 *
 * @author xiaoxuan.lp 
 */
@Data
public class AgentAPIResult<T> implements Serializable {
    private boolean success;
    private T data;
    private Integer code;
    private String message;
    private String subCode;
    private String subMessage;
    private String subErrorDetail;
    private String requestId;

    public AgentAPIResult(boolean success, T data, Integer code, String message, String subCode, String subMessage, String subErrorDetail, String requestId) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
        this.subCode = subCode;
        this.subMessage = subMessage;
        this.subErrorDetail = subErrorDetail;
        this.requestId = requestId;
    }

    public static <T> AgentAPIResult<T> success() {
        return new AgentAPIResult<T>(true, null, null, null, null, null, null, null);
    }

    public static <T> AgentAPIResult<T> success(T data) {
        return new AgentAPIResult<T>(true, data, null, null, null, null, null, null);
    }

    public static <T> AgentAPIResult<T> success(T data, String requestId) {
        return new AgentAPIResult<T>(true, data, null, null, null, null, null, requestId);
    }

    public static <T> AgentAPIResult<T> fail(Integer code, AgentMagicErrorCode agentMagicErrorCode, String subMessage, String requestId){
        return new AgentAPIResult<>(false, null, code, agentMagicErrorCode.getCode(), agentMagicErrorCode.getMessage(), subMessage, null, requestId);
    }

    public static <T> AgentAPIResult<T> fail(Integer code, String message, String subCode, String subMessage, String requestId){
        return new AgentAPIResult<>(false, null, code, message, subCode, subMessage, null, requestId);
    }

    public static <T> AgentAPIResult<T> fail(Integer code, String message, String subCode, String subMessage, String subErrorDetail, String requestId){
        return new AgentAPIResult<>(false, null, code, message, subCode, subMessage, subErrorDetail, requestId);
    }

    public boolean isSuccess() {
        return this.success;
    }
}

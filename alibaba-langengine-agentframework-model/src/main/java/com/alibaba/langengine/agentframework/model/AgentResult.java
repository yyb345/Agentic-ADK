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
package com.alibaba.langengine.agentframework.model;

import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.exception.ExceptionUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * Agent统一返回体
 *
 * @author xiaoxuan.lp
 *
 * @param <T>
 */
@Data
public class AgentResult<T> implements Serializable {
    private boolean success;
    private T data;
    private String errorCode;
    private String errorMsg;
    private String errorDetail;
    private String requestId;
    private String processInstanceId;

    public AgentResult() {
        this(false, null, null, null);
    }

    public AgentResult(boolean success, T data, String errorCode, String errorMsg) {
        this(success, data, errorCode, errorMsg, null, null);
    }

    public AgentResult(boolean success, T data, String errorCode, String errorMsg, String errorDetail, String requestId) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.errorDetail = errorDetail;
        this.requestId = requestId;
    }

    public AgentResult(boolean success, T data, String errorCode, String errorMsg, String errorDetail, String requestId, String processInstanceId) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.errorDetail = errorDetail;
        this.requestId = requestId;
        this.processInstanceId = processInstanceId;
    }

    public static <T> AgentResult<T> success() {
        return new AgentResult<T>(true, null, null, null);
    }

    public static <T> AgentResult<T> success(T data) {
        return new AgentResult<T>(true, data, null, null);
    }

    public static <T> AgentResult<T> success(String processInstanceId, T data) {
        AgentResult agentResult = new AgentResult<T>(true, data, null, null);
        agentResult.setProcessInstanceId(processInstanceId);
        return agentResult;
    }

    public static <T> AgentResult<T> success(T data, String requestId) {
        return new AgentResult<T>(true, data, null, null, null, requestId);
    }

    public static <T> AgentResult<T> fail(String errorCode, String errorMsg, String errorDetail) {
        return new AgentResult<T>(false,
                null,
                errorCode,
                errorMsg,
                errorDetail, null);
    }

    public static <T> AgentResult<T> fail(String errorCode, String errorMsg, String errorDetail, String requestId) {
        return new AgentResult<T>(false,
                null,
                errorCode,
                errorMsg,
                errorDetail,
                requestId);
    }

    public static <T> AgentResult<T> fail(String errorCode, String errorMsg, String errorDetail, String requestId, String processIsntanceId) {
        AgentResult agentResult = new AgentResult<T>(false,
                null,
                errorCode,
                errorMsg,
                errorDetail,
                requestId);
        agentResult.setProcessInstanceId(processIsntanceId);
        return agentResult;
    }

    public static <T> AgentResult<T> fail(AgentMagicErrorCode agentPaasErrorCode, String errorDetail) {
        return new AgentResult<T>(false,
                null,
                agentPaasErrorCode.getCode(),
                agentPaasErrorCode.getMessage(),
                errorDetail, null);
    }

    public static <T> AgentResult<T> fail(AgentMagicErrorCode agentPaasErrorCode, String errorDetail, String requestId) {
        return new AgentResult<T>(false,
                null,
                agentPaasErrorCode.getCode(),
                agentPaasErrorCode.getMessage(),
                errorDetail, requestId);
    }

    public static <T> AgentResult<T> fail(AgentMagicErrorCode agentPaasErrorCode, String errorDetail, String requestId, String processInstanceId) {
        AgentResult agentResult = new AgentResult<T>(false,
                null,
                agentPaasErrorCode.getCode(),
                agentPaasErrorCode.getMessage(),
                errorDetail, requestId);
        agentResult.setProcessInstanceId(processInstanceId);
        return agentResult;
    }

    public static <T> AgentResult<T> fail(AgentMagicErrorCode agentPaasErrorCode, Throwable throwable) {
        return new AgentResult<T>(false,
                null,
                agentPaasErrorCode.getCode(),
                agentPaasErrorCode.getMessage(),
                throwable != null ? ExceptionUtils.getStackTraceDetail(throwable) : null, null);
    }

    public static <T> AgentResult<T> fail(AgentMagicErrorCode agentPaasErrorCode, Throwable throwable, String requestId) {
        return new AgentResult<T>(false,
                null,
                agentPaasErrorCode.getCode(),
                agentPaasErrorCode.getMessage(),
                throwable != null ? ExceptionUtils.getStackTraceDetail(throwable) : null, requestId);
    }

    public static <T> AgentResult<T> fail(AgentMagicErrorCode agentPaasErrorCode, Throwable throwable, String requestId, String processInstanceId) {
        AgentResult agentResult = new AgentResult<T>(false,
                null,
                agentPaasErrorCode.getCode(),
                agentPaasErrorCode.getMessage(),
                throwable != null ? ExceptionUtils.getStackTraceDetail(throwable) : null, requestId);
        agentResult.setProcessInstanceId(processInstanceId);
        return agentResult;
    }

    public static <T> AgentResult<T> fail(AgentMagicException agentPaasException) {
        return new AgentResult<T>(false,
                null,
                agentPaasException.getErrorCode(),
                agentPaasException.getErrorMessage(),
                agentPaasException.getErrorDetail() != null ? agentPaasException.getErrorDetail() : ExceptionUtils.getStackTraceDetail(agentPaasException), null);
    }

    public static <T> AgentResult<T> fail(AgentMagicException agentPaasException, String requestId) {
        return new AgentResult<T>(false,
                null,
                agentPaasException.getErrorCode(),
                agentPaasException.getErrorMessage(),
                agentPaasException.getErrorDetail() != null ? agentPaasException.getErrorDetail() : ExceptionUtils.getStackTraceDetail(agentPaasException), requestId);
    }

    public static <T> AgentResult<T> fail(AgentMagicException agentPaasException, String requestId, String processInstanceId) {
        AgentResult agentResult = new AgentResult<T>(false,
                null,
                agentPaasException.getErrorCode(),
                agentPaasException.getErrorMessage(),
                agentPaasException.getErrorDetail() != null ? agentPaasException.getErrorDetail() : ExceptionUtils.getStackTraceDetail(agentPaasException), requestId);
        agentResult.setProcessInstanceId(processInstanceId);
        return agentResult;
    }

    public static String getAgentResultError(AgentResult agentResult) {
        return "errorCode:" + agentResult.getErrorCode() + ",errorMsg:" + agentResult.getErrorMsg() + ",errorDetail:" + agentResult.getErrorDetail() + ",requestId:" + agentResult.getRequestId();
    }

    public boolean isSuccess() {
        return this.success;
    }
}

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
package com.alibaba.langengine.agentframework.model.exception;

import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import lombok.Data;

/**
 * AgentMagic Exception
 *
 * @author xiaoxuan.lp
 */
@Data
public class AgentMagicException extends RuntimeException {

    /**
     * 错误码code
     */
    private String errorCode;

    /**
     * 错误message
     */
    private String errorMessage;

    /**
     * 错误详细信息
     */
    private String errorDetail;

    /**
     * 请求id
     */
    private String requestId;

    public AgentMagicException(AgentMagicErrorCode agentPaasErrorCode, String errorDetail, String requestId) {
        super(agentPaasErrorCode.getMessage());
        this.errorCode = agentPaasErrorCode.getCode();
        this.errorMessage = agentPaasErrorCode.getMessage();
        this.errorDetail = errorDetail;
        this.requestId = requestId;
    }

    public AgentMagicException(AgentMagicErrorCode agentPaasErrorCode, Throwable cause, String requestId) {
        super(agentPaasErrorCode.getMessage(), cause);
        this.errorCode = agentPaasErrorCode.getCode();
        this.errorMessage = agentPaasErrorCode.getMessage();
        this.errorDetail = ExceptionUtils.getStackTraceDetail(cause);
        this.requestId = requestId;
    }

    public AgentMagicException(String errorCode, String errorMessage, String errorDetail, String requestId) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.requestId = requestId;
    }
}

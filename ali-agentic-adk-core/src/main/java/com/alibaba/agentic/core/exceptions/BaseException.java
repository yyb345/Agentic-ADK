/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.exceptions;

import lombok.Getter;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 14:07
 */
@Getter
public class BaseException extends RuntimeException {

    private final ErrorEnum errorEnum;

    public BaseException(String message, Throwable cause, ErrorEnum errorEnum) {
        super(message, cause);
        this.errorEnum = errorEnum;
    }

    public BaseException(String message, ErrorEnum errorEnum) {
        super(message);
        this.errorEnum = errorEnum;
    }

    public BaseException(Throwable cause, ErrorEnum errorEnum) {
        super(cause);
        this.errorEnum = errorEnum;
    }

}

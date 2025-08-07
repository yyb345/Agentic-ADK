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
 * @date 2025/7/8 14:10
 */
@Getter
public enum ErrorEnum {

    SYSTEM_ERROR("500", "system error", false),

    FLOW_CONFIG_ERROR("600", "flow configuration error", false),

    PROPERTY_CONFIG_ERROR("601", "property configuration error", false);

    private final String code;

    private final String message;

    private final Boolean retry;

    ErrorEnum(String code, String message, Boolean retry) {
        this.code = code;
        this.message = message;
        this.retry = retry;
    }
}

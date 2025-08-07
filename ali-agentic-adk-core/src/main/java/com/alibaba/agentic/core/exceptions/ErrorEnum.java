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

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

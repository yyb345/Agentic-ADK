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
package com.alibaba.langengine.agentframework.model.enums;

public enum OperatorEnum {
    /**
     * 为空
     */
    IS_NULL("is_null","isNull(%s)","isNull",new Class[]{Object.class}),
    /**
     * 不为空
     */
    IS_NOT_NULL("is_not_null","isNotNull(%s)","isNotNull",new Class[]{Object.class}),
    /**
     * 等于/是
     */
    EQUAL("equal","equal(%s,%s)","equal",new Class[]{Object.class,Object.class}),
    /**
     * 不等于/不是
     */
    NOT_EQUAL("not_equal","notEqual(%s,%s)","notEqual",new Class[]{Object.class,Object.class}),
    /**
     * 大于
     */
    GREATER_THAN("greater_than","greaterThan(%s,%s)","greaterThan",new Class[]{Object.class,Object.class}),
    /**
     * 大于等于
     */
    GREATER_THAN_OR_EQUAL("greater_than_or_equal","greaterThanOrEqual(%s,%s)","greaterThanOrEqual",new Class[]{Object.class,Object.class}),
    /**
     * 小于
     */
    LESS_THAN("less_than","lessThan(%s,%s)","lessThan",new Class[]{Object.class,Object.class}),
    /**
     * 小于等于
     */
    LESS_THAN_OR_EQUAL("less_than_or_equal","lessThanOrEqual(%s,%s)","lessThanOrEqual",new Class[]{Object.class,Object.class}),
    /**
     * 在···中
     */
    IN("in","in(%s,%s)","in",new Class[]{Object.class,Object.class}),
    /**
     * 不在···中
     */
    NOT_IN("not_in","notIn(%s,%s)","notIn",new Class[]{Object.class,Object.class}),

    /**
     * 包含
     */
    CONTAIN("contain","contain(%s,%s)","contain",new Class[]{Object.class,Object.class}),

    /**
     * 不包含
     */
    NOT_CONTAIN("not_contain","notContain(%s,%s)","notContain",new Class[]{Object.class,Object.class}),

    /**
     * urlEncode
     */
    URL_ENCODE("url_encode","urlEncode(%s)","urlEncode",new Class[]{String.class}),

    /**
     * min
     */
    MIN("min","min(%s)","min",new Class[]{Object.class}),

    /**
     * max
     */
    MAX("max","max(%s)","max",new Class[]{Object.class}),

    /**
     * dateCompute
     */
    DATE_COMPUTE("date_compute","dateCompute(%s,%s,%s,%s)","dateCompute",new Class[]{String.class,Integer.class,String.class,String.class}),
    ;


    private String code;
    private String expr;
    private String funcName;
    private Class[] paramType;


    OperatorEnum(String code, String expr, String funcName, Class[] paramType) {
        this.code = code;
        this.expr = expr;
        this.funcName = funcName;
        this.paramType = paramType;
    }

    public static OperatorEnum getByCode(String code) {
        if(code == null || "".equals(code)) {
            return null;
        }
        for (OperatorEnum item : values()) {
            if(item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getExpr() {
        return expr;
    }

    public String getFuncName() {
        return funcName;
    }

    public Class[] getParamType() {
        return paramType;
    }
}

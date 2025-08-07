package com.alibaba.agentic.core.engine.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/5 18:00
 */
@Component
public class PropertyConstant {

    public static String dashscopeApiKey;

    public static final String DEFAULT = "default";

    public static final String SYMBOL_KEY = "symbol";

    public static final String SYMBOL_VALUE_CONDITION_DEFAULT_FLOW = "conditionDefaultFlow";

    @Value(value = "${ali.agentic.adk.flownode.dashscope.apiKey:**}")
    public void setDashscopeApiKey(String dashscopeApiKey) {
        PropertyConstant.dashscopeApiKey = dashscopeApiKey;
    }
}

package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/16 15:38
 */
@Data
@Accessors(chain = true)
public class FunctionCallRequest {

    private String toolName;

    private Map<String, Object> toolParameter;

}

package com.alibaba.agentic.core.engine.delegation.domain;

import com.google.adk.agents.InvocationContext;
import lombok.Data;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/17 10:21
 */
@Data
public class AgentRequest {

    private InvocationContext invocationContext;
}

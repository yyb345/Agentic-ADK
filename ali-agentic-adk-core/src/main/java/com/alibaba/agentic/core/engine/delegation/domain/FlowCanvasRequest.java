package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;

import java.util.Map;

@Data
public class FlowCanvasRequest {

    private String flowDefinition;

    private String flowVersion;

    private Map<String, Object> request;

}

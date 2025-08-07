package com.alibaba.agentic.core.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowDefinition {

    private String definitionId;

    private String version;

    private String bpmnXml;

}

package com.alibaba.agentic.core.engine.node.sub;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ToolParam {

    private String name;

    private String value;

    private String description;


}

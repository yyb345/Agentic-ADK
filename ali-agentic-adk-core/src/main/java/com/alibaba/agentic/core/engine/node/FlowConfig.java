package com.alibaba.agentic.core.engine.node;

import lombok.Data;

import java.util.Map;

@Data
public class FlowConfig {

    //配置的全局变量
    private Map<String, Object> globalConfig;
    //用户的文本输入
    private String query;

}

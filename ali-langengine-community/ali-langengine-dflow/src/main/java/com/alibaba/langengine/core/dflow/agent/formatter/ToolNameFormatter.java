package com.alibaba.langengine.core.dflow.agent.formatter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.tool.BaseTool;

public  class ToolNameFormatter implements Function<Object, String> {

    public static String genToolsNames(List<BaseTool> tools) {
        return tools.stream().map(x -> x.getName()).collect(Collectors.joining(", "));
    }
    public String apply(Object tools) {
        return genToolsNames((List<BaseTool>)tools);
    }
}
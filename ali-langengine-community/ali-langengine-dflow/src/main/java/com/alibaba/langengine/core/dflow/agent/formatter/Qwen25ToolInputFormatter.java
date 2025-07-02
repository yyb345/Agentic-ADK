package com.alibaba.langengine.core.dflow.agent.formatter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;

public class Qwen25ToolInputFormatter implements Function<Object, String> {
    @Override
    public String apply(Object o) {
        if(o instanceof String){
            JSONArray tools = JSON.parseArray((String)o);
            return tools.stream().map(x->JSON.toJSONString(x)).collect(Collectors.joining("\n"));
        }else {
            List<BaseTool> tools = (List<BaseTool>)o;
            return tools.stream().map(
                    x -> {
                        String paramsDef = "";
                        if (x instanceof StructuredTool) {
                            paramsDef = ((StructuredTool)x).formatStructSchema();
                        } else {
                            paramsDef = x.getInputSchema();
                        }
                        HashMap<String, Object> functionDef = new HashMap<>();
                        functionDef.put("name", x.getName());
                        functionDef.put("description", x.getDescription());
                        functionDef.put("parameters", JSON.parse(paramsDef));
                        return JSON.toJSONString(functionDef);
                    }
                ).map(x -> {
                    //合并到一行
                    return Arrays.stream(x.split("\n"))
                        .map(y->y.trim())
                        .collect(Collectors.joining());
                })
                .collect(Collectors.joining("\n"));
        }

    }
}

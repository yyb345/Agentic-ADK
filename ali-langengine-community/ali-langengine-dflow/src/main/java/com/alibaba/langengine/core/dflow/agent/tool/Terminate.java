package com.alibaba.langengine.core.dflow.agent.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Terminate extends StructuredTool {

    private String PARAMETERS = "{\n" +
        "        \"type\": \"object\",\n" +
        "        \"properties\": {\n" +
        "            \"status\": {\n" +
        "                \"type\": \"string\",\n" +
        "                \"description\": \"The finish status of the interaction.\",\n" +
        "                \"enum\": [\"success\", \"failure\"],\n" +
        "            }\n" +
        "        },\n" +
        "        \"required\": [\"status\"],\n" +
        "    }";

    public Terminate() {
        setName("terminate");

        StructuredSchema schema = new StructuredSchema();
        List<StructuredParameter> params = new ArrayList<>();
        StructuredParameter p = new StructuredParameter();
        p.setName("status");
        p.setDescription("The finish status of the interaction.");
        Map<String, Object> schemap = new HashMap();
        schemap.put("type", "string");
        schemap.put("enum", new String[]{"success", "failure"});
        p.setSchema(schemap);
        params.add(p);

        StructuredParameter p2 = new StructuredParameter();
        p2.setName("conclusion");
        //conculation
        // 结论 英语
        p2.setDescription("The conclusion of the interaction.");
        Map<String, Object> schemap2 = new HashMap();
        schemap2.put("type", "string");
        p2.setSchema(schemap2);
        params.add(p2);

        schema.setParameters(params);
        this.setStructuredSchema(schema);
        //setDescription("Terminate the interaction if the assistant cannot proceed further with the task.");
        setDescription("Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.");
    }
    public String getInputSchema() {
        return PARAMETERS;
    }
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.warn("Terminate toolInput:" + toolInput);
        String result = String.format("The interaction has been completed with status: %s", toolInput);
        return new ToolExecuteResult(result);
    }

    @Override
    public ToolExecuteResult execute(String s) {
        return run(s, null);
    }
}
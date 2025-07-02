package com.alibaba.langengine.core.dflow.agent.tool;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.dflow.util.CopyContextDFlowHelper;
import com.alibaba.langengine.core.dflow.agent.DFlowBaseAgent;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

public class AgentTool extends StructuredTool implements DFlowTool {

    CopyContextDFlowHelper helper;
    DFlowBaseAgent agent;

    public AgentTool(DFlowBaseAgent agent) throws DFlowConstructionException {
        helper = new CopyContextDFlowHelper(agent.getName(),(ctx,p)->{
            return agent.run(p, ctx.get(DFlowBaseAgent.SESSIONID));
        });
        this.agent = agent;

    }

    @Override
    public ToolExecuteResult execute(String s) {
        return null;
    }

    @Override
    public DFlow<ToolExecuteResult> dflowRun(ContextStack ctx, String toolInput) throws DFlowConstructionException {
        return helper.run(ctx, toolInput)
            .map((c,s)->{
                ToolExecuteResult toolExecuteResult = new ToolExecuteResult();
                toolExecuteResult.setOutput(s);
                return toolExecuteResult;
            },ToolExecuteResult.class).id("DFlow_Agent_Tool_tool_result"+agent.getName());
    }
}

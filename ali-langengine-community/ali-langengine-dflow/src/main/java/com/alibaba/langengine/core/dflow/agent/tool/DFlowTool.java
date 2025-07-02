package com.alibaba.langengine.core.dflow.agent.tool;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

public interface DFlowTool {
    DFlow<ToolExecuteResult> dflowRun(ContextStack ctx, String toolInput) throws DFlowConstructionException;
}

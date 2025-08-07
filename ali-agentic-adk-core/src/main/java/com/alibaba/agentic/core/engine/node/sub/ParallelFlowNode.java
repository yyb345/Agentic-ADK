package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.node.FlowNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.concurrent.ExecutorService;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParallelFlowNode extends FlowNode {

    //并行条件下的所有分支node。注意，触发并行节点的前提是next节点为空
    private List<FlowNode> parallelNodeList;
    //自定义的并发节点执行器
    private ExecutorService executorService;

    @Override
    protected String getNodeType() {
        return NodeType.PARALLEL;
    }

    @Override
    protected String getDelegationClassName() {
        return null;
    }
}

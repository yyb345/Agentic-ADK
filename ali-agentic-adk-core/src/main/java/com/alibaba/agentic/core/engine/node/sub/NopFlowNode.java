package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.delegation.DelegationNop;
import com.alibaba.agentic.core.engine.node.FlowNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

// 空操作节点
@EqualsAndHashCode(callSuper = true)
@Data
public class NopFlowNode extends FlowNode {

    public NopFlowNode() {
        super();
        setName(NodeType.NOP);
    }

    @Override
    protected String getNodeType() {
        return NodeType.NOP;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationNop.class.getName();
    }
}

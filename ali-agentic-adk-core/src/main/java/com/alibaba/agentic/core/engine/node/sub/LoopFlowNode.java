package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoopFlowNode extends FlowNode {

    //内部循环执行的runner
    private FlowCanvas innerCanvas;

    public FlowCanvas getInnerRunner() {
        return innerCanvas;
    }

    public void setInnerRunner(FlowCanvas innerCanvas) {
        this.innerCanvas = innerCanvas;
    }

    @Override
    protected String getNodeType() {
        return null;
    }

    @Override
    protected String getDelegationClassName() {
        return null;
    }
}

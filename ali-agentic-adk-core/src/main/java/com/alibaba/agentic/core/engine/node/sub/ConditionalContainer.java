package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.behavior.BaseCondition;
import com.alibaba.agentic.core.engine.node.FlowNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public abstract class ConditionalContainer implements BaseCondition {

    // 如果选择分支条件成立，则flowNode字段则为接下来会执行的节点
    // 如需嵌套使用选择分支，请将以下字段设置为NopFlowNode类型节点，再在NopFlowNode类型节点的conditionalFancyNodeList字段中设置新的条件节点
    protected FlowNode flowNode;

}

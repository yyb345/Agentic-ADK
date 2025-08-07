/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

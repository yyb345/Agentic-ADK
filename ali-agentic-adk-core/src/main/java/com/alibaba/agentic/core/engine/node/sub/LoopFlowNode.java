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

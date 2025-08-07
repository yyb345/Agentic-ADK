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

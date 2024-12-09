/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.agentframework.behavior;

import com.alibaba.langengine.agentframework.delegation.provider.AgentTraceHelper;
import com.alibaba.langengine.agentframework.config.extension.AgentExpressionUtil;
import com.alibaba.langengine.agentframework.domain.ProcessExecuteLog;
import com.alibaba.smart.framework.engine.behavior.TransitionBehavior;
import com.alibaba.smart.framework.engine.behavior.base.AbstractTransitionBehavior;
import com.alibaba.smart.framework.engine.bpmn.assembly.process.SequenceFlow;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.model.assembly.ConditionExpression;
import com.alibaba.smart.framework.engine.model.assembly.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * 重写AbstractTransitionBehavior
 *
 * @author xiaoxuan.lp
 */
@ExtensionBinding(group = ExtensionConstant.ACTIVITY_BEHAVIOR, bindKey = TransitionBehavior.class, priority = 1)
public class AgentSequenceFlowBehavior extends AbstractTransitionBehavior<SequenceFlow> {

    @Override
    public boolean match(ExecutionContext context, Transition transition) {

        ConditionExpression conditionExpression = transition.getConditionExpression();

        if (null != conditionExpression) {

            Boolean result = AgentExpressionUtil.eval(context, conditionExpression);

            if(result) {
                AgentTraceHelper.traceTransition(context, conditionExpression, true);

                Map<String, Object> enirchMap = new HashMap<>();
                enirchMap.put("templateType", "exclusiveGateway");
                enirchMap.put("parentTemplateType", "start");
                ProcessExecuteLog.stop(context, conditionExpression, true, enirchMap);

                String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
                //设置当前节点作为下一个运行节点的父节点
                context.getRequest().put("temporaryParentTemplateType", "exclusiveGateway" + "_" + activityId);
            }

            return result;
        }else{
            throw new EngineException("Should config condition expression for ExclusiveGateway,the sequenceFlow is " + transition);
        }
    }
}

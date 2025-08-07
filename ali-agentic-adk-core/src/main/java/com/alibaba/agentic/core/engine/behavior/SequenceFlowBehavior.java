package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.smart.framework.engine.behavior.TransitionBehavior;
import com.alibaba.smart.framework.engine.behavior.base.AbstractTransitionBehavior;
import com.alibaba.smart.framework.engine.bpmn.assembly.process.SequenceFlow;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.model.assembly.Transition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
@ExtensionBinding(group = ExtensionConstant.ACTIVITY_BEHAVIOR, bindKey = TransitionBehavior.class, priority = 1)
public class SequenceFlowBehavior extends AbstractTransitionBehavior<SequenceFlow> {

    @Override
    public boolean match(ExecutionContext executionContext, Transition transition) {
        log.info("smart engine execute fancy sequence flow match: {}", executionContext.getRequest());
        Map<String, Object> request = executionContext.getRequest();
        if (!request.containsKey(ExecutionConstant.SYSTEM_CONTEXT)) {
            throw new BaseException(String.format("smart engine execute flow sequence flow's systemcontext should not be empty, executionContext: %s, transition: %s.", executionContext, transition), ErrorEnum.SYSTEM_ERROR);
        }
        // default/else分支，默认先不匹配。如果所有条件分支均不成立，才在chooseOnlyOne逻辑中匹配
        if (MapUtils.isNotEmpty(transition.getProperties()) && PropertyConstant.SYMBOL_VALUE_CONDITION_DEFAULT_FLOW.equals(transition.getProperties().get(PropertyConstant.SYMBOL_KEY))) {
            return false;
        }
        // exclusiveGatewayId
        String sourceRefId = transition.getSourceRef();
        // next nodeId if branch is true
        String targetRefId = transition.getTargetRef();
        if (StringUtils.isEmpty(sourceRefId) || StringUtils.isEmpty(targetRefId)) {
            throw new BaseException(String.format("smart engine execute flow sequence flow's sourceRef or targetRef should not be empty, executionContext: %s, transition: %s.", executionContext, transition), ErrorEnum.SYSTEM_ERROR);
        }
        SystemContext systemContext = (SystemContext) request.get(ExecutionConstant.SYSTEM_CONTEXT);
        return ConditionRegistry.eval(systemContext, sourceRefId, targetRefId);
    }
}

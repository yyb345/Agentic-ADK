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
package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.smart.framework.engine.behavior.base.AbstractActivityBehavior;
import com.alibaba.smart.framework.engine.bpmn.assembly.gateway.ExclusiveGateway;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@ExtensionBinding(group = ExtensionConstant.ACTIVITY_BEHAVIOR, bindKey = ExclusiveGateway.class, priority = 1)
public class ExclusiveGatewayBehavior extends AbstractActivityBehavior<ExclusiveGateway> {

    public ExclusiveGatewayBehavior() {
        super();
    }

    @Override
    public void leave(ExecutionContext context, PvmActivity pvmActivity) {
        fireEvent(context, pvmActivity, PvmEventConstant.ACTIVITY_END);

        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();

        Map<String, Object> response = context.getResponse();
        Flowable<Result> flowableResult = (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
        flowableResult.doOnNext(result -> {
            if (!result.isSuccess()) {
                log.warn("Flow execution error somewhere, exclusiveGateway chooses default edge.");
                GatewayHelper.chooseDefaultEdge(context, pvmActivity);
            }
            if (outcomeTransitions.size() >= 2) {
                GatewayHelper.chooseOnlyOne(context, pvmActivity);
            } else {
                throw new EngineException("the outcomeTransitions.size() should >= 2");
            }
        }).onErrorReturn(Result::fail).subscribe();
    }
}
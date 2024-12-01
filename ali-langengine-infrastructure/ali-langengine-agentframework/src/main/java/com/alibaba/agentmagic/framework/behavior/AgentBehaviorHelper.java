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
package com.alibaba.agentmagic.framework.behavior;

import com.alibaba.agentmagic.framework.delegation.provider.AgentTraceHelper;
import com.alibaba.smart.framework.engine.bpmn.behavior.gateway.ExclusiveGatewayBehaviorHelper;
import com.alibaba.smart.framework.engine.common.util.MapUtil;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AgentBehaviorHelper {

    public static void leave(ExecutionContext context, PvmActivity pvmActivity) {
        //执行每个节点的hook方法
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();

        if(MapUtil.isEmpty(outcomeTransitions)){
            log.info("AgentBehavior outcomeTransitions found for activity id: "+pvmActivity.getModel().getId()+", it's just fine, it should be the last activity of the process");
            log.info("AgentBehaviorHelper process end with " + context.getProcessDefinition().getId() + ":" + context.getProcessDefinition().getVersion());

            AgentTraceHelper.traceProcessEnd(context);
        }else{
            if( outcomeTransitions.size() == 1) {
                for (Map.Entry<String, PvmTransition> pvmTransitionEntry : outcomeTransitions.entrySet()) {
                    PvmActivity target = pvmTransitionEntry.getValue().getTarget();
                    target.enter(context);
                }
            }else {
                ExclusiveGatewayBehaviorHelper.chooseOnlyOne(context, outcomeTransitions);
            }
        }
    }
}

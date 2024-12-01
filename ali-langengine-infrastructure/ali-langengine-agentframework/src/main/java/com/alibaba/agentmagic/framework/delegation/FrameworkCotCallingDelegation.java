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
package com.alibaba.agentmagic.framework.delegation;

import com.alibaba.agentmagic.framework.delegation.constants.CotCallingConstant;
import com.alibaba.agentmagic.framework.delegation.constants.ToolCallingConstant;
import com.alibaba.agentmagic.framework.delegation.cotexecutor.*;
import com.alibaba.agentmagic.framework.delegation.provider.DelegationHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.agent.domain.AgentRelation;
import com.alibaba.langengine.agentframework.model.domain.AgentAPIInvokeResponse;
import com.alibaba.langengine.agentframework.model.domain.AgentAPIResult;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.alibaba.agentmagic.framework.utils.FrameworkSystemContextUtils;

import java.util.*;
import java.util.function.Consumer;

import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.*;

/**
 * Framework CoT调用节点
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Component
public class FrameworkCotCallingDelegation extends FrameworkDelegationBase<Map<String, Object>> implements ToolCallingConstant, CotCallingConstant {

    private static BaseCotExecutor functionCallExecutor = new FunctionCallExecutor();
    private static BaseCotExecutor planAndExecuteExecutor = new PlanAndExecuteExecutor();
    private static BaseCotExecutor sequentialPlannerExecutor = new SequentialPlannerExecutor();
    private static ShortcutExecutor shortcutExecutor = new ShortcutExecutor();

    @Override
    public Map<String, Object> executeInternal(ExecutionContext executionContext, JSONObject properties, JSONObject request) {
        FrameworkSystemContext systemContext = FrameworkSystemContextUtils.getSystemContext(request, executionContext);
        Object agentRelationObj = DelegationHelper.getSystem(request, AGENT_RELATION_KEY);
        if(agentRelationObj == null) {
            throw new AgentMagicException(AgentMagicErrorCode.COT_SYSTEM_ERROR, "agent relation is empty, agentCode is " + systemContext.getAgentCode(), systemContext.getRequestId());
        }
        AgentRelation agentRelation = systemContext.getAgentRelation();
        log.info("CotCallingDelegation.executeInternal agentRelation is " + JSON.toJSONString(agentRelation));

        int executeType = agentRelation.getExecuteType();
        log.info("cotExecuteType is " + executeType + ", agentCode is " + systemContext.getAgentCode());
        if(executeType == ExecuteType.PLAN.getValue()) {
            // PlanAndExecute模式
            return planAndExecuteExecutor.invokeAgent(systemContext, this);
        } else  if(executeType == ExecuteType.SEMANTIC.getValue()) {
            // SemanticKernel-SequentialPlanner模式
            return sequentialPlannerExecutor.invokeAgent(systemContext, this);
        } else if (executeType == ExecuteType.SHORTCUT.getValue()) {
            // 快捷指令调用
            return shortcutExecutor.execute(systemContext, this);
        } else {
            // 普通FunctionCall模式
            return functionCallExecutor.invokeAgent(systemContext, this);
        }
    }

    public void onStreamNext(FrameworkSystemContext systemContext, AgentAPIResult<AgentAPIInvokeResponse> apiResult) {
        Consumer<Object> chunkConsumer = systemContext.getChunkConsumer();
        if(chunkConsumer != null) {
            chunkConsumer.accept(apiResult);
        }
    }
}

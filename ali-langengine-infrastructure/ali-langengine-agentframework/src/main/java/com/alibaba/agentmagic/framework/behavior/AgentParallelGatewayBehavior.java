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

import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.agentmagic.framework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.smart.framework.engine.behavior.base.AbstractActivityBehavior;
import com.alibaba.smart.framework.engine.bpmn.assembly.gateway.ParallelGateway;
import com.alibaba.smart.framework.engine.common.util.InstanceUtil;
import com.alibaba.smart.framework.engine.common.util.MarkDoneUtil;
import com.alibaba.smart.framework.engine.configuration.ConfigurationOption;
import com.alibaba.smart.framework.engine.configuration.LockStrategy;
import com.alibaba.smart.framework.engine.configuration.ParallelServiceOrchestration;
import com.alibaba.smart.framework.engine.configuration.impl.PvmActivityTask;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.model.instance.ExecutionInstance;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import static com.alibaba.langengine.agentframework.model.constant.ProcessConstants.OUT_PARALLEL_START_KEY;

@Slf4j
@ExtensionBinding(group = ExtensionConstant.ACTIVITY_BEHAVIOR, bindKey = ParallelGateway.class, priority = 1)

public class AgentParallelGatewayBehavior extends AbstractActivityBehavior<ParallelGateway> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentParallelGatewayBehavior.class);


    public AgentParallelGatewayBehavior() {
        super();
    }

    @Override
    public boolean enter(ExecutionContext context, PvmActivity pvmActivity) {
        ParallelGateway parallelGateway = (ParallelGateway)pvmActivity.getModel();

        Boolean async = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), SystemConstant.ASYNC_KEY, false);
        Boolean sequentialExecution = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), SystemConstant.SEQUENTIAL_EXECUTION_KEY, false);
        log.info("AgentParallelGatewayBehavior enter async is " + async + ", sequentialExecution is " + sequentialExecution + ", parallelGateway id is " + parallelGateway.getId());
        if(async && sequentialExecution) {
            return sequentialExecutionLogic(context, pvmActivity, parallelGateway);
        }

        return _enter(context, pvmActivity);
    }

    private boolean _enter(ExecutionContext context, PvmActivity pvmActivity) {

        //算法说明:ParallelGatewayBehavior 同时承担 fork 和 join 职责。所以说,如何判断是 fork 还是 join ?
        // 目前主要原则就看pvmActivity节点的 incomeTransition 和 outcomeTransition 的比较。
        // 如果 income 为1,则为 join 节点。
        // 如果 outcome 为 1 ,则为 fork 节点。
        // 重要:在流程定义解析时,需要判断如果是 fork,则 outcome >=2, income=1; 类似的,如果是 join,则 outcome = 1,income>=2

        ParallelGateway parallelGateway = (ParallelGateway)pvmActivity.getModel();



        ConfigurationOption serviceOrchestrationOption = processEngineConfiguration
                .getOptionContainer().get(ConfigurationOption.SERVICE_ORCHESTRATION_OPTION.getId());

        //此处，针对基于并行网关的服务编排做了特殊优化处理。
        if(serviceOrchestrationOption.isEnabled()){

            fireEvent(context,pvmActivity, PvmEventConstant.ACTIVITY_START);

            ParallelServiceOrchestration parallelServiceOrchestration = context.getProcessEngineConfiguration()
                    .getParallelServiceOrchestration();

            parallelServiceOrchestration.orchestrateService(context, pvmActivity);

            //由于这里仅是服务编排，所以这里直接返回`暂停`信号。
            return true;

        }else {


            return defaultLogic(context, pvmActivity, parallelGateway);
        }



    }



    private boolean defaultLogic(ExecutionContext context, PvmActivity pvmActivity, ParallelGateway parallelGateway) {


        Map<String, PvmTransition> incomeTransitions = pvmActivity.getIncomeTransitions();
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();

        int outComeTransitionSize = outcomeTransitions.size();
        int inComeTransitionSize = incomeTransitions.size();

        if (outComeTransitionSize >= 2 && inComeTransitionSize == 1) {
            //fork

            fireEvent(context,pvmActivity, PvmEventConstant.ACTIVITY_START);


            ExecutorService executorService = context.getProcessEngineConfiguration().getExecutorService();
            if(null == executorService){
                //顺序执行fork
                for (Entry<String, PvmTransition> pvmTransitionEntry : outcomeTransitions.entrySet()) {
                    PvmActivity target = pvmTransitionEntry.getValue().getTarget();
                    target.enter(context);
                }
            }else{
                //并发执行fork

                List<PvmActivityTask> tasks = new ArrayList<PvmActivityTask>(outcomeTransitions.size());

                for (Entry<String, PvmTransition> pvmTransitionEntry : outcomeTransitions.entrySet()) {
                    PvmActivity target = pvmTransitionEntry.getValue().getTarget();

                    PvmActivityTask task = new PvmActivityTask(target,context);
                    tasks.add(task);
                }


                try {
                    executorService.invokeAll(tasks);
                } catch (InterruptedException e) {
                    throw new EngineException(e.getMessage(), e);
                }

            }

        } else if (outComeTransitionSize == 1 && inComeTransitionSize >= 2) {
            //join 时必须使用分布式锁。

            LockStrategy lockStrategy = context.getProcessEngineConfiguration().getLockStrategy();
            String processInstanceId = context.getProcessInstance().getInstanceId();
            try{

                super.enter(context, pvmActivity);

                lockStrategy.tryLock(processInstanceId,context);


                Collection<PvmTransition> inComingPvmTransitions = incomeTransitions.values();


                ProcessInstance processInstance = context.getProcessInstance();

                //当前内存中的，新产生的 active ExecutionInstance
                List<ExecutionInstance> executionInstanceListFromMemory = InstanceUtil.findActiveExecution(processInstance);


                //当前持久化介质中中，已产生的 active ExecutionInstance。
                List<ExecutionInstance> executionInstanceListFromDB =  executionInstanceStorage.findActiveExecution(processInstance.getInstanceId(), super.processEngineConfiguration);

                //Merge 数据库中和内存中的EI。如果是 custom模式，则可能会存在重复记录，所以这里需要去重。 如果是 DataBase 模式，则不会有重复的EI.

                List<ExecutionInstance> mergedExecutionInstanceList = new ArrayList<ExecutionInstance>(executionInstanceListFromMemory.size());


                for (ExecutionInstance instance : executionInstanceListFromDB) {
                    if (executionInstanceListFromMemory.contains(instance)){
                        //ignore
                    }else {
                        mergedExecutionInstanceList.add(instance);
                    }
                }


                mergedExecutionInstanceList.addAll(executionInstanceListFromMemory);


                int reachedJoinCounter = 0;
                List<ExecutionInstance> chosenExecutionInstances = new ArrayList<ExecutionInstance>(executionInstanceListFromMemory.size());

                if(null != mergedExecutionInstanceList){

                    for (ExecutionInstance executionInstance : mergedExecutionInstanceList) {

                        if (executionInstance.getProcessDefinitionActivityId().equals(parallelGateway.getId())) {
                            reachedJoinCounter++;
                            chosenExecutionInstances.add(executionInstance);
                        }
                    }
                }


                if(reachedJoinCounter == inComingPvmTransitions.size() ){
                    //把当前停留在join节点的执行实例全部complete掉,然后再持久化时,会自动忽略掉这些节点。

                    if(null != chosenExecutionInstances){
                        for (ExecutionInstance executionInstance : chosenExecutionInstances) {
                            MarkDoneUtil.markDoneExecutionInstance(executionInstance,executionInstanceStorage,
                                    processEngineConfiguration);
                        }
                    }

                    return false;

                }else{
                    //未完成的话,流程继续暂停
                    return true;
                }

            }finally {

                lockStrategy.unLock(processInstanceId,context);
            }

        }else{
            throw new EngineException("should touch here:"+pvmActivity);
        }

        return true;
    }

    private boolean sequentialExecutionLogic(ExecutionContext context, PvmActivity pvmActivity, ParallelGateway parallelGateway) {
        Map<String, PvmTransition> incomeTransitions = pvmActivity.getIncomeTransitions();
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();

        int outComeTransitionSize = outcomeTransitions.size();
        int inComeTransitionSize = incomeTransitions.size();

        if (outComeTransitionSize >= 2 && inComeTransitionSize == 1) {
            //fork

            fireEvent(context,pvmActivity, PvmEventConstant.ACTIVITY_START);

            // 异步化链路
            Boolean async = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), SystemConstant.ASYNC_KEY, false);
            if(async) {
                String outParallelStartActivityId = null;
                if (context.getRequest().get(OUT_PARALLEL_START_KEY) == null) {
                    outParallelStartActivityId = pvmActivity.getModel().getId();
                    context.getRequest().put(OUT_PARALLEL_START_KEY, outParallelStartActivityId);
                } else {
                    outParallelStartActivityId = (String) context.getRequest().get(OUT_PARALLEL_START_KEY);
                }
                log.info("out_parallelStartActivityId is " + outParallelStartActivityId);
            }

            //顺序执行fork
            for (Entry<String, PvmTransition> pvmTransitionEntry : outcomeTransitions.entrySet()) {
                PvmActivity target = pvmTransitionEntry.getValue().getTarget();
                try {
                    target.enter(context);
                } catch (AgentMagicException exception) {
                    if(exception.getErrorCode().equals(AgentMagicErrorCode.PARALLEL_NODE_PROCESS.getCode())) {
                        log.info("parallel_node_process start");
                        return true;
                    }
                    throw exception;
                }
            }

        } else if (outComeTransitionSize == 1 && inComeTransitionSize >= 2) {
            //join 时必须使用分布式锁。

            LockStrategy lockStrategy = context.getProcessEngineConfiguration().getLockStrategy();
            String processInstanceId = context.getProcessInstance().getInstanceId();
            try{

                super.enter(context, pvmActivity);

                lockStrategy.tryLock(processInstanceId,context);


                Collection<PvmTransition> inComingPvmTransitions = incomeTransitions.values();


                ProcessInstance processInstance = context.getProcessInstance();

                //当前内存中的，新产生的 active ExecutionInstance
                List<ExecutionInstance> executionInstanceListFromMemory = InstanceUtil.findActiveExecution(processInstance);
                log.info("executionInstanceListFromMemory size is " + executionInstanceListFromMemory.size());
//                log.info("executionInstanceListFromMemory is " + JSON.toJSONString(executionInstanceListFromMemory));


                //当前持久化介质中中，已产生的 active ExecutionInstance。
                List<ExecutionInstance> executionInstanceListFromDB =  executionInstanceStorage.findActiveExecution(processInstance.getInstanceId(), super.processEngineConfiguration);

                //Merge 数据库中和内存中的EI。如果是 custom模式，则可能会存在重复记录，所以这里需要去重。 如果是 DataBase 模式，则不会有重复的EI.

                List<ExecutionInstance> mergedExecutionInstanceList = new ArrayList<ExecutionInstance>(executionInstanceListFromMemory.size());


                for (ExecutionInstance instance : executionInstanceListFromDB) {
                    if (executionInstanceListFromMemory.contains(instance)){
                        //ignore
                    }else {
                        mergedExecutionInstanceList.add(instance);
                    }
                }


                mergedExecutionInstanceList.addAll(executionInstanceListFromMemory);
                log.info("mergedExecutionInstanceList size is " + mergedExecutionInstanceList.size());

                int reachedJoinCounter = 0;
                List<ExecutionInstance> chosenExecutionInstances = new ArrayList<ExecutionInstance>(executionInstanceListFromMemory.size());

                if(null != mergedExecutionInstanceList){

                    for (ExecutionInstance executionInstance : mergedExecutionInstanceList) {

                        if (executionInstance.getProcessDefinitionActivityId().equals(parallelGateway.getId())) {
                            reachedJoinCounter++;
                            chosenExecutionInstances.add(executionInstance);
                        }
                    }
                }

                log.info("reachedJoinCounter is " + reachedJoinCounter + " parallelGateway id is " + parallelGateway.getId());

                if(reachedJoinCounter == inComingPvmTransitions.size() ){
                    //把当前停留在join节点的执行实例全部complete掉,然后再持久化时,会自动忽略掉这些节点。

                    if(null != chosenExecutionInstances){
                        for (ExecutionInstance executionInstance : chosenExecutionInstances) {
                            MarkDoneUtil.markDoneExecutionInstance(executionInstance,executionInstanceStorage,
                                processEngineConfiguration);
                        }
                    }

                    // 异步化链路
                    Boolean async = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), SystemConstant.ASYNC_KEY, false);
                    if (async) {
                        // 移除并行标
                        context.getRequest().remove(OUT_PARALLEL_START_KEY);
                        log.info("out_parallelStartActivityId removed at " + pvmActivity.getModel().getId());
                    }

                    return false;

                }else{
                    //未完成的话,流程继续暂停
                    return true;
                }

            }finally {

                lockStrategy.unLock(processInstanceId,context);
            }

        }else{
            throw new EngineException("should touch here:"+pvmActivity);
        }

        return true;
    }






}

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
package com.alibaba.agentmagic.framework.config;

import com.alibaba.agentmagic.framework.behavior.AgentParallelGatewayBehavior;
import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.agentmagic.framework.delegation.provider.DelegationHelper;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.LockService;
import com.alibaba.smart.framework.engine.behavior.ActivityBehavior;
import com.alibaba.smart.framework.engine.bpmn.behavior.gateway.GatewaySticker;
import com.alibaba.smart.framework.engine.bpmn.behavior.gateway.ParallelGatewayBehavior;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public  class AgentPvmActivityTask implements Callable<PvmActivity> {

    private PvmActivity pvmActivity;
    private ExecutionContext context;
    private LockService lockService;

    public PvmActivity getPvmActivity() {
        return pvmActivity;
    }

    public AgentPvmActivityTask(PvmActivity pvmActivity, ExecutionContext context, LockService lockService) {
            this.pvmActivity = pvmActivity;
            this.context = context;
            this.lockService = lockService;
        }


        @Override
        public PvmActivity call() {
            String processInstanceId = context.getProcessInstance().getInstanceId();
            Boolean async = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), SystemConstant.ASYNC_KEY, false);

            PvmActivity pvmActivity ;
            try {
                GatewaySticker.create();
                //忽略了子线程的返回值
                log.info("AgentPvmActivityTask task pvmActivity id is " + this.pvmActivity.getModel().getId() +
                        ", activityId is " + (context.getExecutionInstance() != null ? context.getExecutionInstance().getProcessDefinitionActivityId() : null));
                context.getRequest().put("sys_AgentPvmActivityTask", true);
                this.pvmActivity.enter(context);

                pvmActivity = GatewaySticker.currentSession().getPvmActivity();
                log.info("pvmActivity is {}, {}", (pvmActivity != null), (pvmActivity != null ? pvmActivity.getClass().getName() : null));
                if(pvmActivity != null) {
                    ActivityBehavior behavior = pvmActivity.getBehavior();
                    if(behavior != null) {
                        log.info("pvmActivity behavior is {}", (behavior instanceof ParallelGatewayBehavior || behavior instanceof AgentParallelGatewayBehavior));
                        if (behavior instanceof ParallelGatewayBehavior || behavior instanceof AgentParallelGatewayBehavior) {
                            if (async) {
//                                String parallelKey = "parallel_one_finished_" + this.pvmActivity.getModel().getId();
//                                log.info("finished node id is {}", parallelKey);
//                                lockService.set(parallelKey, "true");
//                                log.info("lockService get is {}", lockService.get(parallelKey));
                                //递减1
                                lockService.decr("parallel_process_" + processInstanceId);
                            }
                        }
                    } else {
                        log.info("pvmActivity not found behavior id {}", pvmActivity.getModel().getId());
                    }
                }
                log.info("AgentPvmActivityTask task pvmActivity success");
            } catch (AgentMagicException e) {
                log.error("AgentPvmActivityTask AgentMagicException", e);
                throw e;
            } catch (Throwable e) {
                log.error("AgentPvmActivityTask Exception", e);
                throw e;
            }finally {
                GatewaySticker.destroySession();

                context.getRequest().remove("sys_AgentPvmActivityTask");
            }

            log.info("AgentPvmActivityTask task success");
            return pvmActivity;


        }
    }

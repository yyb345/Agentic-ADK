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
import com.alibaba.langengine.agentframework.model.enums.AgentMagicErrorCode;
import com.alibaba.langengine.agentframework.model.exception.AgentMagicException;
import com.alibaba.langengine.agentframework.model.service.LockService;
import com.alibaba.smart.framework.engine.behavior.ActivityBehavior;
import com.alibaba.smart.framework.engine.bpmn.behavior.gateway.GatewaySticker;
import com.alibaba.smart.framework.engine.bpmn.behavior.gateway.ParallelGatewayBehavior;
import com.alibaba.smart.framework.engine.common.util.MapUtil;
import com.alibaba.smart.framework.engine.common.util.StringUtil;
import com.alibaba.smart.framework.engine.configuration.ExceptionProcessor;
import com.alibaba.smart.framework.engine.configuration.ParallelServiceOrchestration;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.configuration.scanner.AnnotationScanner;
import com.alibaba.smart.framework.engine.constant.ParallelGatewayConstant;
import com.alibaba.smart.framework.engine.constant.RequestMapSpecialKeyConstant;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.context.factory.ContextFactory;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import com.alibaba.smart.framework.engine.util.CompletedFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static com.alibaba.agentmagic.framework.delegation.constants.SystemConstant.TRACE_OUTPUT_KEY;
import static com.alibaba.langengine.agentframework.model.constant.ProcessConstants.OUT_PARALLEL_START_KEY;

/**
 * 自定义实现服务编排执行器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class AgentParallelServiceOrchestration implements ParallelServiceOrchestration {

    /**
     *  锁服务
     */
    private LockService lockService;

    @Override
    public void orchestrateService(ExecutionContext context, PvmActivity pvmActivity) {
        Map<String, PvmTransition> incomeTransitions = pvmActivity.getIncomeTransitions();
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        String processInstanceId = context.getProcessInstance().getInstanceId();
        Boolean async = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), SystemConstant.ASYNC_KEY, false);

        int outComeTransitionSize = outcomeTransitions.size();
        int inComeTransitionSize = incomeTransitions.size();

        if (outComeTransitionSize >= 2 && inComeTransitionSize == 1) {
            Map<String, String> properties = pvmActivity.getModel().getProperties();

            //并发执行fork
            ProcessEngineConfiguration processEngineConfiguration = context.getProcessEngineConfiguration();
            // 兜底先使用共享线程池
            ExecutorService executorService = processEngineConfiguration.getExecutorService();

            // 如果能匹配到自定义的线程池，直接使用。
            Map<String, ExecutorService> poolsMap = processEngineConfiguration.getExecutorServiceMap();
            String poolName;
            if (poolsMap != null && properties != null && (poolName =
                    properties.get(ParallelGatewayConstant.POOL_NAME)) != null
                    && poolsMap.containsKey(poolName)) {
                executorService = poolsMap.get(poolName);
            }

            List<AgentPvmActivityTask> tasks = new ArrayList<AgentPvmActivityTask>(outComeTransitionSize);

            AnnotationScanner annotationScanner = processEngineConfiguration.getAnnotationScanner();

            ContextFactory contextFactory = annotationScanner.getExtensionPoint(ExtensionConstant.COMMON,
                    ContextFactory.class);

            int counter = 0;
            PvmActivity finalJoinActivity = null;
            for (Entry<String, PvmTransition> pvmTransitionEntry : outcomeTransitions.entrySet()) {

                //target 为fork 节点的后继节点，比如service1，service3
                PvmActivity target = pvmTransitionEntry.getValue().getTarget();

//                String incomeTransitionKey = null;
//                if(async) {
//                    incomeTransitionKey = pvmTransitionEntry.getKey();
//                    if (context.getRequest().get("parallel_income_" + incomeTransitionKey) != null) {
//                        log.info("The incomeTransitionKey " + incomeTransitionKey + " execute ignore");
//                        continue;
//                    }
//                }

                // 先将子任务的下一个节点，即join
                finalJoinActivity = findOutTheJoinPvmActivity(target, 0);

                // 异步化链路
                if (async) {
                    String outParallelStartActivityId = null;
                    if (context.getRequest().get(OUT_PARALLEL_START_KEY) == null) {
                        outParallelStartActivityId = pvmActivity.getModel().getId();
                        context.getRequest().put(OUT_PARALLEL_START_KEY, outParallelStartActivityId);
                    }
                    log.info("out_parallelStartActivityId is " + outParallelStartActivityId);

                    counter++;
//                    String parallelKey = "parallel_one_finished_" + target.getModel().getId();
//                    log.info("task start is {}", parallelKey);
//                    log.info("lockService is {}", (lockService != null));
//                    log.info("lockService get is {}", lockService.get(parallelKey));
//                    if(lockService != null && StringUtil.isEmpty(lockService.get(parallelKey))) {
//                        counter++;
//                    }
                }

                //从ParentContext 复制父Context到子线程内。这里得注意下线程安全。
                ExecutionContext subThreadContext = contextFactory.createChildThreadContext(context);

                AgentPvmActivityTask task = new AgentPvmActivityTask(target, subThreadContext, lockService);

                tasks.add(task);
            }

            if(lockService != null && async) {
                log.info("lockService reset is {}", counter);
                Long count = lockService.reset("parallel_process_" + processInstanceId, Long.valueOf(counter));
                // TODO 半小时失效
                lockService.expire("parallel_process_" + processInstanceId, 30 * 60);
                log.info("lockService value is {}", count);
            }

            //注意这里的逻辑：这里假设是子线程在执行某个fork分支的逻辑后，然后会在join节点时返回。这个join节点就是 futureJoinParallelGateWay。
            // 当await 执行结束后，这里的假设不变式：所有子线程都已经到达了join节点。
            ExceptionProcessor exceptionProcessor = processEngineConfiguration.getExceptionProcessor();

            try {

                Long latchWaitTime = null;

                // 优先从并行网关属性上获取等待超时时间
                String waitTimeout = (String) MapUtil.safeGet(properties, ParallelGatewayConstant.WAIT_TIME_OUT);
                if (StringUtil.isNotEmpty(waitTimeout)) {
                    try {
                        latchWaitTime = Long.valueOf(waitTimeout);
                    } catch (NumberFormatException e) {
                        throw new EngineException("latchWaitTime type should be Long");
                    }
                }

                // 如果网关属性上未配置超时时间，或格式非法。兜底从request上下文中获取配置
                if (null == latchWaitTime || latchWaitTime <= 0) {
                    Object latchWaitTimeObject = MapUtil.safeGet(context.getRequest(),
                            RequestMapSpecialKeyConstant.LATCH_WAIT_TIME_IN_MILLISECOND);
                    if (latchWaitTimeObject != null) {
                        if (latchWaitTimeObject instanceof Integer) {
                            latchWaitTime = Long.parseLong(latchWaitTimeObject.toString());
                        } else {
                            latchWaitTime = (Long) latchWaitTimeObject;
                        }
                    }
                }

                // 是否跳过超时异常
                boolean isSkipTimeoutExp = false;
                String skipTimeoutProp = (String) MapUtil.safeGet(properties, ParallelGatewayConstant.SKIP_TIMEOUT_EXCEPTION);
                ParallelGatewayConstant.ExecuteStrategy executeStrategy = getExecuteStrategy(properties);

                List<Future<PvmActivity>> futureExecutionResultList = new ArrayList<Future<PvmActivity>>();

                if (null != latchWaitTime && latchWaitTime > 0L) {
                    isSkipTimeoutExp = null != skipTimeoutProp && skipTimeoutProp.trim().equals(Boolean.TRUE.toString());
                    if (executeStrategy.equals(ParallelGatewayConstant.ExecuteStrategy.INVOKE_ALL)) {
                        log.info("invoke all tasks by latchWaitTime is {}", latchWaitTime);
                        futureExecutionResultList = executorService.invokeAll(tasks, latchWaitTime, TimeUnit.MILLISECONDS);
                    } else {
                        log.info("invoke any tasks by latchWaitTime is {}", latchWaitTime);
                        futureExecutionResultList.add(invokeAnyOf(executorService, tasks, latchWaitTime,
                                isSkipTimeoutExp));
                    }
                } else {
                    // 超时等待时间为空或不大于0，无需wait
                    if (executeStrategy.equals(ParallelGatewayConstant.ExecuteStrategy.INVOKE_ALL)) {
                        log.info("invoke all tasks");
                        futureExecutionResultList = executorService.invokeAll(tasks);
                    } else {
                        log.info("invoke any tasks");
                        futureExecutionResultList.add(invokeAnyOf(executorService, tasks, 0,
                                false));
                    }
                }

                boolean failed = false;
                for (Future<PvmActivity> pvmActivityFuture : futureExecutionResultList) {
                    try {
                        if (isSkipTimeoutExp) {
                            pvmActivityFuture.get(latchWaitTime, TimeUnit.MILLISECONDS);
                        } else {
                            pvmActivityFuture.get();
                        }
                    } catch (InterruptedException e) {
                        exceptionProcessor.process(e, context);
                    } catch (AgentMagicException e) {
                        log.error("parallelService AgentMagicException", e);
                        exceptionProcessor.process(e, context);
//                        throw e;
                    } catch (ExecutionException e) {
                        log.error("parallelService ExecutionException", e);
                        Throwable throwable = e.getCause();
                        if(throwable != null && throwable instanceof AgentMagicException) {
//                            exceptionProcessor.process((AgentMagicException)throwable, context);
                            boolean traceOutput = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), TRACE_OUTPUT_KEY, false);
                            if(traceOutput) {
                                failed = true;
                            } else {
                                throw e;
                            }
                        } else {
                            exceptionProcessor.process(e, context);
                        }
                    } catch (CancellationException e) {
                        // 忽略超时异常
                        if (isSkipTimeoutExp) {
                            // 跳过超时异常，只记录log
                            log.warn("parallel gateway occur timeout, skip exception!", e);
                        } else {
                            throw e;
                        }
                    }
                }

                if(failed) {
                    log.error("parallelService failed");
                    return;
                }

                // 获取第一个成功执行的future
                Future<PvmActivity> pvmActivityFuture = getSuccessFuture(futureExecutionResultList, isSkipTimeoutExp);

                PvmActivity futureJoinParallelGateWay = null;
                if (null == pvmActivityFuture) {
                    // 如果没有找到，只有一种可能就是子任务全超时被cancel了。直接使用finalJoinActivity
                    futureJoinParallelGateWay = finalJoinActivity;
                } else {
                    // 直接从future中获取join事件节点
                    futureJoinParallelGateWay = pvmActivityFuture.get();
                }

                if (futureJoinParallelGateWay == null) {
                    // 异步化
                    if (async) {
                        log.info("AgentParallelServiceOrchestration async processing");
                        return;
                    } else {
                        log.error("futureJoinParallelGateWay is null");
                        String requestId = DelegationHelper.getSystemString(context.getRequest(), SystemConstant.REQUEST_ID_KEY);
                        // 直接抛出异常并结束
                        throw new AgentMagicException(AgentMagicErrorCode.PARALLEL_NODE_ERROR, "", requestId);
                    }
                }

//                if(failed) {
//                    log.error("parallelService failed");
//                    return;
//                }

                ActivityBehavior behavior = futureJoinParallelGateWay.getBehavior();

                // 异步化链路
                if (async) {
                    // 移除并行标
                    context.getRequest().remove(OUT_PARALLEL_START_KEY);
                    log.info("out_parallelStartActivityId removed in {}", pvmActivity.getModel().getId());

                    lockService.delete("parallel_process_" + processInstanceId);


//                    for (AgentPvmActivityTask task : tasks) {
//                        lockService.delete("parallel_one_finished_" + task.getPvmActivity().getModel().getId());
//                    }

                    //移除并行中各个入口线的标
//                    Iterator<Entry<String, Object>> iterator = context.getRequest().entrySet().iterator();
//                    while(iterator.hasNext()) {
//                        Entry<String, Object> entry = iterator.next();
//                        if(entry.getKey().indexOf("parallel_income_") == 0) {
//                            iterator.remove();
//                            log.info("{} income removed in {}", entry.getKey(), pvmActivity.getModel().getId());
//                        }
//                    }
                }

                //模拟正常流程的继续驱动，将继续推进caller thread 执行后续节点。
                behavior.leave(context, futureJoinParallelGateWay);

            } catch (AgentMagicException e) {
                log.error("AgentMagic parallelService total error", e);
                exceptionProcessor.process(e, context);
            } catch (Exception e) {
                log.error("orchestrateService error", e);
                Throwable throwable = e.getCause();
                if(throwable != null && throwable instanceof AgentMagicException) {
                    log.error("orchestrateService AgentMagicException error", throwable);

                    Boolean traceOutput = DelegationHelper.getSystemBooleanOrDefault(context.getRequest(), TRACE_OUTPUT_KEY, false);
                    if(traceOutput) {
                        return;
                    } else {
                        AgentMagicException agentMagicException = (AgentMagicException) throwable;
                        throw agentMagicException;
                    }
//                    exceptionProcessor.process((AgentMagicException) throwable, context);
//                    context.setNeedPause(true);
                }
                throw new EngineException(e);
            }


        } else if (outComeTransitionSize == 1 && inComeTransitionSize >= 2) {
            log.info("AgentParallelServiceOrchestration join start");

            //在服务编排场景，仅是子线程在执行到最后一个节点后，会进入到并行网关的join节点。CallerThread 不会执行到这里的逻辑。
            GatewaySticker.currentSession().setPvmActivity(pvmActivity);

        } else {
            throw new EngineException("Should not touch here:" + pvmActivity);
        }

    }

    /**
     * 获取成功的一个future
     * @param futureList future列表
     * @param skipTimeout 是否忽略超时
     * @return 返回第一个成功的future
     */
    private Future<PvmActivity> getSuccessFuture(List<Future<PvmActivity>> futureList, boolean skipTimeout) {
        if (null == futureList) {
            return null;
        }

        // 没有抑制超时异常，直接获取第一个即可。
        if (!skipTimeout) {
            return futureList.get(0);
        }

        for (Future<PvmActivity> future : futureList) {
            // DONE且是非取消状态
            if (future.isDone() && !future.isCancelled()) {
                return future;
            }
        }
        return null;
    }

    /**
     * 获取当前activity的下一个节点。从outcome列表中任意取一个
     * @param pvmActivity 当前节点
     * @return 任意下一节点
     */
    private PvmActivity getFirstOutcomePvmActivity(PvmActivity pvmActivity) {
        Map<String, PvmTransition> transitions;
        if (pvmActivity == null || (transitions = pvmActivity.getOutcomeTransitions()) == null) {
            return null;
        }
        for (Entry<String, PvmTransition> outcome : transitions.entrySet()) {
            return outcome.getValue().getTarget();
        }

        return null;
    }

    private static final int DEEP_MAX = 20;
    /**
     * 从单个节点开始，不停的下一步遍历，找到第一个为并行网关的节点。暂时不支持并行网关的嵌套，理论可以获取到JoinPvmActivity节点。
     * @param pvmActivity 当前节点
     * @return 并行网关的join节点
     */
    private PvmActivity findOutTheJoinPvmActivity(PvmActivity pvmActivity, int counter) {
        if(DEEP_MAX == counter) {
            return null;
        }

        PvmActivity matchedTarget = null;

        Map<String, PvmTransition> transitions = pvmActivity.getOutcomeTransitions();

        for (Entry<String, PvmTransition> outcome : transitions.entrySet()) {
            PvmActivity  successorTarget = outcome.getValue().getTarget();

            ActivityBehavior behavior = successorTarget.getBehavior();
            if(behavior instanceof ParallelGatewayBehavior || behavior instanceof AgentParallelGatewayBehavior){
                matchedTarget = successorTarget;

                if(matchedTarget != null){
                    break;
                }

            }else{
                findOutTheJoinPvmActivity(successorTarget, ++counter);

            }

        }

        return matchedTarget;


    }

    /**
     * 获取执行策略，默认用ALL兜底
     *
     * @param properties
     * @return
     */
    private ParallelGatewayConstant.ExecuteStrategy getExecuteStrategy(Map<String, String> properties) {
        if (null == properties || properties.isEmpty()) {
            return ParallelGatewayConstant.ExecuteStrategy.INVOKE_ALL;
        }
        String strategyProp = (String) MapUtil.safeGet(properties, ParallelGatewayConstant.EXE_STRATEGY);
        ParallelGatewayConstant.ExecuteStrategy executeStrategy = null;
        if (StringUtil.isNotEmpty(strategyProp)) {
            executeStrategy = ParallelGatewayConstant.ExecuteStrategy.build(strategyProp);
        }
        if (executeStrategy == null) {
            executeStrategy = ParallelGatewayConstant.ExecuteStrategy.INVOKE_ALL;
        }
        return executeStrategy;
    }

    /**
     * race模式执行，返回最快的一个
     * @param pool 线程池
     * @param tasks 任务集
     * @param timeout 超时时间
     * @param ignoreTimeout 是否忽略超时异常
     * @return future对象
     */
    public Future<PvmActivity> invokeAnyOf(ExecutorService pool, List<AgentPvmActivityTask> tasks, long timeout,
                                           boolean ignoreTimeout) throws Exception {

        PvmActivity pvmActivity = null;
        Exception ex = null;

        // 不处理超时的情况
        if (timeout <= 0) {
            pvmActivity = pool.invokeAny(tasks);
        } else {
            // 处理timeout的方式
            try {
                pvmActivity = pool.invokeAny(tasks, timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw e;
            } catch (ExecutionException e) {
                throw e;
            } catch (TimeoutException e) {
                if (!ignoreTimeout) {
                    throw e;
                }
                ex = e;
            }
        }

        return new CompletedFuture<PvmActivity>(pvmActivity, ex);
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }
}
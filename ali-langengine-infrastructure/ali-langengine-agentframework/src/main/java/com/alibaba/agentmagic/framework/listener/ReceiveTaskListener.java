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
package com.alibaba.agentmagic.framework.listener;

import com.alibaba.agentmagic.framework.constants.ProcessConstant;
import com.alibaba.agentmagic.framework.utils.DelegationUtils;
import com.alibaba.agentmagic.framework.utils.FrameworkUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.listener.Listener;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReceiveTask类型节点
 */
@Slf4j
@Component
public class ReceiveTaskListener implements Listener {

//    @Resource
//    private ProcessInstanceManager processInstanceManager;
//
//    @Resource
//    private ProcessInstanceService processInstanceService;
//
//    @Resource
//    private ProcessTaskManager processTaskManager;

    @Override
    public void execute(PvmEventConstant event, ExecutionContext context) {
        if(event != PvmEventConstant.ACTIVITY_START) {
            return;
        }
        // 获取节点属性
        String processInstanceId = context.getProcessInstance().getInstanceId();
        String activityId = context.getExecutionInstance().getProcessDefinitionActivityId();
        JSONObject propertiesJson = FrameworkUtils.getJsonPropertiesFromContext(context, activityId);
        // 解析节点参数
        JSONObject requestJson = new JSONObject(context.getRequest());
        String formFieldStr = DelegationUtils.buildJsonArrayValue(propertiesJson.getString(ProcessConstant.AUDIT_FIELD_LIST)).toJSONString();
        String userType = propertiesJson.getString(ProcessConstant.AUDIT_USER_TYPE);
        String auditType = propertiesJson.getString(ProcessConstant.AUDIT_AUDIT_TYPE);
        List<String> userItemList = DelegationUtils.buildJsonArrayValue(propertiesJson.getString(ProcessConstant.AUDIT_USER_LIST)).toJavaList(String.class);

        List<String> userIdList = new ArrayList<>();
        if("fixed".equals(userType)) {
            for (String item : userItemList) {
                Object processVariable = FrameworkUtils.getProcessVariable(item,requestJson);
                if(processVariable == null) {
                    continue;
                }else if(processVariable instanceof List) {
                    List<String> collect = ((List<Object>) processVariable).stream().map(Object::toString).collect(Collectors.toList());
                    userIdList.addAll(collect);
                }else {
                    userIdList.add(processVariable.toString());
                }
            }
        }
        // TODO: 动态获取操作人

        // TODO by xiaoxuan.lp
//        formFieldStr = ConnectorDelegationBase.replace(formFieldStr, requestJson);
//
//        // 进入节点时，保存节点状态
//        ProcessInstanceDO processInstanceDO = new ProcessInstanceDO();
//        processInstanceDO.setProcessInstanceId(processInstanceId);
//        // 记录系统参数 流程参数
//        ProcessSystemContext processSystemContext = requestJson.getObject(ProcessSystemContext.KEY, ProcessSystemContext.class);
//        processSystemContext.setHasRecord(true);
//        context.getRequest().put(ProcessSystemContext.KEY,processSystemContext);
//        processInstanceDO.setContext(JSON.toJSONString(context.getRequest()));
//        // 记录流程实例Map
//        ProcessInstanceSnapshot snapshot = new ProcessInstanceSnapshot();
//        snapshot.putProcessInstance(context.getProcessInstance());
//        if (Objects.nonNull(context.getParent()) && Objects.nonNull(context.getParent().getProcessInstance())) {
//            snapshot.putProcessInstance(context.getParent().getProcessInstance());
//        }
//        processInstanceDO.setProcessDefinitionId(context.getProcessDefinition().getId());
//        processInstanceDO.setProcessDefinitionVersion(Integer.valueOf(context.getProcessDefinition().getVersion()));
//        processInstanceDO.setActivityId(activityId);
//        processInstanceDO.setSerializedProcessInstance(JSON.toJSONString(snapshot));
//        processInstanceDO.setProcessInstanceId(processInstanceId);
//        processInstanceDO.setStatus(ProcessInstanceDO.STATUS_PAUSE);
//        processInstanceDO.setFormField(formFieldStr);
//        processInstanceDO.setNeedRetry(0);
//        processInstanceManager.addOrReplace(processInstanceDO);
//
//        List<ProcessTaskDO> taskDOList = new ArrayList<>();
//        if(CollectionUtils.isEmpty(userIdList)) {
//            // 无操作人
//            ProcessTaskDO taskDO = new ProcessTaskDO();
//            taskDO.setUserId("*");
//            taskDO.setActivityId(activityId);
//            taskDO.setProcessInstanceId(processInstanceId);
//            taskDO.setStatus(ProcessTaskDO.STATUS_CREATED);
//            taskDO.setType("audit");
//            taskDO.setTaskType(auditType);
//            taskDO.setContent(formFieldStr);
//            taskDOList.add(taskDO);
//            processTaskManager.batchAddTask(taskDOList);
//            return;
//        }else {
//            // 有操作人，创建任务
//            for (String userId : userIdList) {
//                ProcessTaskDO taskDO = new ProcessTaskDO();
//                taskDO.setUserId(userId);
//                taskDO.setActivityId(activityId);
//                taskDO.setProcessInstanceId(processInstanceId);
//                taskDO.setStatus(ProcessTaskDO.STATUS_CREATED);
//                taskDO.setType("audit");
//                taskDO.setTaskType(auditType);
//                taskDO.setContent(formFieldStr);
//                taskDOList.add(taskDO);
//            }
//            processTaskManager.batchAddTask(taskDOList);
//        }
//        SeUtils.addSeProcessLog(event, context, "创建审批任务");
//
//        if(context.getRequest().get(ProcessConstant.ASYNC_CONTEXT) != null && context.getRequest().get(ProcessConstant.ASYNC_CONTEXT) instanceof AsyncContext) {
//            AsyncContext asyncContext =(AsyncContext) context.getRequest().get(ProcessConstant.ASYNC_CONTEXT);
//            SeUtils.addInstanceIdForResponse(context.getResponse(),context.getProcessInstance().getInstanceId());
//            asyncContext.write(ResultDO.returnSuccess(context.getResponse()));
//        }
//        CompletableFuture<ResultDO<Map<String, Object>>> resultFuture = SeUtils.getResultFuture(context);
//        if(resultFuture != null) {
//            resultFuture.complete(ResultDO.returnSuccess(context.getResponse()));
//        }

    }
}

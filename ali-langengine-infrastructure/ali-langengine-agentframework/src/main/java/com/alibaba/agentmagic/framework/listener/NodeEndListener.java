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

import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.listener.Listener;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 各节点结束监听器
 */
@Component
public class NodeEndListener implements Listener {

    private final static String LOG_CONFIG_KEY = "logConfig";

    @Override
    public void execute(PvmEventConstant event, ExecutionContext executionContext) {
        // TODO by xiaoxuan.lp
//        AutomateLogConfig logConfig = SeUtils.getLogConfig(executionContext);
//        if(logConfig != null) {
//            JSONObject requestJson = new JSONObject(executionContext.getRequest());
//            ProcessSystemContext processSystemContext = SeUtils.getProcessSystemContext(requestJson);
//            String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
//
//            String nodeStatus = logConfig.getNodeStatus(activityId);
//            // 更新流程状态
//            if(StringUtils.isNotBlank(nodeStatus)) {
//                processSystemContext.setProcessStatus(nodeStatus);
//            }
//            // 更新是否需要写入运行记录
//            if(!processSystemContext.getNeedInsertLog()) {
//                if(StringUtils.isBlank(logConfig.getForceLogNodeId())) {
//                    processSystemContext.setNeedInsertLog(true);
//                }else {
//                    String[] nodIdList = logConfig.getForceLogNodeId().split(",");
//                    if(Arrays.asList(nodIdList).contains(activityId)) {
//                        processSystemContext.setNeedInsertLog(true);
//                    }
//                }
//            }
//            executionContext.getRequest().put(ProcessSystemContext.KEY,processSystemContext);
//        }

    }
}

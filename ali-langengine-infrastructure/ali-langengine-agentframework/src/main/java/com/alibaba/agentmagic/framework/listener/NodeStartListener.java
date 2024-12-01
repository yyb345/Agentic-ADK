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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 各节点开始监听器
 */
@Slf4j
@Component
public class NodeStartListener implements Listener {

    private static final String ENABLE_AUTO_RECOVERY = "enableAutoRecovery";

//    @Resource
//    ProcessInstanceManager processInstanceManager;
//
//    @Resource
//    MessageProducer messageProducer;

    @Override
    public void execute(PvmEventConstant event, ExecutionContext executionContext) {
        // 如果应用开始下线，判断当前流程是否需要持久化
        // TODO by xiaoxuan.lp
//        if(ProcessSwitch.offlineStart) {
//            Map<String, String> properties = executionContext.getProcessDefinition().getProperties();
//            // 开启部署中断恢复
//            if(properties != null
//                    && StringUtils.isNotBlank(properties.get(ENABLE_AUTO_RECOVERY))
//                    && properties.get(ENABLE_AUTO_RECOVERY).equals("true")) {
//                // 持久化并发送metaq
//                ProcessInstanceDO processInstanceDO = processInstanceManager.saveProcessInstanceForPause(executionContext);
//                ProcessMessage processMessage = new ProcessMessage();
//                processMessage.setProcessDefinitionId(processInstanceDO.getProcessDefinitionId());
//                processMessage.setProcessInstanceId(processInstanceDO.getProcessInstanceId());
//                messageProducer.sendSignalProcessMsg(processMessage);
//                SeUtils.addSeProcessLog(event,executionContext);
//                executionContext.setNeedPause(true);
//            }
//        }

    }
}

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
package com.alibaba.langengine.agentframework.delegation.cotexecutor.support;

import com.alibaba.langengine.agentframework.delegation.FrameworkCotCallingDelegation;
import com.alibaba.langengine.agentframework.delegation.constants.CotCallingConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.agentframework.model.agent.domain.ComponentCallingInput;
import com.alibaba.langengine.agentframework.model.domain.FrameworkSystemContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Llm构建辅助工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class LlmBuildingUtils implements CotCallingConstant {

//    public static BaseLanguageModel buildLlm(LlmTemplateConfig llmTemplateConfig, FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation, String flag) {
//        BaseLanguageModel model = null;
//        String modelName = llmTemplateConfig.getModelName();
//        String modelKey = llmTemplateConfig.getModelApiKey();
//        if ("WhaleHttpChatModel".equals(llmTemplateConfig.getModelTemplate())) {
//            WhaleHttpChatModel llm = new WhaleHttpChatModel(modelName, delegation.getWhaleModelType(llmTemplateConfig.getModelType()), modelKey);
//            llm.setTemperature(llmTemplateConfig.getTemperature());
//            llm.setMaxTokens(llmTemplateConfig.getMaxLength());
//            llm.setMaxNewTokens(llmTemplateConfig.getMaxNewTokens());
//            llm.setTopK(llmTemplateConfig.getTopK());
//            llm.setTopP(llmTemplateConfig.getTopP());
//            llm.setAutoLlmFlag(llmTemplateConfig.getAutoLlmFlag() != null ? llmTemplateConfig.getAutoLlmFlag() : false);
//            llm.setSseInc(llmTemplateConfig.getSseInc());
//            model = llm;
//        } else if("ApiGatewayChatModel".equals(llmTemplateConfig.getModelTemplate())) {
//            ApiGatewayModelType apiGatewayModelType = Enum.valueOf(ApiGatewayModelType.class, llmTemplateConfig.getModelType().toUpperCase());
//            ApiGatewayChatModel llm = new ApiGatewayChatModel(llmTemplateConfig.getModelName(),llmTemplateConfig.getModelVersion(), apiGatewayModelType, systemContext.getEnv(),
//                    !StringUtils.isEmpty(systemContext.getApikey()) ? systemContext.getApikey() : llmTemplateConfig.getModelApiKey());
//            llm.setRequestId(systemContext.getRequestId());
//            llm.setTemperature(llmTemplateConfig.getTemperature());
//            llm.setMaxTokens(llmTemplateConfig.getMaxLength());
//            llm.setTopP(llmTemplateConfig.getTopP());
//            llm.setTopK(llmTemplateConfig.getTopK());
//            llm.setAutoLlmFlag(llmTemplateConfig.getAutoLlmFlag() != null ? llmTemplateConfig.getAutoLlmFlag() : false);
//            llm.setSseInc(llmTemplateConfig.getSseInc());
//
//            if(StringUtils.isNotEmpty(llmTemplateConfig.getSubModelName())) {
//                llm.setModelName(llmTemplateConfig.getSubModelName());
//            }
//
//            if(systemContext.getApikeyCall() != null
//                    && systemContext.getApikeyCall()
//                    && !StringUtils.isEmpty(systemContext.getApikey())) {
//                llm.setFreeCheck(false);
//            } else {
//                llm.setFreeCheck(true);
//            }
//            model = llm;
//        }else if("AppEngineChatModel".equals(llmTemplateConfig.getModelTemplate())) {
//            AppEngineModelType appEngineModelType = Enum.valueOf(AppEngineModelType.class, llmTemplateConfig.getModelType().toUpperCase());
//            AppEngineChatModel llm = new AppEngineChatModel(llmTemplateConfig.getModelName(), appEngineModelType, llmTemplateConfig.getModelApiKey());
//            llm.setModel(llmTemplateConfig.getModelName());
//            llm.setTemperature(llmTemplateConfig.getTemperature());
//            llm.setMaxTokens(llmTemplateConfig.getMaxLength());
//            llm.setTopP(llmTemplateConfig.getTopP());
//            llm.setTopK(llmTemplateConfig.getTopK());
//            llm.setAutoLlmFlag(llmTemplateConfig.getAutoLlmFlag() != null ? llmTemplateConfig.getAutoLlmFlag() : false);
//            llm.setSseInc(llmTemplateConfig.getSseInc());
//            llm.setContainFunctionCallAIMessage(llmTemplateConfig.getContainFunctionCallAIMessage());
//            model = llm;
//        } else if("IdealabOpenAIChatModel".equals(llmTemplateConfig.getModelTemplate())) {
//            IdealabOpenAIChatModel llm = new IdealabOpenAIChatModel(llmTemplateConfig.getModelApiKey());
//            llm.setModel(llmTemplateConfig.getModelName());
//            llm.setTemperature(llmTemplateConfig.getTemperature());
//            llm.setMaxTokens(llmTemplateConfig.getMaxLength());
//            llm.setTopP(llmTemplateConfig.getTopP());
////            llm.setTopK(llmTemplateConfig.getTopK());
////            llm.setAutoLlmFlag(llmTemplateConfig.getAutoLlmFlag() != null ? llmTemplateConfig.getAutoLlmFlag() : false);
//            llm.setSseInc(llmTemplateConfig.getSseInc());
//            model = llm;
//        }
//        log.info("callLLmWithFunctionCall is " + modelName + "," + modelKey + ",flag:" + flag);
//        return model;
//    }

    public static String buildSystemPrompt(FrameworkSystemContext systemContext, FrameworkCotCallingDelegation delegation, String knowledgeContext) {
        String rolePrompt = systemContext.getAgentRelation().getRolePrompt();
        if(StringUtils.isEmpty(rolePrompt)) {
            rolePrompt = SYSTEM_PROMPT;
        }
        // 单独增加工具上下文
        if(delegation.getSysPromptContainFunctionEnabled() != null
                && delegation.getSysPromptContainFunctionEnabled().equals(Boolean.TRUE)
                && !CollectionUtils.isEmpty(systemContext.getAgentRelation().getComponentList())) {
            rolePrompt += "\n\n## 工具集合\n";
            for (ComponentCallingInput component : systemContext.getAgentRelation().getComponentList()) {
                rolePrompt += "- " + component.getComponentId() + ":" + component.getComponentDesc() + ", parameters:" + JSON.toJSONString(component.getInputParams()) + "\n";
            }
        }
        // 单独增加知识库记忆上下文
        if(!StringUtils.isEmpty(knowledgeContext)) {
            rolePrompt += "\n\n## 以下是你已知的知识内容\n - " + knowledgeContext + "\n";
        }
        return rolePrompt;
    }
}

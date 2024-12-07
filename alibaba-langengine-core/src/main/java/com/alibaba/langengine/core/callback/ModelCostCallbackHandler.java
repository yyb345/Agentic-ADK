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
package com.alibaba.langengine.core.callback;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.langengine.core.callback.api.DashScopeTokenConsumptionStrategy;
import com.alibaba.langengine.core.callback.api.MinimaxTokenConsumptionStrategy;
import com.alibaba.langengine.core.callback.api.TokenConsumptionStrategy;
import com.alibaba.langengine.core.callback.api.TokenConsumptionStrategy.TokenConsumption;
import com.alibaba.langengine.core.callback.api.XingChenTokenConsumptionStrategy;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aihe.ah
 * @time 2024/3/11
 * 功能说明：用于统计下不同模型当前消耗的Token数量，后续方便进行整体统计计费
 * 仍然是用BizContext，通过BizId用于区分不同的业务；
 * 另外主要以LLMEnd作为结束统计，其它情况要注意自行统计
 *
 * 引入方式
 * <include resource="com/alibaba/langengine/core/callback/langengine-cost.xml" />
 */
@Slf4j
public class ModelCostCallbackHandler extends EmptyCallbackHandler {

    /**
     * 成本统计日志
     */
    private static final Logger COST_LOG = LoggerFactory.getLogger("llm_cost_log");

    /**
     * 统计Token消耗的策略
     */
    private static Map<String, TokenConsumptionStrategy> strategyMap = new HashMap<>(4);

    static {
        strategyMap.put("com.alibaba.langengine.model.minimax.MiniMaxLlm", new MinimaxTokenConsumptionStrategy());
        strategyMap.put("com.alibaba.langengine.model.dashscope.DashScopeLLM", new DashScopeTokenConsumptionStrategy());
        strategyMap.put("com.alibaba.langengine.model.tongyi.XingchenChatLlm", new XingChenTokenConsumptionStrategy());
    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {
        try {
            // 拿到LLM
            BaseLanguageModel llm = executionContext.getLlm();
            // 根据LLM的类型进行统计,LLM的类型比较多,要实现各自的Token获取方式
            // 如果executionContext是BizContext,可以拿到对应的BizId
            // 如果没有,直接返回
            String className = llm.getClass().getName();
            if (!strategyMap.containsKey(className)) {
                return;
            }

            TokenConsumptionStrategy strategy = strategyMap.get(className);
            TokenConsumption tokenConsumption = strategy.calculateTokenConsumption(llm, executionContext);

            if (tokenConsumption == null) {
                tokenConsumption = new TokenConsumption();
            }

            String modelGroup = llm.getClass().getSimpleName();
            String modelName = tokenConsumption.getModelName() != null ? tokenConsumption.getModelName() : "";
            int inputTokens = tokenConsumption.getInputTokens() != null ? tokenConsumption.getInputTokens() : 0;
            int outputTokens = tokenConsumption.getOutputTokens() != null ? tokenConsumption.getOutputTokens() : 0;
            int totalTokens = tokenConsumption.getTotalTokens() != null ? tokenConsumption.getTotalTokens() : 0;

            String bizId = "";
            String userId = "";
            String bizType = "";
            if (executionContext instanceof BizExecutionContext) {
                BizExecutionContext bizContext = (BizExecutionContext)executionContext;
                bizId = bizContext.getBizId() != null ? bizContext.getBizId() : "";
                userId = bizContext.getUserId() != null ? bizContext.getUserId() : "";
                bizType = bizContext.getBizType() != null ? bizContext.getBizType() : "";
            }

            // 打印日志,使用|分隔,顺序为:modelGroup|modelName|inputTokens|outputTokens|totalTokens|bizId|userId
            String logStr = String.format("%s|%s|%d|%d|%d|%s|%s|%s",
                modelGroup, modelName, inputTokens, outputTokens, totalTokens, bizId, userId, bizType);
            COST_LOG.info(logStr);
        } catch (Exception e) {
            log.error("Error occurred while processing LLM end callback", e);
        }
    }
}

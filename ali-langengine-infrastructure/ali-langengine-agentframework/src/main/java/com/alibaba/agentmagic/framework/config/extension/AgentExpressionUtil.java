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
package com.alibaba.agentmagic.framework.config.extension;

import com.alibaba.smart.framework.engine.common.expression.evaluator.ExpressionEvaluator;
import com.alibaba.smart.framework.engine.configuration.ConfigurationOption;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.ConditionExpression;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public abstract class AgentExpressionUtil {

    public static Boolean eval(ExecutionContext context, ConditionExpression conditionExpression) {
        String type = conditionExpression.getExpressionType();

        Object eval = eval( context.getRequest(),type,
            conditionExpression.getExpressionContent(),context.getProcessEngineConfiguration());

        return (Boolean)eval;
    }

    public static Boolean eval( Map<String,Object> requestContext, ConditionExpression conditionExpression, ProcessEngineConfiguration processEngineConfiguration) {
        String type = conditionExpression.getExpressionType();
        String expressionContent = conditionExpression.getExpressionContent();

        Object eval = eval(requestContext,type,
            expressionContent, processEngineConfiguration);

        return (Boolean)eval;
    }



    private static Object eval( Map<String,Object> requestContext,String type, String expression,  ProcessEngineConfiguration processEngineConfiguration) {

        ConfigurationOption configurationOption = processEngineConfiguration
            .getOptionContainer().get(ConfigurationOption.EXPRESSION_COMPILE_RESULT_CACHED_OPTION.getId());

        ExpressionEvaluator expressionEvaluator = processEngineConfiguration.getExpressionEvaluator();
        Object result = expressionEvaluator.eval(expression, requestContext, configurationOption.isEnabled());

        log.info("agentExpressionEvaluator result is {}, each param is {}",result, expression);

        return result;

    }
}

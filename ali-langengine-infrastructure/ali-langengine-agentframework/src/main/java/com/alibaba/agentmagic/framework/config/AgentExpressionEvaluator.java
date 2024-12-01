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

import com.alibaba.agentmagic.framework.utils.FrameworkUtils;
import com.alibaba.agentmagic.framework.utils.MvelUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.domain.MVELScriptMethod;
import com.alibaba.smart.framework.engine.common.expression.evaluator.ExpressionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AgentExpressionEvaluator
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Component
public class AgentExpressionEvaluator implements ExpressionEvaluator {

    MVELScriptMethod script = new MVELScriptMethod();

    /**
     * 获取object括号内key 获取以$!{ 开头，以 } 结尾的字符串
     */
    private static Pattern objectMat = Pattern.compile("\\$\\!\\{(.*?)}");

    private static final String DEFAULT_ACTIVITY_ID = "1";

    @Override
    public Object eval(String expression, Map<String, Object> vars, boolean needCached) {
        // 获取以$!{ 开头，以 } 结尾的字符串
        try{
            //如果包含$!{xxxxxx}，就用velocity表达式
            if(expression.indexOf("$!{") >= 0 && expression.indexOf("}") >= 0) {

                return FrameworkUtils.checkConditionRule(expression, vars, DEFAULT_ACTIVITY_ID);

                // 拼接velocity判断条件
//                String velocityTemplate = "#{if}(" + expression + ")true#{else}false#{end}";
//                String result = FrameworkUtils.buildInputDataValue(DEFAULT_ACTIVITY_ID, velocityTemplate, vars, null);
//                return "true".equals(result);

//                ExpressionFactory factory = new ExpressionFactoryImpl();
//                SimpleContext context = new SimpleContext();
//                if(vars != null) {
//                    for (Map.Entry<String, Object> var : vars.entrySet()) {
//                        context.setVariable(var.getKey(), factory.createValueExpression(var.getValue(), Object.class));
//                    }
//                }
//                ValueExpression valueExpression = factory.createValueExpression(context, expression, Boolean.class);
//                return valueExpression.getValue(context);
            } else {
//            Matcher mat = objectMat.matcher(expression);
//            JSONObject dataJson = new JSONObject(vars);
//            while (mat.find()){
//                String key = mat.group(1);
//                expression = expression.replace("$!{"+key+"}", SeUtils.getVariableForMvel("$!{"+key+"}",dataJson));
//            }
                Map<String, Object> outputNoteTransform = new HashMap<>();
                for (Map.Entry<String, Object> entry : vars.entrySet()) {
                    if (entry.getKey().startsWith("out_")) {
                        outputNoteTransform.put(entry.getKey().replaceAll("\\-", "_"), entry.getValue());
                    }
                }
                vars.put("sys_script", script);
                vars.putAll(outputNoteTransform);
            }
            return MvelUtils.eval(expression,vars, needCached);
        }catch (Exception e) {
            log.error("eval error,expressions:"+expression,e);
            throw new RuntimeException("表达式执行失败，请检查参数");
        }
    }

    private static Object eval2(String expression, Map<String, Object> vars, boolean needCached) {
        // 获取以$!{ 开头，以 } 结尾的字符串
        Matcher mat = objectMat.matcher(expression);
        JSONObject dataJson = new JSONObject(vars);
        while (mat.find()){
            String key = mat.group(1);
            expression = expression.replace("$!{"+key+"}", FrameworkUtils.getVariableForMvel("$!{"+key+"}",dataJson));
        }
        return MvelUtils.eval(expression,vars,  needCached);
    }

    public static void main(String[] args) {
//        String expression = "${userId == \"1\"}";
//        Map<String, Object> vars = new HashMap<>();
//        vars.put("userId", "1");
//        AgentExpressionEvaluator agentExpressionEvaluator = new AgentExpressionEvaluator();
//        Object result = agentExpressionEvaluator.eval(expression, vars, true);
//        System.out.println(result);

        String expression = "!($!{service.isNotNull($!{out_758d80a0-3772-4aa8-b880-eb56330a66ed.Intent})})";
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> res = new HashMap<>();
        res.put("Intent", "Translate");
        vars.put("out_758d80a0-3772-4aa8-b880-eb56330a66ed", res);

//        String expression = "$!{userId} > 2";
//        Map<String, Object> vars = new HashMap<>();
//        vars.put("userId", "4");
        AgentExpressionEvaluator agentExpressionEvaluator = new AgentExpressionEvaluator();
        Object result = agentExpressionEvaluator.eval(expression, vars, true);
        System.out.println(result);


//        JSONObject jsonObject = new JSONObject();
//
//        JSONObject data = new JSONObject();
//        data.put("new_record", "31231231231231231");
//
//        JSONObject record = new JSONObject();
//        record.put("system_status","d");
//
//        data.put("new_record",record);
//
//
//        JSONObject tool = new JSONObject();
//        tool.put("qtjorti", "system_status");
//        tool.put("target", Arrays.asList("a","b"));
//        tool.put("target2", "c");
//
//        jsonObject.put("data", data);
//        jsonObject.put("tool", tool);
//
////        System.out.println(eval2("1000120120123123B + (99) * 1231231231310000B}",jsonObject,false) );
//        System.out.println(eval2("equal(\"$!{tool.qtjorti}\",{\"system_status\"}) && in(\"$!{data.new_record.system_status}\",{\"$!{tool.target}\",\"$!{tool.target2}\"})", jsonObject, false));
////        System.out.println(new BigDecimal("12E1").intValue());
    }
}

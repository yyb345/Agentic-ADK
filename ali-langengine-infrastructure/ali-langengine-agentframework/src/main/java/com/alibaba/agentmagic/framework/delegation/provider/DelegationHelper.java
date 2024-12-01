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
package com.alibaba.agentmagic.framework.delegation.provider;

import com.alibaba.agentmagic.framework.delegation.constants.MessageNodeConstant;
import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.agentmagic.framework.utils.FrameworkUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.agentframework.model.domain.ContextSerialization;
import com.alibaba.smart.framework.engine.common.util.MvelUtil;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DelegationHelper extends SystemConstant {

    /**
     * 占位符
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$!\\{(.*?)}");

    /**
     * 表达式占位符
     */
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("#\\{(.*?)}#");

    private static final Set<String> NO_NEED_REPLACE_KEY_SET = new HashSet<String>() {{
        add(MessageNodeConstant.PROPER_KEY_CARD_CONFIG);
        add(MessageNodeConstant.PROPER_KEY_RESPONSE_TEMPLATE);
    }};

    /**
     * 从BpmnXml上下文获取Json属性列表
     *
     * @param executionContext
     * @return
     */
    public static JSONObject getProperties(ExecutionContext executionContext) {
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        log.info("activityId:" + activityId);
        return FrameworkUtils.getJsonPropertiesFromContext(executionContext, activityId);
    }

    /**
     * 从调用中获取上下文变量值
     *
     * @param executionContext
     * @return
     */
    public static JSONObject getRequest(ExecutionContext executionContext) {
        return new JSONObject(executionContext.getRequest());
    }

    public static JSONObject getResponse(ExecutionContext executionContext) {
        if(executionContext.getResponse() == null) {
            executionContext.setResponse(new HashMap<>());
        }
        return new JSONObject(executionContext.getResponse());
    }

    public static String replace(String actionParamJson, JSONObject requestJson) {
        // 替换为流程变量值
        StringBuffer valueResult = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(actionParamJson);
        while(matcher.find()) {
            String placeHolder = matcher.group(1);
            Object paramObj = FrameworkUtils.getObjectByExpr(placeHolder, requestJson);
            String result = "";
            if(paramObj == null) {
                result = "";
            }else if(paramObj instanceof Map) {
                result = JSON.toJSONString(paramObj);
            }else if(paramObj instanceof List) {
                result = JSONArray.toJSONString(paramObj);
            }else {
                result = String.valueOf(paramObj);
            }
            matcher.appendReplacement(valueResult, result.replace("\"","'"));
        }
        matcher.appendTail(valueResult);

        // 执行表达式
        String paramValueJson = valueResult.toString();
        StringBuffer realResult = new StringBuffer();
        Matcher expressionMatcher = EXPRESSION_PATTERN.matcher(paramValueJson);
        while(expressionMatcher.find()) {
            String expression = expressionMatcher.group(1);
            Object paramObj = MvelUtil.eval(expression,new HashMap<>(),  true);
            expressionMatcher.appendReplacement(realResult, String.valueOf(paramObj).replace("\"","'"));
        }
        expressionMatcher.appendTail(realResult);
        return realResult.toString();
    }

    public static JSONObject replaceBusinessJson(JSONObject jsonObject, JSONObject requestJson, String grayStrategyConfig) {
        for (String key : jsonObject.keySet()) {
            if (org.apache.commons.lang3.StringUtils.isEmpty(key)){
                continue;
            }
            if(isSystemKey(key)) {
                continue;
            }
            if(isNoNeedReplaceKey(key)) {
                continue;
            }
            Object obj = jsonObject.get(key);
            Object o = replaceItem(obj, requestJson, grayStrategyConfig);
            jsonObject.put(key,o);
        }
        return jsonObject;
    }

//    /**
//     * 替换占位符，当占位符里对应的key不存在时，不替换该占位符
//     * @param expr  如 xxx=$!{aaa}
//     * @param jsonObject 如 {"bbb":123}
//     * @return xxx=$!{aaa}
//     */
//    public static Object getProcessVariableIgnoreNullValue(String expr,JSONObject jsonObject, String caller) {
//        // getProcessVariable 内部使用 Velocity.evaluate 来执行占位符替换
//        // 如果占位符对应的key不存在时，占位符对应内容会被替换成空，可能有业务不需要做这种替换，
//        // 这里采用一种不是很优雅的方案，通过深拷贝扩展一个dummy json，当占位符对应的key在原始json里不存在时，会put一个k-v到dummy json里
//        // 示例：占位符=$!{aaa},  jsonObject={"bbb":123}  dummy={"aaa":"$!{aaa}","bbb":123}
//        // TODO 可以在研究一下Velocity，看看能不能在key不存在时保留占位符内容本身
//        JSONObject dummy = (JSONObject) jsonObject.clone();
//        Matcher matcher = PLACEHOLDER_PATTERN.matcher(expr);
//        while(matcher.find()) {
//            String placeHolder = matcher.group(1);
//            Object paramObj = FrameworkUtils.getObjectByExpr(placeHolder, dummy);
//            if (paramObj == null) {
//                // 如果占位符对应的值不存在，则把占位符作为value存进去
////                dummy.put(placeHolder, matcher.group());
//            }
//        }
//        return FrameworkUtils.getProcessVariable(expr, dummy, null, caller);
//    }

    public static JSONObject replaceJson(JSONObject jsonObject, JSONObject requestJson) {
        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);
            Object o = replaceItem(obj,requestJson);
            jsonObject.put(key, o);
        }
        return jsonObject;
    }

    public static JSONArray replaceArray(JSONArray jsonArray, JSONObject requestJson) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object o = replaceItem(jsonArray.get(i), requestJson);
            if(o instanceof ContextSerialization) {
                jsonArray.set(i, o.toString());
            } else {
                jsonArray.set(i, o);
            }
        }
        return jsonArray;
    }

    public static boolean isSystemKey(String key) {
        if(key.equals(SystemConstant.OUTPUT_PARAMETERS_KEY)
                || key.equals(SystemConstant.FINAL_OUTPUT_PARAMETERS_KEY)
                || key.equals(SystemConstant.INPUT_PARAMETERS_KEY)) {
            return true;
        }
        return false;
    }

    public static boolean isNoNeedReplaceKey(String key) {
        if(NO_NEED_REPLACE_KEY_SET.contains(key)) {
            return true;
        }
        return false;
    }

    public static String getSystemValue(JSONObject request, String key) {
        Map<String, Object> system = (Map<String, Object>) request.get(SYSTEM_KEY);
        if(system != null && system.get(key) != null) {
            return system.get(key).toString();
        }
        return null;
    }

    public static Boolean getSystemBoolean(Map<String, Object> request, String key) {
        Map<String, Object> system = (Map<String, Object>) request.get(SYSTEM_KEY);
        if(system != null && system.get(key) != null) {
            return (Boolean) system.get(key);
        }
        return null;
    }

    public static Boolean getSystemBooleanOrDefault(Map<String, Object> request, String key, boolean defaultValue) {
        Map<String, Object> system = (Map<String, Object>) request.get(SYSTEM_KEY);
        if(system != null && system.get(key) != null) {
            return (Boolean) system.get(key);
        }
        return defaultValue;
    }

    public static Integer getSystemIntegerOrDefault(Map<String, Object> request, String key, Integer defaultValue) {
        Map<String, Object> system = (Map<String, Object>) request.get(SYSTEM_KEY);
        if(system != null && system.get(key) != null) {
            return MapUtils.getInteger(system, key);
        }
        return defaultValue;
    }

    public static String getSystemString(Map<String, Object> request, String key) {
        Map<String, Object> system = (Map<String, Object>) request.get(SYSTEM_KEY);
        if(system != null && system.get(key) != null) {
            return (String)system.get(key);
        }
        return null;
    }

    public static Object getSystem(JSONObject request, String key) {
        Map<String, Object> system = (Map<String, Object>) request.get(SYSTEM_KEY);
        if(system != null && system.get(key) != null) {
            return system.get(key);
        }
        return null;
    }

    public static Object replaceItem(Object obj, JSONObject requestJson) {
        return replaceItem(obj, requestJson, null);
    }

    public static Object replaceItem(Object obj, JSONObject requestJson, String grayStrategyConfig) {
        if(obj instanceof JSONObject) {
            JSONObject subObj = (JSONObject) obj;
            return replaceJson(subObj, requestJson);
        }else if(obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                Object o = replaceItem(arr.get(i),requestJson, grayStrategyConfig);
                arr.set(i,o);
            }
            return arr;
        }else if(obj instanceof String) {
            String value = (String) obj;
            return FrameworkUtils.getProcessVariable(value, requestJson, grayStrategyConfig, null);
        }
        // 其他类型不处理
        return obj;
    }

    public static String replaceNewLine(String value) {
        if(value == null) {
            return null;
        }
        return value.replaceAll("\\\\n", "\n");
    }

    public static String replacePromptToVelocity(String prompt, JSONObject request) {
        // TODO 需要优化代码
//        log.info("Prompt string0:" + prompt);
        // 提取占位符{xxxxxx}变成$!{xxxxxx}
        Pattern pattern = Pattern.compile("\\{([^:\"'}\\s]+)\\}");
        Matcher matcher = pattern.matcher(prompt);
        prompt = matcher.replaceAll("\\$!{$1}");

//        log.info("Prompt string1:" + prompt);
        prompt = (String) DelegationHelper.replaceItem(prompt, request);
//        log.info("Prompt string2:" + prompt);
        return prompt;
    }

    public static void main(String[] args) {
        String prompt = "案例七:\n" +
                "[评价]:我收到了我的物品,部分损坏了,我从未以为过。令人失望。\n" +
                "输出:{abc}\n" +
                "\n" +
                "案例八:\n" +
                "[评价]:撕裂并被卡在车上的状态下到达了。产品的电缆接头也被压扁了,末端也被刮伤了。退货申请也没有,感到很难过。\n" +
                "输出:{\"一级类目\":\"商品有问题\",\"二级类目\":\"商品有划伤\"}";
        Map<String, Object> reqeust = new HashMap<>();
        String value = replacePromptToVelocity(prompt, new JSONObject(reqeust));
        System.out.println(value);
    }

    public static JSONObject preProcessActivityContext(ExecutionContext executionContext, JSONObject requestJson, JSONObject propertiesJson, String grayStrategyConfig) {
        //获取inputParameters
        String intputParameters = propertiesJson.getString(SystemConstant.INPUT_PARAMETERS_KEY);
        JSONArray realJSONArray = null;
        Map<String, Object> nodeRequest = new HashMap<>();
        if(intputParameters != null) {
            JSONArray jsonArray = JSON.parseArray(intputParameters);
            realJSONArray = DelegationHelper.replaceArray(jsonArray, requestJson);

            for (int i = 0; i < realJSONArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                for (String key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    nodeRequest.put(key, value);
                    requestJson.put(key, value);
                }
            }
        }

        // traceOutput
        if(nodeRequest.size() > 0) {
            AgentTraceHelper.traceNodeStart(executionContext, nodeRequest);
        }

        //获取业务json
        JSONObject realPropertiesJson = DelegationHelper.replaceBusinessJson(propertiesJson, requestJson, grayStrategyConfig);

        // 如果realJSONArray有实际值，将real inputParameters放入realPropertiesJson，替换掉原来的inputParameters
        if(!CollectionUtils.isEmpty(realJSONArray)) {
            realPropertiesJson.put(SystemConstant.INPUT_PARAMETERS_KEY, realJSONArray);
        }
        return realPropertiesJson;
    }

    public static void saveRequestContext(ExecutionContext executionContext, JSONObject properties,
                                      JSONObject request, JSONObject response, Object param) {
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        request.put("out_" + activityId, param);

        String finalOutputParameters = properties.getString(SystemConstant.FINAL_OUTPUT_PARAMETERS_KEY);
        if(finalOutputParameters != null) {
            JSONArray jsonArray = JSON.parseArray(finalOutputParameters);
            JSONArray realJSONArray = DelegationHelper.replaceArray(jsonArray, request);
            for (int i = 0; i < realJSONArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                for (String key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    response.put(key, value);
                }
            }
        }

        String outputParameters = properties.getString(SystemConstant.OUTPUT_PARAMETERS_KEY);
        if(outputParameters != null) {
            JSONArray jsonArray = JSON.parseArray(outputParameters);
            JSONArray realJSONArray = DelegationHelper.replaceArray(jsonArray, request);

            for (int i = 0; i < realJSONArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                for (String key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    request.put(key, value);
                }
            }
        }
    }

    public static Map<String, Object> saveFinalRequestContext(ExecutionContext executionContext) {
        JSONObject request = getRequest(executionContext);
        JSONObject response = getResponse(executionContext);
        JSONObject propertiesJson = getProperties(executionContext);
        String finalOutputParameters = propertiesJson.getString(SystemConstant.FINAL_OUTPUT_PARAMETERS_KEY);
        Map<String, Object> endResponse = new HashMap<>();
        if(finalOutputParameters != null) {
            JSONArray jsonArray = JSON.parseArray(finalOutputParameters);
            JSONArray realJSONArray = DelegationHelper.replaceArray(jsonArray, request);
            for (int i = 0; i < realJSONArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                for (String key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    endResponse.put(key, value);
                    response.put(key, value);
                }
            }
        }
        return endResponse;
    }

    public static void setMetaQInfo(Map<String, Object> invokeContext, String agentCode, String processInstanceId, String projectName) {
        if(!StringUtils.isEmpty(projectName) && "aidc-agentpaas-api".equals(projectName)) {
            String metaQTag = String.format("%s_%s", agentCode, processInstanceId);
            invokeContext.put("metaQTag", metaQTag);
            invokeContext.put("metaQTopic", "AI_COMPONENT_EXECUTE_RESULT");
        } else {
            // TODO 暂时所有，后面改成agentCode
//            String metaQTag = agentCode;
            String metaQTag = String.format("%s_%s", agentCode, processInstanceId);
            invokeContext.put("metaQTag", metaQTag);
            invokeContext.put("metaQTopic", "AI_COMPONENT_EXECUTE_RESULT_SERVERLESS");
        }
    }
}

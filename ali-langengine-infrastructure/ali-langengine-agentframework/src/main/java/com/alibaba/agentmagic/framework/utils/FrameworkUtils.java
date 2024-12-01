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
package com.alibaba.agentmagic.framework.utils;

import com.alibaba.agentmagic.framework.constants.ProcessConstant;
import com.alibaba.agentmagic.framework.delegation.constants.SystemConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.agentframework.model.AgentResult;
import com.alibaba.langengine.agentframework.model.domain.ProcessSystemContext;
import com.alibaba.smart.framework.engine.bpmn.assembly.task.ServiceTask;
import com.alibaba.smart.framework.engine.constant.ExtensionElementsConstant;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionDecorator;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElementContainer;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElements;
import com.alibaba.smart.framework.engine.model.assembly.impl.AbstractActivity;
import com.alibaba.smart.framework.engine.smart.Properties;
import com.alibaba.smart.framework.engine.smart.PropertiesElementMarker;
import com.alibaba.smart.framework.engine.smart.Value;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.util.CollectionUtils;
import com.google.common.base.Splitter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 流程工具类
 */
@Slf4j
public class FrameworkUtils {

    public static final String VELOCITY_SCRIPT_TYPE = "velocity";

    public static final String SYS_INTENT_PREFIX = "sys_intent_";

    public static final String KV_SEPARATOR = "\\u003";

    public static final String ELEMENT_SEPARATOR = "\\u004";

    private static final Splitter.MapSplitter MAP_SPLITTER =
            Splitter.on(ELEMENT_SEPARATOR)
                    .trimResults()
                    .omitEmptyStrings()
                    .withKeyValueSeparator(KV_SEPARATOR);

    private static final Joiner.MapJoiner MAP_JOINER =
            Joiner.on(ELEMENT_SEPARATOR).withKeyValueSeparator(KV_SEPARATOR);

    /**
     * 初始化velocity回调函数
     */
    static {
        Velocity.setProperty("eventhandler.invalidreferences.class", "com.alibaba.agentmagic.framework.listener.VelocityInvalidRefHandler");
    }

    public static CompletableFuture<AgentResult<Map<String, Object>>> getResultFuture(ExecutionContext context) {
        if(context != null && context.getRequest() != null
                && context.getRequest().get(ProcessConstant.RESULT_FUTURE) != null
                && context.getRequest().get(ProcessConstant.RESULT_FUTURE) instanceof CompletableFuture) {
            return (CompletableFuture<AgentResult<Map<String, Object>>>) context.getRequest().get(ProcessConstant.RESULT_FUTURE);
        }
        return null;
    }

    public static JSONObject replaceJson(JSONObject jsonObject, JSONObject requestJson) {
        JSONObject newJson = new JSONObject();
        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);
            Object o = replaceItem(obj,requestJson);
            newJson.put(key,o);
        }
        return newJson;
    }

    public static JSONArray replaceArray(JSONArray jsonArray, JSONObject requestJson) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object o = replaceItem(jsonArray.get(i),requestJson);
            jsonArray.set(i,o);
        }
        return jsonArray;
    }

    private static Object replaceItem(Object obj, JSONObject requestJson) {
        if(obj instanceof JSONObject) {
            JSONObject subObj = (JSONObject) obj;
            return replaceJson(subObj, requestJson);
        }else if(obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                Object o = replaceItem(arr.get(i),requestJson);
                arr.set(i,o);
            }
            return arr;
        }else if(obj instanceof String) {
            String value = (String) obj;
            return getProcessVariable(value, requestJson);
        }
        // 其他类型不处理
        return obj;
    }

    public static String getVariableForMvel(String expr,JSONObject jsonObject) {
        Object processVariable = getProcessVariable(expr, jsonObject);
        if(processVariable == null) {
            return "null";
        }else if(processVariable instanceof Map) {
            return StringEscapeUtils.escapeJava(JSONObject.toJSONString(processVariable));
        }else if(processVariable instanceof List) {
            return StringEscapeUtils.escapeJava(JSONObject.toJSONString(processVariable));
        }
        String result = StringEscapeUtils.escapeJava(String.valueOf(processVariable));
        // 如果大数字类型，转mvel的Bigdecimal
        if(NumberUtils.isParsable(result)) {
            return result+"B";
        }
        return result;
    }

    public static <T> T getProcessVariableFormProperties(String key,Class<T> clazz, JSONObject properties, JSONObject requestJson) {
        if(properties == null) {
            return null;
        }
        String expr = properties.getString(key);
        if(StringUtils.isBlank(expr)) {
            return null;
        }
        Object processVariable = FrameworkUtils.getProcessVariable(expr, requestJson);
        if(processVariable == null) {
            return null;
        }
        if(String.class.equals(clazz)) {
            return (T)String.valueOf(processVariable);
        }
        try {
            Method method = clazz.getDeclaredMethod("valueOf",String.class);
            return (T)method.invoke(null,String.valueOf(processVariable));
        }catch (Exception e) {
            log.error("getProcessVariableFormProperties error,key:"+key);
        }
        return null;
    }

    /**
     * 获取变量，用于mvel表达式， 转为String,并对引号转义
     * @param expr
     * @param jsonObject
     * @return
     */
//    public static String getVariableForMvel(String expr,JSONObject jsonObject) {
//        Object processVariable = getProcessVariable(expr, jsonObject);
//        if(processVariable == null) {
//            return "null";
//        }else if(processVariable instanceof Map) {
//            return StringEscapeUtils.escapeJava(JSONObject.toJSONString(processVariable));
//        }else if(processVariable instanceof List) {
//            return StringEscapeUtils.escapeJava(JSONObject.toJSONString(processVariable));
//        }
//        String result = StringEscapeUtils.escapeJava(String.valueOf(processVariable));
//        // 如果大数字类型，转mvel的Bigdecimal
//        if(NumberUtils.isParsable(result)) {
//            return result+"B";
//        }
//        return result;
//    }

    public static Object getProcessVariable(String expr,JSONObject jsonObject) {
        return getProcessVariable(expr, jsonObject, null, null);
    }

    /**
     * 解析value的值
     * @param expr
     * @param jsonObject
     * @return
     */
    public static Object getProcessVariable(String expr,JSONObject jsonObject, String grayStrategyConfig, String velocityTemplateName) {
        if(expr == null) {
            return null;
        }
        if("".equals(expr)) {
            return "";
        }

        // FIXME xiaoxuan.lp
        expr = expr.replaceAll("\\\\n", "\n");

//        if(jsonObject.get(ProcessConstant.SYSTEM) == null) {
//            jsonObject.put(ProcessConstant.SYSTEM,new JSONObject());
//        }
//        jsonObject.getJSONObject(ProcessConstant.SYSTEM).put("systemTime",System.currentTimeMillis());
//        jsonObject.getJSONObject(ProcessConstant.SYSTEM).put("systemDateStr",DateFormatUtils.format(new Date(),"yyyy-MM-dd"));
//        jsonObject.getJSONObject(ProcessConstant.SYSTEM).put("systemTimeStr",DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
//        jsonObject.put("systemTime",System.currentTimeMillis());
//        jsonObject.put("systemTimeStr",DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));

        // 同步设置
//        ConcurrentMap<String, Object> jsonObject = new ConcurrentHashMap<>();
//        jsonObject.computeIfAbsent(ProcessConstant.SYSTEM, k -> new ConcurrentHashMap<String, Object>());
//
//        ConcurrentMap<String, Object> systemMap = (ConcurrentMap<String, Object>) jsonObject.get(ProcessConstant.SYSTEM);
//        long currentTime = System.currentTimeMillis();
//        String currentDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
//        String currentTimeStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//
//        systemMap.put("systemTime", currentTime);
//        systemMap.put("systemDateStr", currentDateStr);
//        systemMap.put("systemTimeStr", currentTimeStr);
//
//        jsonObject.put("systemTime", currentTime);
//        jsonObject.put("systemTimeStr", currentTimeStr);

        if(expr.startsWith("$!{") && expr.endsWith("}") && StringUtils.countMatches(expr,"$!{") == 1) {
            // 单变量 TODO by xiaoxuan.lp
//            if(ProcessSwitch.enableJsonPath) {
//                return getObjectByJsonPath(expr.substring(3,expr.length()-1), jsonObject);
//            }
            return getObjectByExpr(expr.substring(3, expr.length() - 1), jsonObject);
        } else if(StringUtils.countMatches(expr,"$!{") > 0) {
            // 变量与常量的组合
            // 这里的velocityTemplateName是给velocity做替换的时候用，用于VelocityInvalidRefHandler回调时作为key来区分调用者
            if (velocityTemplateName == null) {
                velocityTemplateName = "getProcessVariable";
            }
            return buildInputDataValue(velocityTemplateName, expr,jsonObject, grayStrategyConfig);
        } else {
            // 常量
            return expr;
        }
    }
    private static void processKey(String value, String startDelimiter, String endDelimiter, Map<String, Object> requestParam, Map.Entry<String, Object> eachParamPair) {
        String key = StringUtils.substringBetween(value, startDelimiter, endDelimiter);
        if (key != null) {
            String[] keys = key.split("\\.");
            if (keys.length > 1) {
                Map<String, Object> currentMap = requestParam;
                /*
                 * 12345.key1
                 * {
                 *    "12345": {
                 *      "key1": "value"
                 *    }
                 * }
                 *
                 */
                for (int i = 0; i < keys.length - 1; i++) {
                    String currentKey = keys[i];
                    if (currentMap.containsKey(currentKey)) {
                        Object nextMap = currentMap.get(currentKey);
                        if (nextMap instanceof Map) {
                            currentMap = (Map<String, Object>) nextMap;
                        } else {
                            log.info("get current Map & return.", currentKey);
                            return; // 如果中间的键不是Map，直接返回
                        }
                    } else {
                        log.info("can not find key {} in currentMap", currentKey);
                        return; // 如果中间的键不存在，直接返回
                    }
                }
                String lastKey = keys[keys.length - 1];
                if (currentMap.containsKey(lastKey)) {
                    eachParamPair.setValue(currentMap.get(lastKey));
                } else {
                    String pairValue = (String) eachParamPair.getValue();
                    if(pairValue.contains("$!{service.escape(")) {
                        eachParamPair.setValue(null);
                    }
                }
            } else {
                if(requestParam.containsKey(key)) {
                    eachParamPair.setValue(requestParam.get(key));
                } else {
                    eachParamPair.setValue(null);
                }
            }
        }
    }

    public static Boolean checkConditionRule(String expression) {
        return checkConditionRule(expression, null);
    }

    public static Boolean checkConditionRule(String expression, Map<String, Object> requestParam) {
        return checkConditionRule(expression, requestParam, "");
    }

    public static Boolean checkConditionRule(String expression, Map<String, Object> requestParam, String activityId) {
        // 拼接velocity判断条件
        String velocityTemplate = "#{if}(" + expression + ")true#{else}false#{end}";
        String result = FrameworkUtils.buildInputDataValue(activityId, velocityTemplate, requestParam, null);
        return "true".equals(result);
    }

    public static String buildInputDataValue(String activityId,String inputData, Map<String, Object> requestParam, String grayStrategyConfig) {
        String agentCode = null;
        String scriptType = null;
        if(requestParam != null && requestParam.get(ProcessConstant.SYSTEM) != null) {
            Map<String, Object> systemParams = (Map<String, Object>)requestParam.get(ProcessConstant.SYSTEM);
            if(systemParams.get(SystemConstant.AGENT_CODE_KEY) != null) {
                agentCode = (String) systemParams.get(SystemConstant.AGENT_CODE_KEY);
            }
            if(systemParams.get(SystemConstant.SCRIPT_TYPE_KEY) != null) {
                scriptType = (String) systemParams.get(SystemConstant.SCRIPT_TYPE_KEY);
            }
        }

        if(inputData == null) {
            return "";
        }
        String outputData = inputData;
        try {
            log.info("json string before:" + inputData + ",agentCode:" + agentCode);
//            log.info("buildInputData agentCode {}", agentCode);

            if (inputData.indexOf("#{if}") < 0) {
                //先把#号进行转成其他字符，避免被velocity注释掉
                inputData = inputData.replaceAll("#", "@@@");
                VelocityContext velocityContext = new VelocityContext(requestParam);
                velocityContext.put("service", new VelocityInnerFunction(requestParam));
                boolean safisfy = GrayStrategyUtils.staifyGrayNotVelocity(grayStrategyConfig, agentCode);
                boolean allSafisfy = GrayStrategyUtils.staifyAllNotVelocity(grayStrategyConfig);
                boolean assignSafisfy = (scriptType == null || !VELOCITY_SCRIPT_TYPE.equals(scriptType));
                log.info("buildInputDataValue agentCode:" + agentCode + ", safisfy:" + safisfy + ", allSafisfy:" + allSafisfy + ", assignSafisfy:" + assignSafisfy);
                if(allSafisfy || safisfy || assignSafisfy) {
                    try {
                        if (inputData.trim().startsWith("[")) {
                            // 解析为 JSON 数组
                            log.info("input data parse as json array : {}", inputData);
                            JSONArray paramsArray = JSON.parseArray(inputData);
                            for (int i = 0; i < paramsArray.size(); i++) {
                                JSONObject eachParamObject = paramsArray.getJSONObject(i);
                                processJSONObject(eachParamObject, requestParam);
                            }
                            outputData = paramsArray.toJSONString();
                        } else {
                            // 解析为 JSON 对象
                            log.info("input data parse as json object: {}", inputData);
                            JSONObject paramsObject = JSON.parseObject(inputData);
                            processJSONObject(paramsObject, requestParam);
                            outputData = paramsObject.toJSONString();
                        }
                        log.info("gson json string success after: {}", outputData);
                    } catch (Throwable e) {
                        log.error("buildInputDataValue error", e);
                        StringWriter stringWriter = new StringWriter();
                        Velocity.evaluate(velocityContext, stringWriter, activityId, inputData);
                        outputData = stringWriter.toString();
                        log.info("gson json string error after:" + outputData);
                    }
                } else {
                    StringWriter stringWriter = new StringWriter();
                    Velocity.evaluate(velocityContext, stringWriter, activityId, inputData);
                    outputData = stringWriter.toString();
                }
                outputData = outputData.replaceAll("@@@", "#");
            } else {
                VelocityContext velocityContext = new VelocityContext(requestParam);
                velocityContext.put("service", new VelocityInnerFunction(requestParam));
                StringWriter stringWriter = new StringWriter();
                Velocity.evaluate(velocityContext, stringWriter, activityId, inputData);
                outputData = stringWriter.toString();
            }
        } catch (Throwable e) {
            log.error("velocity error:" + agentCode, e);
            try {
                VelocityContext velocityContext = new VelocityContext(requestParam);
                velocityContext.put("service", new VelocityInnerFunction(requestParam));
                StringWriter stringWriter = new StringWriter();
                Velocity.evaluate(velocityContext, stringWriter, activityId, inputData);
                outputData = stringWriter.toString();
            } catch (Throwable ex) {
                log.error("retry velocity error:" + agentCode, ex);
            }
        }
        return outputData;
    }

    /**
     * 处理单个JSONObject的键值对
     *
     * @param paramsObject JSON对象
     * @param requestParam 请求参数
     */
    private static void processJSONObject(JSONObject paramsObject, Map<String, Object> requestParam) {
        Iterator<Map.Entry<String, Object>> iterator = paramsObject.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> eachParamPair = iterator.next();

//                            if(eachParamPair.getValue() != null && !(eachParamPair.getValue() instanceof String)) {
//                                // TODO 后面可以嵌套object去解析$!{xxx}
//                                continue;
//                            }

            String value = (String) eachParamPair.getValue();

            if (value.contains("$!{service.escape(")) {
                processKey(value, "$!{service.escape($!{", "})}", requestParam, eachParamPair);
            } else {
                String key = StringUtils.substringBetween(value, "{", "}");
                if(key == null){
                    // key is 123
                    continue;
                }else{
                    key = key.split("\\.")[0];
                    if (requestParam.containsKey(key)) {
                        processKey(value, "{", "}", requestParam, eachParamPair);
                    } else {
                        // key has no value, remove the key
                        iterator.remove();
                    }
                }

            }
        }
    }


    public static void main(String[] args) {
//        String value = "## Role\nYou are an intent recognition expert, responsible for recognizing the User's intent from the conversation between the User and Assistant.\n";

//        String value = "{\"country\":\"\",\"orig_price\":\"\",\"image_names\":\"\",\"image_url\":\"\",\"product_attributes\":\"\",\"language\":\"\",\"need_title\":\"\",\"product_title\":\"\",\"price_interest_text\":\"\",\"logistics_interest_text\":\"\",\"disc_price\":\"\",\"product_id\":\"\",\"need_sell_point\":\"\",\"pcate_category\":\"\",\"product_description\":\"16\" 41cm Emulsion Scoop Coater Silk Screen Printing Sizing Scrape Coating\",\"output_image_num\":\"\",\"pcate_leaf_name\":\"\"}";

        String str = "{\"apiCode\":\"PPC_DPA_TPP_sellpoint_image\",\"apiVersion\":\"1.0.0\",\"paramJson\":\"{\\\"country\\\":\\\"CL\\\",\\\"orig_price\\\":\\\"\\\",\\\"image_names\\\":\\\"S6616ca98bbe749e69eff3e950c6b382dn.jpg,S98f3b97ad0a1456c882ee03817f112ccr.jpg,S6fa75004ed0d4a06a80366efacacd2a2J.jpg,Sd49110d0a3d2446a8ff9379c4dbc4e80l.jpg,S230852b1dce340afa12fc63eb26597ea0.jpg,S7c27ccd6cf444259a8b73d9e9d04751dq.jpg\\\",\\\"image_url\\\":\\\"https://ae01.alicdn.com/kf/S6616ca98bbe749e69eff3e950c6b382dn.jpg\\\",\\\"product_attributes\\\":\\\"Connection method:Zigbee / wifi;Working mode:jog, self-locking, interlocking;Remote Control frequency:433MHz;Three control methods:WiFi Control, Remote Control and Manual Operation;Brand Name:Scimagic-RC;Camera Equipped:No;Model Number:WIFI-RF-30A;Name6:20a smart switch;Name7:Smart switch wifi 20a;Name4:30a relay wifi for ac;Name5:wifi relay 30a 220v;Name2:30a wifi relay;Name3:220v wifi 30a relay;Product Name:Wifi or zigbee Smart Switch 30a;Name1:30a smart breaker;Compatibility:All Compatible;Name8:Smart switch 20a wifi;Name9:wifi smart switch 20a;Max remote paired:5pcs;Communication method:Wi-Fi,RF,Zigbee;Max.Current:4000W 30A / 6600W 30A;Input voltage:AC85-250V 110V 220V 230V;Origin:CN(Origin);Remote Control Distance:50m(wild);Certification:CE;APP:Tuya/Smart Life;State of Assembly:Ready-to-Go;NOTE:6600W DOES NOTE Support RF function\\\",\\\"language\\\":\\\"es_ES\\\",\\\"need_title\\\":\\\"Y\\\",\\\"product_title\\\":\\\"Tuya Smart Switch WIFI / ZIGBEE 6600W / 4000W 30A 20A Relay Module Smart Life Wireless Remote Control Breaker Work with Alexa\\\",\\\"price_interest_text\\\":\\\"\\\",\\\"logistics_interest_text\\\":\\\"Envío gratis\\\",\\\"disc_price\\\":\\\"\\\",\\\"product_id\\\":\\\"1005005457224603\\\",\\\"need_sell_point\\\":\\\"Y\\\",\\\"pcate_category\\\":\\\"Consumer Electronics\\\",\\\"product_description\\\":\\\"Purchase tips*Zigbee 30A 6600W needs to be used together with Zigbee gateway*6600W switch without RF function*4000W Switch with RF functionHIGH POWER Max.Current 4000W / 30A wifi Smart SwitchProduct Application AreaFactory \\\\ Farm\\\\\\nOffice\\\\ Instrument\\\\\\nVentilation \\\\ Pump \\\\ Motor \\\\ Remote control garage door \\\\\\nRemote control house door \\\\ Remote control Lamp \\\\ Remote control curtains \\\\ Remote control Gate and other remote control deviceTuya smart\\nInching/ Self-locking WIFI Wireless Smart Home Switch Remote Control with Amazon AlexaThis product is a wireless switch supports inching /self-locking mode. Users can add the device to the APP Smart life in order to remotely control connected home appliances or devices. In self-locking mode, customers can remotely turn on/off connected devices immediately. When in inching mode, customers can have two wiring ways to select, and you can set the inching time in APP .WIFi control model, no need to program. No matter where you are, you only need to download a APP to connect the wifi and control the cloud.RF mode , you can control the board with RF 433 remote controller.Receiver Description:*Voltage: AC85-250V 50/60HzMax.Current:4000W 6600W/ 30A *WIFI frequency:2.4G. IEEE 802.11 b/g/n*Remote control frequency: 433MHz*Battery no included in the remote controlThis receiver will have a detailed instruction manual, please rest assured to buy. I will send the package within 18 hours, and I will do the best service.Preparation before use:1.Your smart\\nphone or tablet has connected to a 2.4G WiFi with internet 2.Turning on bluetooth helps with the connection 3.not allow to hide wifi (SSID)4.do not set \\\\\\\"not allow Wi-Fi squatter\\\\\\\" or MAC address limits on routersThe intelligent switch is wired and energized.Observe the green LED.If the green LED fast blinks(Blink twice a second),Press the button for 7s until the green LED fast blinks(Blink twice a second).Note:A receiver can be configured with 8 remote controllers at most, and the first one will be overwritten after the 9th one is added.Features:*Supports WiFi network. Supports numerous WiFi smart switches on one smart phone.*Supports status tracking: device status timely provided to the Tuya / Smart life.*Supports remotely turn on or off connected appliances, for example light, electric kettle, electric rice cooker, electric curtain, etc.*Supports max 8 enabled scheduled/countdown/loop timers for each device.*Voice Control: Compatible with \\\\\\\"Amazon\\\\\\\" Alexa, for Google Home/Nest, .Supported Functions Include:*Remote turn on/off.*Timing schedules.*Device sharing.*Group management.*Works with Amazon Echo, Echo Dot, Amazon Tap.*Works with Google Home.*Compatible with Amazon Alexa.Remote control code:1.Power off the smart switch for 5seconds2.Power the smart switch3.Press immediately (within 5 seconds) after energizing the switch more Three clicks on the remote control button (need to ptess that with that),the red LED flashes 2 to indicate the success of the remote control learning code .If there is no flicker,please repeat the above operationAPP Remote ControlJust tap on Tuya/Smartlife App to turn on a light anytime and anywhere.Timer and Countdown SettingControl your devioes at will to automatically turn on/off.Support RF function433mhz Learning Code RFFree Your Hands with VoiceFamily SharingShare the control with family memberEnjoy the convenience and happiness togetherNote:All remote controls do not contain batteries\\\",\\\"output_image_num\\\":\\\"1\\\",\\\"pcate_leaf_name\\\":\\\"Automation Modules\\\"}\",\"scene\":\"test\"}";
        String inputData ="{\"productTitle\":\"$!{productTitle}\",\"marketScene\":\"$!{makeScene}\",\"targetLanguage\":\"$!{targetLanguage}\",\"generateCounts\":\"1\",\"sourceLanguage\":\"$!{sourceLanguage}\",\"productDescription\":\"$!{productDescription}\",\"productCategory\":\"$!{service.escape($!{productCategory})}\"}";
        String requestStr = "{\"targetLanguage\":\"th\",\"log_node_start\":{\"activityId\":\"start\",\"activityName\":\"start\",\"activityType\":\"StartEvent\",\"agentCode\":\"199731151966179328\",\"componentAsync\":false,\"executeTime\":0,\"processDefineId\":\"empty_proc_pre_2494_aeefc9f615e04a83\",\"processDefineVersion\":\"5\",\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"response\":\"{}\",\"startTime\":1720152641411,\"stream\":false,\"success\":true},\"log_node_a51c68fe-a4b2-4e8d-a425-2659374dd05c\":{\"activityId\":\"a51c68fe-a4b2-4e8d-a425-2659374dd05c\",\"activityName\":\"营销文案生成\",\"activityType\":\"com.alibaba.agentmagic.core.delegation.ToolCallingDelegation\",\"agentCode\":\"199731151966179328\",\"componentAsync\":false,\"processDefineId\":\"empty_proc_pre_2494_aeefc9f615e04a83\",\"processDefineVersion\":\"5\",\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"startTime\":1720152641411,\"stream\":false},\"systemTimeStr\":\"2024-07-05 12:10:41\",\"systemTime\":1720152641411,\"_$_smart_engine_$_latchWaitTime\":180000,\"productCategory\":[\"aaa\",\"bbb\"],\"processSystemContext\":{\"hasRecord\":false,\"lockKeyList\":[],\"needInsertLog\":false,\"nodeCallback\":{},\"nodeRetry\":{},\"nodeSubCallback\":{},\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"retryTimes\":0},\"system\":{\"globalVariables\":[],\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"systemDateStr\":\"2024-07-05\",\"debug\":\"false\",\"traceOutput\":true,\"agentCode\":\"199731151966179328\",\"history\":[],\"systemTimeStr\":\"2024-07-05 12:10:41\",\"offlineBatch\":false,\"systemTime\":1720152641411,\"componentAsync\":false,\"promptInfo\":{},\"apikeyCall\":false},\"makeScene\":\"General\",\"sourceLanguage\":\"en\",\"productDescription\":\"1\",\"out_start\":{}}";
        // String requestStr = "{\"targetLanguage\":\"th\",\"log_node_start\":{\"activityId\":\"start\",\"activityName\":\"start\",\"activityType\":\"StartEvent\",\"agentCode\":\"199731151966179328\",\"componentAsync\":false,\"executeTime\":0,\"processDefineId\":\"empty_proc_pre_2494_aeefc9f615e04a83\",\"processDefineVersion\":\"5\",\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"response\":\"{}\",\"startTime\":1720152641411,\"stream\":false,\"success\":true},\"log_node_a51c68fe-a4b2-4e8d-a425-2659374dd05c\":{\"activityId\":\"a51c68fe-a4b2-4e8d-a425-2659374dd05c\",\"activityName\":\"营销文案生成\",\"activityType\":\"com.alibaba.agentmagic.core.delegation.ToolCallingDelegation\",\"agentCode\":\"199731151966179328\",\"componentAsync\":false,\"processDefineId\":\"empty_proc_pre_2494_aeefc9f615e04a83\",\"processDefineVersion\":\"5\",\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"startTime\":1720152641411,\"stream\":false},\"systemTimeStr\":\"2024-07-05 12:10:41\",\"systemTime\":1720152641411,\"_$_smart_engine_$_latchWaitTime\":180000,\"productCategory\":[\"aaa\",\"bbb\"],\"productTitle\":\"mocktitle\",\"processSystemContext\":{\"hasRecord\":false,\"lockKeyList\":[],\"needInsertLog\":false,\"nodeCallback\":{},\"nodeRetry\":{},\"nodeSubCallback\":{},\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"retryTimes\":0},\"system\":{\"globalVariables\":[],\"processInstanceId\":\"3ef0d1a2-de6e-40de-a2c6-50fe245da3b2\",\"systemDateStr\":\"2024-07-05\",\"debug\":\"false\",\"traceOutput\":true,\"agentCode\":\"199731151966179328\",\"history\":[],\"systemTimeStr\":\"2024-07-05 12:10:41\",\"offlineBatch\":false,\"systemTime\":1720152641411,\"componentAsync\":false,\"promptInfo\":{},\"apikeyCall\":false},\"makeScene\":\"General\",\"sourceLanguage\":\"en\",\"productDescription\":\"1\",\"out_start\":{}}";
        // 将requestStr通过JSON转为Map
        Map<String, Object> map = JSON.parseObject(requestStr, new TypeReference<Map<String, Object>>(){});
        // buildInputDataValue("mockid", inputData, map);
//        str = str.replace("\\", "");
        JSONObject jsonObject = JSON.parseObject(str);

        System.out.println(JSON.toJSONString(jsonObject));

//        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
//                .add("sourceLanguage", request.getSourceLanguage())
//                .add("targetLanguage", request.getTargetLanguage())
//                .add("excludingProductArea",BooleanUtils.isFalse(request.isIncludingProductArea()))
//                .add("useEditor",BooleanUtils.isTrue(request.getUseImageEditor()))
//                .add("imageUrl", request.getImageUrl());
//        gatewayRequest.setParamJson(jsonObjectBuilder.build().toString());


//        String value = "##  ### 一、升级后支持的特性\\n\\n（1）货币类统一成com.alibaba.global.money.Money类，支持AE场景，ILocalizedMonetaryService返回依然是MonetaryAmount，只是实现类变了，不影响原先的使用，不需要更改代码<br />（2）货币展示支持不同场景的定制（比如：Lazada泰国站点，导购链路的系统，将不展示.00，去掉分，而交易链路依然展示.00，方案是只要在工程目录下新增g11n.properties文件，添加配置scenario=shopping-guide即可，无需修改代码）<br />（3）丰富区域/国家、币种、语言等国际化信息，以支持AE场景，并加入了AE的汇率服务<br />        - 区域/国家，由原来11个，增加至250个<br />        - 币种，由原来11个，增加至153个<br />        - 语言，由原来20个，增加至50个\\n\\n注：所谓G11n = Globalization, i18n = International, L10n = Localization;  G11n = i18n + L10n;<br />比如新加坡币，国际化展示为“S$”，本地化展示为“$”；即满足AE的国际化场景，也满足Lazada本对本的定制\n" +
//                "##  ### 五、启动报错Load site packages failed! Missing packages of tenantIds\\n1）问题现象\\n\\nAE的应用多支持了一个F1的租户，启动时报错Load site packages failed! Missing packages of tenantIds。<br />![8957407B-D543-48A6-BB07-620B465A7066.png](https://intranetproxy.alipay.com/skylark/lark/0/2021/png/234517/1615456817049-8c442fe2-ed54-4c47-a2dd-da2975470b27.png#height=460&id=c0Jw7&originHeight=460&originWidth=3338&originalType=binary&ratio=1&size=618128&status=done&style=none&width=3338)<br />2）问题原因：\\n\\n应用配置支持租户的方式有两种，第一是通过supported.oeIds，声明支持运营实体下的所有租户；第二是通过supported.tenantIds，声明支持的租户列表。当两种方式同时配置时，取**租户交集**。<br />示例：\\n```\\n# 方式1：支持AE下的所有租户\\nsupported.oeIds=AE\\n\\n# 方式2：支持的AE_GLOBAL租户\\nsupported.tenantIds=AE_GLOBAL\\n```\\n\\n运营实体和租户间的关系是中心化的配置，不需要每个应用单独配置，配置数据存储在diamond，dataId=base.config，group=global.tenant。\\n\\nAE应用有些是通过supported.oeIds=AE的方式来声明支持AE_GLOBAL租户，以前AE下只有AE_GLOBAL一个租户，所有这种配置方式也没有问题，但随着业务发展，AE会有新增其他租户，例如AE_F1。这种配置方式会导致AE应用需要额外支持AE_F1租户，会引发以下问题：\\n\\n- AE应用需要额外支持AE_F1租户，这不是AE应用希望发生的。\\n- 多租户框架在3.0.0以后引入站点包研发模式，如果应用支持的租户没有对应的站点包，应用启动会报错：\\n\\n![8957407B-D543-48A6-BB07-620B465A7066.png](https://intranetproxy.alipay.com/skylark/lark/0/2021/png/234517/1615456817049-8c442fe2-ed54-4c47-a2dd-da2975470b27.png#height=460&id=lFRWc&originHeight=460&originWidth=3338&originalType=binary&ratio=1&size=618128&status=done&style=none&width=3338)\\n\\n3）解决方案\\n\\n检查应用启动后的/home/admin/logs/satellite/boot.log文件，是否有supported.oeIds=AE。<br />![0A21C15F-33BE-4BF8-9F68-916784621C19.png](https://intranetproxy.alipay.com/skylark/lark/0/2021/png/234517/1615457047585-c7ff3790-0486-4d9e-a6e4-5e290855b522.png#height=182&id=E6Iof&originHeight=742&originWidth=2804&originalType=binary&ratio=1&size=470178&status=done&style=none&width=686)\\n\\n添加supported.tenantIds=AE_GLOBAL配置，supported.oeIds后面版本会废弃。\n" +
//                "##  ### 配置优先级\\n| 配置项 | 范围 | 配置形式 | 优先级（越大越高） | 加载类 | 说明 |\\n| --- | --- | --- | --- | --- | --- |\\n| dataId:<br />satellite.properties<br />groupId:<br />DEFAULT | 国际化全部应用 | Diamond | 0 | com.alibaba.global.satellite.config.SatellitePropertiesManager | 多站点全局默认配置，不只是多租户，比如g11n,日志，多租户等 |\\n| satellite.properties | 单应用 | classpath根路径配置文件，支持profile后缀 | 1 | com.alibaba.global.satellite.config.SatellitePropertiesManager | 单个应用的多站点全局配置，配置内容。 |\\n| dataId:{appName}:satellite.properties<br />groupId:DEFAULT | 单应用 | Diamond | 2 | com.alibaba.global.satellite.config.SatellitePropertiesManager | 单个应用单个单元维度的多站点全局配置，配置内容。 |\\n| landlord.properties | 单应用 | classpath根路径配置文件，支持profile后缀 | 3 | com.alibaba.global.landlord.admin.LandlordPropertiesLoader | 单个应用的多站点全局配置，配置内容。 |\\n| dataId:{appName}:landlord.properties<br />groupId:DEFAULT | 单应用 | Diamond | 4 | com.alibaba.global.landlord.admin.LandlordPropertiesLoader | 单个应用单个单元维度的多站点全局配置，配置内容。 |\\n| dataId:{appName}<br />\\u0000groupId:env-deploy.properties | 单应用<br />这里定义了某个环境从哪里加载多租户配置（diamond的dataid/groupid或者本地properties文件） | Diamond | 5 | com.alibaba.global.common.utils.EnvUtils | 是应用启动文件，用来指定当前环境支持哪些租户，以及该环境一些差异化的租户配置以及应用配置。但是env-deploy.properties的diamond配置，可以外部关联静态文件或者diamond的动态文件，并且该配置只会在应用启动加载，不支持动态热替换。<br />env-deploy.properties实例：<br />voyager_sg52-stagingg4.import.landlord.diamond.dataId=global-trade-center-s\\\\\\\\:satellite.propertiesvoyager_sg52-stagingg4.import.landlord.diamond.groupId=lazada<br />voyager_sg52-stagingg4.import.spring.diamond.dataId=com.alibaba.global.trade\\\\\\\\:app.properties<br />voyager_sg52-stagingg4.import.spring.diamond.groupId=lazada<br />voyager_sg52-stagingg4.machineGroups=lazada-trade-ns-s_lazada_sg_2_sg52-stagingg4_prehost<br />voyager_sg52-stagingg4.supported.tenantIds=LAZADA_VN,LAZADA_PH,LAZADA_SG,LAZADA_MY,LAZADA_TH<br />voyager_sg52-stagingg4.units=lazada-sg-2-pre |\\n| tenant/{tenantId}.properties<br />\\u0000 | 单应用下单个租户 | classpath根路径配置文件，支持profile后缀 | 6 | com.alibaba.global.landlord.admin.LandlordPropertiesLoader | 单个应用的单个租户全局配置，配置内容。 |\\n| 来自spring的配置<br />load.spring.properties=on | 单应用 | classpath根路径配置文件，支持profile后缀 | 7 | com.alibaba.global.landlord.admin.LandlordPropertiesLoader | 从spring的配置中加载组合的配置 |\n" +
//                "##  ### 三、ScheduleX2.0\\n\\n#### 1、配置出现错误\\n![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2020/png/234517/1587293672992-8d4f21e8-a5f3-4a1a-9cb6-5780aab098e4.png#height=155&id=TUOEm&originHeight=310&originWidth=2202&originalType=binary&ratio=1&size=154547&status=done&style=none&width=1101)<br />landlord-sdk 3.0.6以及其以下的版本读取ScheduleX2.0配置有bug。升级landlord-sdk版本解决：[https://yuque.antfin-inc.com/satellite/sa/hmlukp](https://yuque.antfin-inc.com/satellite/sa/hmlukp)\\n\\n正确的配置示例：\\n```\\n# 某个租户的定制值，需要加上租户前缀\\nLAZADA_SG.global.landlord.schedulerx2.groupId=xxxxx\\nLAZADA_SG.global.landlord.schedulerx2.appKey=xxxxxx\\n\\n# 所有租户的默认值\\nglobal.landlord.schedulerx2.groupId=xxxxx\\nglobal.landlord.schedulerx2.appKey=xxxxxx\\nglobal.landlord.schedulerx2.reusable=true\\nglobal.landlord.schedulerx2.domainName=xxxxx\\n```\n" +
//                "##  ### 一. HSF\\n\\n#### 1、新单元调老单元服务\\n\\n新单元：rg-sg,rg-id(云上), lazada-sg-2,lazada-id-2(云下)<br />老单元：lazada和daraz之前是每个站点对应一个单元的，如LAZADA_SG对应单元是lazada-sg\\n\\n```\\n#默认调用新单元的服务\\nglobal.landlord.hsf.consumer.default.modes=RAW\\n#需要调用老单元的服务列表\\nglobal.landlord.hsf.consumer.custom[0].list=com.lazada.promotion.api.facade.PromotionWriteFacade,com.lazada.promotion.api.facade.PromotionReadFacade\\n# SOURCE_RAW 是指需要调到老单元\\nglobal.landlord.hsf.consumer.custom[0].modes=SOURCE_RAW\\n```\\n\\n若没有新老单元的概念，可直接用HSF指定单元调用的方式：\\n```java\\nHSFApiConsumerBean consumer = new HSFApiConsumerBean();\\nconsumer.setInterfaceClass({service to be consumed});\\nconsumer.setConfigserverCenter(Lists.newArrayList(EnvUtils.isPre() ? \\\"lazada-sg-pre\\\" : \\\"lazada-sg\\\")); \\nconsumer.init();\\nObject facade = consumer.getObject();\\n```\n" +
//                "##  ### 二. TDDL\\n\\n#### 1、TStatement抛NPE\\n![屏幕快照 2020-04-14 下午6.15.42.png](https://intranetproxy.alipay.com/skylark/lark/0/2020/png/234517/1586859453690-3a3762c1-a1bb-45e8-bef3-aa425acb8499.png#height=218&id=lF2ae&originHeight=218&originWidth=1970&originalType=binary&ratio=1&size=228555&status=done&style=none&width=1970)<br />若分库分表，检查DB实例所在单元的prectrl上是否有配置分库分布规则。\\n\\n若非分库分表的，需要加上配置：\\n```\\nglobal.landlord.tddl.dataSource.{sprin\u7684beanName}.sharding=false\\n```";
//        Map<String, Object> requestParam = new HashMap<>();
//        Map<String, Object> res = new HashMap<>();
//        res.put("Intent", "Translate");
//        requestParam.put("out_758d80a0-3772-4aa8-b880-eb56330a66ed", res);
//
//        value = value.replaceAll("#", "@@@");
//        VelocityContext velocityContext = new VelocityContext();
//        StringWriter stringWriter = new StringWriter();
//        velocityContext.put("service", new VelocityInnerFunction(requestParam));
//        Velocity.evaluate(velocityContext,stringWriter, "", value);
////        System.out.println(stringWriter);
//        value = stringWriter.toString().replaceAll("@@@", "#");
//        System.out.println(value);


//        String inputData = "test.test.{category input}hello.";
//        // 提取占位符{xxxxxx}变成$!{xxxxxx}
//        Pattern pattern = Pattern.compile("\\{([^}\\s]+)\\}");
//        Matcher matcher = pattern.matcher(inputData);
//        inputData = matcher.replaceAll("\\$!{$1}");
//        System.out.println(inputData);
////        String inputData = "SYSTEM: I want you to act as an e-commerce merchandising expert. You should give helpful, harmless, honest answers that fit your role and strictly follow the user's instruction. Your task is to generate a concise and structured product title based on the following content:\n- [rules]: the length, spelling, and other content format constraints for the generated product title.\n- [title structure schema]: the provided schema for each category to structure the title.\n- [product information]: the product Category, and the product original title.\n- [Category Relation]: it shows what main categories of each product Category in #[product information] belonging to.\n  --##Electronics：electronics accessories、audio、mobiles、tablets、smart devices、cameras drones、televisions、videos、large appliances、moniitors、printers、data storage、computers、laptops\n  --##Fashion: women’s shoes and clothing、men’s shoes and clothing、sports shoes and clothing、watches、sunglasses、jewellery、bags、travel、kids’ shoes、kids’ clothing\n  --##General Merchandise: motors、tools、home improvement、sports、outerdoors、lighting、decor、stationery、craft、ketchen、dining、outdoor、garden、furniture、organization、bedding、bath、media、music、books、laundry、cleaning equipment；\n  --##Fast-Moving Consumer Goods: beauty、toys、games、groceries、mother&baby、health、pet supplies、household supplies\n\n#[rules]\n1. Ensure the title is descriptive and appealing to potential customers. \n2. Each product title must be in English.\n3. The generated title should be adhere to #[title structure schema] based on the main categories of [Category] belonging to.\n4. Tailor each product title to reflect key selling points and product specifics.\n5. Avoid any language that could be considered misleading or inaccurate.\n6. Keep the product title underneath 50 characters wherever possible to maximize impact and readability.\n\n#[title structure schema]\n##Electronics:\nStructure: Category + Brand + Model + Attribute\nExample Input: \"Category\": \"televisions\", \"Brand\": \"Acme\", \"Model\": \"UltraView 3000\", \"Attribute\": \"4K HDR Smart TV\" \nExample Output: Televisions - Acme UltraView 3000 - 4K HDR Smart TV\n\n##Fashion:\nStructure: Category + Attribute + Others + Audience\nExample Input: \"Category\": \"women’s shoes\", \"Attribute\": \"Leather\", \"Others\": \"High Heels\", \"Audience\": \"Party Wear\" \nExample Output: Women’s Shoes - Leather High Heels - Party Wear\n\n##General Merchandise:\nStructure: Category + Attribute + Other\nExample Input:  \"Category\": \"furniture\", \"Attribute\": \"Contemporary\", \"Other\": \"3-Seater Sofa\" \nExample Output: Furniture - Contemporary 3-Seater Sofa\n\n##Fast-Moving Consumer Goods:\nStructure: Category + Attribute + Other\nExample Input:\"Category\": \"beauty\", \"Attribute\": \"Hydrating\", \"Other\": \"Facial Moisturizer\" \nExample Output: Beauty - Hydrating Facial Moisturizer\nTerminology Definitions:\n\n-Audience refers to the target demographic of the product.\n-Brand refers to the company or manufacturer that produced the product.\n-Category signifies the high-level product classification as per the main categories mentioned.\n-Model pertains to the specific model or version of the product.\n-Other includes information such as number, unit, time, genre, event, taste, place.\n-Attribute implies features such as IP, style, quantity, design, function, smell, material, pattern, color, or shape.\n\n#[product information]\n[Category]:$!{product_category}\n[original title]:$!{product_title}\n\nOutput:";
//        VelocityContext velocityContext = new VelocityContext();
////        velocityContext.put("service", new VelocityInnerFunction(requestParam));
//        StringWriter stringWriter = new StringWriter();
//        Velocity.evaluate(velocityContext,stringWriter, "", inputData);
//        System.out.println(stringWriter);
    }

//    /**
//     * 设置流程变量值
//     * @return
//     */
//    public static Boolean setProcessVariable(String expr,JSONObject jsonObject,Object value) {
//        String jsonPath = convertExpToJsonPath(expr);
//        if(StringUtils.isBlank(jsonPath)) {
//            return false;
//        }
//        return JSONPath.set(jsonObject,jsonPath,value);
//    }
//
//    public static Object getObjectByJsonPath(String expr,JSONObject jsonObject) {
//        if(StringUtils.isBlank(expr)) {
//            return null;
//        }
//        String jsonPath = convertExpToJsonPath(expr);
//        if(StringUtils.isBlank(jsonPath)) {
//            return null;
//        }
//        return JSONPath.eval(jsonObject,jsonPath);
//    }
//
//    public static String convertExpToJsonPath(String expr) {
//        if(StringUtils.isBlank(expr)) {
//            return null;
//        }
//        if(expr.startsWith("$!{") && expr.endsWith("}") && StringUtils.countMatches(expr,"$!{") == 1) {
//            expr = expr.substring(3,expr.length()-1);
//        }
//        String[] keyArray = expr.split("\\.");
//        List<String> collect = Arrays.stream(keyArray).map(e -> {
//            if (e.contains("[") && e.contains("]")) {
//                return "['" + e.substring(0, e.indexOf("[")) + "']" + e.substring(e.indexOf("["));
//            } else {
//                return "['" + e + "']";
//            }
//        }).collect(Collectors.toList());
//        return "$"+StringUtils.join(collect, "");
//    }

    public static Object getObjectByExpr(String expr,JSONObject jsonObject) {
        if(StringUtils.isBlank(expr)) {
            return null;
        }
        if(expr.equals("systemTime")) {
            return System.currentTimeMillis();
        }else if(expr.equals("systemTimeStr")) {
            return DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        }
        Object res = null;
        List<String> codeList = Arrays.asList(expr.split("\\."));
        if(codeList.size() <= 1) {
            return jsonObject.get(codeList.get(0));
        }
        for (int i = 0; i < codeList.size() - 1; i++) {
            Object temp = jsonObject.get(codeList.get(i));
            if(temp == null) {
                return null;
            }
            if(temp instanceof Map){
                jsonObject = jsonObject.getJSONObject(codeList.get(i));
            }else if(temp instanceof ProcessSystemContext){
                jsonObject = JSONObject.parseObject(JSON.toJSONString(temp));
            } else {
                return null;
            }
        }
        res = jsonObject.get(codeList.get(codeList.size()-1));
        return res;
    }

    public static String getActivityName(ExecutionContext context, String activityId) {
        ExtensionElementContainer idBasedElement = (ExtensionElementContainer)context.getProcessDefinition().getIdBasedElementMap().get(activityId);
        if(idBasedElement instanceof AbstractActivity) {
            AbstractActivity activity = (AbstractActivity) idBasedElement;
            String name = activity.getName();
            if(StringUtils.isEmpty(name)
                    && MapUtils.isNotEmpty(activity.getProperties())
                    && activity.getProperties().containsKey("name")) {
                name = activity.getProperties().get("name");
            }
            String toolVersion = getProcessDefinitionValue(idBasedElement, "toolVersion");
            if(StringUtils.isEmpty(toolVersion)) {
                return name;
            }
            return name + "_V" + toolVersion;
        }
        return activityId;
    }

    public static String getProcessDefinitionValue(ExtensionElementContainer idBasedElement, String key) {
        try {
            ExtensionElements extensionElements = idBasedElement.getExtensionElements();
            if(Objects.isNull(extensionElements) || MapUtils.isEmpty(extensionElements.getDecorationMap())) {
                return "";
            }
            Map map = (Map)extensionElements.getDecorationMap().get(ExtensionElementsConstant.PROPERTIES);
            if(MapUtils.isEmpty(map)) {
                return "";
            }

            return MapUtils.getString(map, key);
        } catch (Exception e) {
            log.error("getProcessDefinitionValue error, key: {}", key);
            return "";
        }
    }

    public static String getActivityType(ExecutionContext context) {
        String activityName = context.getBaseElement().getClass().getSimpleName();
        if ("ServiceTask".equals(activityName)) {
            ServiceTask serviceTask = (ServiceTask)context.getBaseElement();
            if(serviceTask.getProperties().get("class") != null) {
                return serviceTask.getProperties().get("class");
            }
        }
        return activityName;
    }

    public static JSONObject getJsonPropertiesFromContext(ExecutionContext context, String activityId) {
        ExtensionElementContainer idBasedElement = (ExtensionElementContainer)context.getProcessDefinition().getIdBasedElementMap().get(activityId);
        ExtensionElements extensionElements = idBasedElement.getExtensionElements();
        if(extensionElements == null || CollectionUtils.isEmpty(extensionElements.getDecorationMap())) {
            return new JSONObject();
        }
        JSONObject jsonObject = new JSONObject();
        List<ExtensionDecorator> propertiesList = extensionElements.getExtensionList();
        for (ExtensionDecorator extensionDecorator: propertiesList) {
            Properties properties = (Properties) extensionDecorator;
            for (PropertiesElementMarker propertiesElementMarker : properties.getExtensionList()) {
                Value value = (Value) propertiesElementMarker;
                if (value == null || value.getName() == null){
                    continue;
                }
                jsonObject.put(value.getName(), value.getValue());
            }
        }

//        propertiesList.stream().forEach(properties -> {
//            List<Value> values = properties.getExtensionList();
//        });
//        JSONObject map = new JSONObject(extensionElements.getDecorationMap());
//        JSONObject jsonObject = map.getJSONObject(ExtensionElementsConstant.PROPERTIES);

        return jsonObject;
    }

    /**
     * 判断是否为异步回调
     *
     * @param requestJson
     * @param activityId
     * @return
     */
    public static boolean isCallback(JSONObject requestJson,String activityId) {
        if(requestJson.get(ProcessSystemContext.KEY) != null) {
            ProcessSystemContext systemContext = requestJson.getObject(ProcessSystemContext.KEY, ProcessSystemContext.class);
            if(systemContext.getNodeCallback() != null && !systemContext.getNodeCallback().isEmpty()) {
                if(systemContext.getNodeCallback().get(activityId) != null && systemContext.getNodeCallback().get(activityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeCallback(ExecutionContext executionContext, JSONObject requestJson,String activityId) {
        if(requestJson.get(ProcessSystemContext.KEY) != null) {
            ProcessSystemContext systemContext = requestJson.getObject(ProcessSystemContext.KEY, ProcessSystemContext.class);
            if(systemContext.getNodeCallback() != null && !systemContext.getNodeCallback().isEmpty()) {
                systemContext.getNodeCallback().remove(activityId);
                executionContext.getRequest().put(ProcessSystemContext.KEY, systemContext);
            }
        }
    }

    public static boolean isSubCallback(JSONObject requestJson, String activityId) {
        if(requestJson.get(ProcessSystemContext.KEY) != null) {
            ProcessSystemContext systemContext = requestJson.getObject(ProcessSystemContext.KEY, ProcessSystemContext.class);
            if(systemContext.getNodeSubCallback() != null && !systemContext.getNodeSubCallback().isEmpty()) {
                if(systemContext.getNodeSubCallback().get(activityId) != null && systemContext.getNodeSubCallback().get(activityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getNodeName(ExecutionContext context) {
        if(context.getBaseElement() != null) {
            if(context.getBaseElement() instanceof AbstractActivity){
                AbstractActivity baseElement = (AbstractActivity)context.getBaseElement();
                if(!CollectionUtils.isEmpty(baseElement.getProperties())) {
                    return baseElement.getProperties().get("name");
                }
            }
        }
        return null;
    }
    /**
     * 获取外部业务id，方便做全链路日志追踪
     */
    public static String getTraceId(JSONObject requestJson) {
        try {
            String traceId = requestJson.getString(ProcessConstant.REQUEST_TRACE_ID);
            if(StringUtils.isBlank(traceId)) {
                if(requestJson.get(ProcessConstant.SYSTEM) != null) {
                    return requestJson.getJSONObject(ProcessConstant.SYSTEM).getString(ProcessConstant.REQUEST_TRACE_ID);
                }
            }
            return traceId;
        } catch (Throwable e) {
            log.error("unexpected err", e);
            return null;
        }
    }
//
//    /**
//     * 获取事件订阅ID
//     * @param requestJson
//     * @return
//     */
//    public static Long getEventId(JSONObject requestJson) {
//        try {
//            if(requestJson.get(ProcessConstant.SYSTEM) != null) {
//                return requestJson.getJSONObject(ProcessConstant.SYSTEM).getLong(ProcessConstant.SYSTEM_EVENT_ID);
//            }
//            return null;
//        } catch (Throwable e) {
//            log.error("unexpected err", e);
//            return null;
//        }
//    }
//
//
//    public static boolean dataFilter(Object left,String opType, Object right) {
//        OperatorEnum operatorEnum = OperatorEnum.getByCode(opType);
//        if(operatorEnum == null) {
//            throw new IllegalArgumentException("无效的过滤条件："+opType);
//        }
//        try {
//            Object result = null;
//            if (operatorEnum.getParamType().length == 0) {
//                result = UserFunction.class.getMethod(operatorEnum.getFuncName(), operatorEnum.getParamType()).invoke(null, new Object[]{});
//            } else if (operatorEnum.getParamType().length == 1) {
//                result = UserFunction.class.getMethod(operatorEnum.getFuncName(), operatorEnum.getParamType()).invoke(null, new Object[]{left});
//            } else if (operatorEnum.getParamType().length == 2) {
//                result = UserFunction.class.getMethod(operatorEnum.getFuncName(), operatorEnum.getParamType()).invoke(null, new Object[]{left, right});
//            }
//            if (result != null &&  result instanceof Boolean) {
//                return (boolean) result;
//            }else {
//                throw new IllegalArgumentException("无效的过滤条件："+opType);
//            }
//        }catch (Exception e) {
//            throw new IllegalArgumentException("条件执行失败，"+opType);
//        }
//
//    }

    public static BigDecimal getNumber(Object o) {
        if(o == null) {
            return null;
        }
        if(o instanceof BigDecimal) {
            return (BigDecimal) o;
        }
        String str = String.valueOf(o);
        if(NumberUtils.isParsable(str)) {
            return new BigDecimal(str);
        }
        return null;
    }

    public static Map<String, Object> buildRequest(Map<String, Object> params) {
        Map<String, Object> request = new HashMap<>();
        if (!CollectionUtils.isEmpty(params)) {
            request.putAll(params);
        }
        return request;
    }

    public static void main_1(String[] args) {
        String input = "category:$#{cat}和$#{aaa} 123123";
//        String regex = "\\$#\\{([^}]+)\\}";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(input);
//        String replaced = matcher.replaceAll("\\$!{service.getAutoIntent\\('$1'\\)}");

        String regex = "\\$#\\{([^}]+)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement("$!{service.getAutoIntent($!{" + matcher.group(1) + "})}"));
        }
        matcher.appendTail(result);

        System.out.println(result);
    }
}
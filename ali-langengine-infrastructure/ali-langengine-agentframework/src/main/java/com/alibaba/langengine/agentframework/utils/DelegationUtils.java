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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;

import java.util.Map;

public class DelegationUtils {

    /**
     * xml 中json转jsonObject
     * @param jsonString
     * @return
     */
    public static JSONObject buildJsonValue(String jsonString) {
        if(StringUtils.isBlank(jsonString)) {
            return new JSONObject();
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonString
                .replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&apos;", "'"));

        return jsonObject;

    }

    /**
     * 去掉转义
     * @param str
     * @return
     */
    public static String getRawString(String str) {
        if(str == null) {
            return "";
        }
        return str.replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&apos;", "'");
    }

    /**
     * xml 中json转jsonObject
     * @param jsonString
     * @return
     */
    public static JSONArray buildJsonArrayValue(String jsonString) {
        if(StringUtils.isBlank(jsonString)) {
            return new JSONArray();
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString
                .replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&apos;", "'"));

        return jsonArray;

    }

    /**
     * 解析参数中的表达式
     * @param paramMap
     * @return
     */
    public static JSONObject buildParamJson(JSONObject paramMap, Map<String,Object> request) {
        JSONObject jsonObject = new JSONObject();
        for(Map.Entry entry : paramMap.entrySet()){
            // 流程变量值
            try {
                String value = String.valueOf(entry.getValue());
                if(StringUtils.isBlank(value)) {
                    jsonObject.put(entry.getKey().toString(),"");
                    continue;
                }
                // 如果全为对象，用表达式获取值
                if(value.startsWith("$!{") && value.endsWith("}")) {
                    String dataPath = value.replace("$!{","").replace("}","");
                    Object obj = MVEL.eval(dataPath, request);
                    if(obj != null){
                        jsonObject.put(entry.getKey().toString(), obj);
                    }else {
                        jsonObject.put(entry.getKey().toString(), "");
                    }
                }else {
                    // 流程引擎
                    jsonObject.put(entry.getKey().toString(), FrameworkUtils.buildInputDataValue(entry.getKey().toString(),value,request,null));
                }
            }catch (Exception e){
                throw new RuntimeException(String.format("表达式计算出错,请检查表达式或变量是否存在，表达式:%s",entry.getValue()));
            }
        }

        return jsonObject;
    }

}

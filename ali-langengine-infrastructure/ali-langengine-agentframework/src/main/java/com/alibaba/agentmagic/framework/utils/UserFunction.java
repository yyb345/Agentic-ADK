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

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class UserFunction {

    public static boolean equal(Object left, Object right) {
        if(right instanceof Object[] || right instanceof List) {
            Object[] rightArray = null;
            if(right instanceof Object[]) {
                rightArray = (Object[]) right;
            }else {
                rightArray = ((List) right).toArray();
            }
            for (int i = 0; i < rightArray.length; i++) {
                if(String.valueOf(left).equals(String.valueOf(rightArray[i]))) {
                    return true;
                }
            }
            return false;
        }
        return StringEscapeUtils.unescapeJava(String.valueOf(left)).equals(String.valueOf(right));
    }

    public static boolean notEqual(Object left, Object right) {
        if(left == null || "null".equals(String.valueOf(left))) {
            return false;
        }
        return !equal(left,right);
    }

    public static boolean isNull(Object left) {
        if(left == null) {
            return true;
        }
        if(left instanceof String) {
            return StringUtils.isBlank(String.valueOf(left)) || "null".equals(left)
                    || "{}".equals(left) || "[]".equals(left);
        }else if(left instanceof Object[]) {
            return ((Object[])left).length == 0;
        }
        if(left instanceof List) {
            return CollectionUtils.isEmpty((List)left);
        }
        return false;
    }

    public static boolean isNotNull(Object left) {
        return !isNull(left);
    }

    public static boolean greaterThan(Object left, Object right) {
        if(left == null) {
            return false;
        }
        if(!isNumber(left)) {
            return false;
        }
        BigDecimal leftNum = getNumber(left);
        // right 为列表
        if(right instanceof Object[] || right instanceof List) {
            Object[] rightArray = null;
            if(right instanceof Object[]) {
                rightArray = (Object[]) right;
            }else {
                rightArray = ((List) right).toArray();
            }
            for (int i = 0; i < rightArray.length; i++) {
                if(!isNumber(rightArray[i])) {
                    continue;
                }
                BigDecimal rightNum = getNumber(rightArray[i]);
                if(leftNum.compareTo(rightNum) > 0) {
                    return true;
                }
            }
            return false;
        }
        // right 为单个
        if(!isNumber(right)) {
            return false;
        }
        BigDecimal rightNum = getNumber(right);
        return leftNum.compareTo(rightNum) > 0;
    }


    public static boolean greaterThanOrEqual(Object left, Object right) {
        if(left == null) {
            return false;
        }
        if(!isNumber(left)) {
            return false;
        }
        BigDecimal leftNum = getNumber(left);
        if(right instanceof Object[] || right instanceof List) {
            Object[] rightArray = null;
            if(right instanceof Object[]) {
                rightArray = (Object[]) right;
            }else {
                rightArray = ((List) right).toArray();
            }
            for (int i = 0; i < rightArray.length; i++) {
                if(!isNumber(rightArray[i])) {
                    continue;
                }
                BigDecimal rightNum = getNumber(rightArray[i]);
                if(leftNum.compareTo(rightNum) >= 0) {
                    return true;
                }
            }
            return false;
        }
        // right 为单个
        if(!isNumber(right)) {
            return false;
        }
        BigDecimal rightNum = getNumber(right);
        return leftNum.compareTo(rightNum) >= 0;
    }

    public static boolean lessThan(Object left, Object right) {
        if(left == null) {
            return false;
        }
        if(!isNumber(left)) {
            return false;
        }
        BigDecimal leftNum = getNumber(left);
        if(right instanceof Object[] || right instanceof List) {
            Object[] rightArray = null;
            if(right instanceof Object[]) {
                rightArray = (Object[]) right;
            }else {
                rightArray = ((List) right).toArray();
            }
            for (int i = 0; i < rightArray.length; i++) {
                if(!isNumber(rightArray[i])) {
                    continue;
                }
                BigDecimal rightNum = getNumber(rightArray[i]);
                if(leftNum.compareTo(rightNum) < 0) {
                    return true;
                }
            }
            return false;
        }
        // right 为单个
        if(!isNumber(right)) {
            return false;
        }
        BigDecimal rightNum = getNumber(right);
        return leftNum.compareTo(rightNum) < 0;
    }

    public static boolean lessThanOrEqual(Object left, Object right) {
        if(left == null) {
            return false;
        }
        if(!isNumber(left)) {
            return false;
        }
        BigDecimal leftNum = getNumber(left);
        if(right instanceof Object[] || right instanceof List) {
            Object[] rightArray = null;
            if(right instanceof Object[]) {
                rightArray = (Object[]) right;
            }else {
                rightArray = ((List) right).toArray();
            }
            for (int i = 0; i < rightArray.length; i++) {
                if(!isNumber(rightArray[i])) {
                    continue;
                }
                BigDecimal rightNum = getNumber(rightArray[i]);
                if(leftNum.compareTo(rightNum) <= 0) {
                    return true;
                }
            }
            return false;
        }
        // right 为单个
        if(!isNumber(right)) {
            return false;
        }
        BigDecimal rightNum = getNumber(right);
        return leftNum.compareTo(rightNum) <= 0;
    }

    public static Number max(Object input) {
        if(input instanceof Object[] || input instanceof List) {
            Object[] objArray = null;
            if(input instanceof Object[]) {
                objArray = (Object[]) input;
            }else {
                objArray = ((List) input).toArray();
            }
            List<BigDecimal> valueList = new ArrayList<>();
            for (int i = 0; i < objArray.length; i++) {
                if(!isNumber(objArray[i])) {
                    continue;
                }
                valueList.add(getNumber(objArray[i]));
            }
            return Collections.max(valueList);
        }
        throw new IllegalArgumentException(String.format("无效的表达式，max(%s)",String.valueOf(input)));

    }

    public static Number min(Object input) {
        if(input instanceof Object[] || input instanceof List) {
            Object[] objArray = null;
            if(input instanceof Object[]) {
                objArray = (Object[]) input;
            }else {
                objArray = ((List) input).toArray();
            }
            List<BigDecimal> valueList = new ArrayList<>();
            for (int i = 0; i < objArray.length; i++) {
                if(!isNumber(objArray[i])) {
                    continue;
                }
                valueList.add(getNumber(objArray[i]));
            }
            return Collections.min(valueList);
        }
        throw new IllegalArgumentException(String.format("无效的表达式，max(%s)",String.valueOf(input)));

    }

    public static boolean notContain(Object left, Object right){
        return !contain(left,right);
    }

    public static boolean contain(Object left, Object right){
        List<Object> targetList = getListValue(left);
        if(CollectionUtils.isEmpty(targetList)) {
            return false;
        }
        List<Object> valueList = getListValue(right);
        return !Collections.disjoint(targetList,valueList);
    }

    /**
     *  left 在 right 中
     * @param left
     * @param right
     * @return
     */
    public static boolean in(Object left, Object right){
        List<Object> valueList = getListValue(right);
        if(CollectionUtils.isEmpty(valueList)) {
            return false;
        }
        List<Object> leftList = getListValue(left);
        return !Collections.disjoint(valueList,leftList);
    }

    public static boolean notIn(Object left, Object right){
        return !in(left,right);
    }

    public static String urlEncode(String left)  {
        try{
            return URLEncoder.encode(left, "UTF-8");
        }catch (Exception e) {
            log.error("URLEncoder error:"+left,e);
            return left;
        }

    }

    /**
     * 日期计算
     * @param dateStr
     * @param amount
     * @param unit
     * @param format
     * @return
     */
    public static String dateCompute(String dateStr,Integer amount,String unit,String format)  {
        try{
            Date date = null;
            if(StringUtils.isNumeric(dateStr)) {
                date = new Date(Long.parseLong(dateStr));
            }else if (dateStr.length() == 10) {
                date = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
            }else {
                date = DateUtils.parseDate(dateStr, "yyyy-MM-dd HH:mm:ss");
            }
            if("y".equals(unit)) {
                date =  DateUtils.addYears(date,amount);
            }else if("M".equals(unit)) {
                date =  DateUtils.addMonths(date,amount);
            }else if("d".equals(unit)) {
                date =  DateUtils.addDays(date,amount);
            }else if("H".equals(unit)) {
                date =  DateUtils.addHours(date,amount);
            }else if("m".equals(unit)) {
                date =  DateUtils.addMinutes(date,amount);
            }else if("s".equals(unit)) {
                date =  DateUtils.addSeconds(date,amount);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        }catch (Exception e) {
            log.error("URLEncoder error:"+dateStr,e);
            return dateStr;
        }

    }

    private static boolean isNumber(Object o){
        if(o == null) {
            return false;
        }
        if(o instanceof BigDecimal) {
            return true;
        }
        String str = String.valueOf(o);
        if(NumberUtils.isParsable(str)) {
            return true;
        }
        if(str.endsWith("B") && NumberUtils.isParsable(str.substring(0,str.length()-1))){
             return true;
        }
        return false;
    }

    private static BigDecimal getNumber(Object o) {
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
        if(str.endsWith("B") && NumberUtils.isParsable(str.substring(0,str.length()-1))){
            return new BigDecimal(str.substring(0,str.length()-1));
        }
        return null;
    }

    private static List<Object> getListValue(Object obj) {
        List<Object> list = new ArrayList<>();
        if(obj instanceof List || obj instanceof Object[]) {
            List keyList = new ArrayList();
            if(obj instanceof Object[] ) {
                keyList = Arrays.asList((Object[]) obj);
            }else {
                keyList = (List) obj;
            }
            for (Object key : keyList) {
                if(key != null && key instanceof String
                        && ((String) key).startsWith("[") && ((String) key).endsWith("]")) {

                    String keyStr = StringEscapeUtils.unescapeJava(String.valueOf(key));;
                    if(keyStr.startsWith("[") && keyStr.endsWith("]")) {
                        list.addAll(JSONArray.parseArray(keyStr));
                    }
                }else if(key != null) {
                    list.add(key);
                }
            }
        }else if(obj instanceof String) {
            String objStr = String.valueOf(obj);
            if(objStr.startsWith("[") && objStr.endsWith("]")) {
                objStr = StringEscapeUtils.unescapeJava(String.valueOf(obj));;
                list.addAll(JSONArray.parseArray(objStr));
            }else {
                list.add(objStr);
            }
        }else {
            list.add(obj);
        }
        return list;
    }




}

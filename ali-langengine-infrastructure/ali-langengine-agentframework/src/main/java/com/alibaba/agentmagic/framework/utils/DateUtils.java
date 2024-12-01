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

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DateUtils {

    /**
     * 正常时间格式 yyyy-MM-dd HH:mm:ss
     */
    public static final String DATETIME_DEFULT_FORMAT_REGEX = "^(\\d{4})[-](\\d{2})[-](\\d{2})\\s{1}(\\d{2}):(\\d{2}):(\\d{2})$";
    public static final String DATETIME_DEFULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间格式 yyyy.MM.dd HH:mm:ss
     */
    public static final String DATETIME_DOT_FORMAT_REGEX = "^(\\d{4})[\\.](\\d{2})[\\.](\\d{2})\\s{1}(\\d{2}):(\\d{2}):(\\d{2})$";
    public static final String DATETIME_DOT_FORMAT = "yyyy.MM.dd HH:mm:ss";

    /**
     * 正常时间格式 yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String DATETIME_FULL_FORMAT_REGEX = "^(\\d{4})[-](\\d{2})[-](\\d{2})\\s{1}(\\d{2}):(\\d{2}):(\\d{2})[.](\\d{3})$";
    public static final String DATETIME_FULL_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 时间格式 yyyy/MM/dd HH:mm:ss
     */
    public static final String DATETIME_FORMAT_REGEX2 = "^(\\d{4})[/](\\d{2})[/](\\d{2})\\s{1}(\\d{2}):(\\d{2}):(\\d{2})$";
    public static final String DATETIME_FORMAT2 = "yyyy/MM/dd HH:mm:ss";


    /**
     * 有符号正常日期格式 yyyy-MM-dd
     */
    public static final String DATE_FORMAT_COMMON_REGEX = "^(\\d{4})[-](\\d{2})[-](\\d{2})$";
    public static final String DATE_FORMAT_COMMON_FORMAT = "yyyy-MM-dd";

    public static final String DATE_FORMAT_DOT_REGEX = "^(\\d{4})[\\.](\\d{2})[\\.](\\d{2})$";
    public static final String DATE_FORMAT_DOT_FORMAT = "yyyy.MM.dd";

    /**
     * 年月 yyyy-MM
     */
    public static final String DATE_YYYY_MM_REGEX = "^(\\d{4})[-](\\d{2})$";
    public static final String DATE_YYYY_MM_FORMAT = "yyyy-MM";

    /**
     * 有符号正常日期格式 yyyy/MM/dd
     */
    public static final String DATE_FORMAT_REGEX = "^(\\d{4})[/](\\d{2})[/](\\d{2})$";
    public static final String DATE_FORMAT_FORMAT = "yyyy/MM/dd";

    /**
     * 无符号正则表达式, yyyyMMdd
     */
    public static final String DATE_FORMAT_NO_SPLIT_REGEX = "^(\\d{4})(\\d{2})(\\d{2})$";
    public static final String DATE_FORMAT_NO_SPLIT_FORMAT = "yyyyMMdd";

    /**
     * 倒序的日期格式  dd/MM/yyyy
     */
    public static final String DATE_FORMAT_REVERT_REGEX = "^(\\d{2})[/](\\d{2})[/](\\d{4})$";
    public static final String DATE_FORMAT_REVERT_FORMAT = "dd/MM/yyyy";

    /**
     * 中文
     */
    public static final String DATE_CHINESE_REGEX = "^(\\d{4})年(\\d{2})月(\\d{2})日$";
    public static final String DATE_CHINESE_FORMAT = "yyyy年MM月dd日";

    /**
     * 中文 时分秒
     */
    public static final String DATETIME_CHINESE_REGEX = "^(\\d{4})年(\\d{2})月(\\d{2})日\\s{1}(\\d{2})时(\\d{2})分(\\d{2})秒$";
    public static final String DATETIME_CHINESE_FORMAT = "yyyy年MM月dd日 HH时mm分ss秒";

    /**
     * 缓存的自动识别的格式正则表达式
     */
    private static List<DateReplace> autoDateCache = new ArrayList<DateReplace>();

    static {
        registerAutoFormat(DATETIME_DEFULT_FORMAT_REGEX,DATETIME_DEFULT_FORMAT);
        registerAutoFormat(DATETIME_DOT_FORMAT_REGEX,DATETIME_DOT_FORMAT);
        registerAutoFormat(DATETIME_FULL_FORMAT_REGEX,DATETIME_FULL_FORMAT);
        registerAutoFormat(DATETIME_FORMAT_REGEX2,DATETIME_FORMAT2);
        registerAutoFormat(DATE_YYYY_MM_REGEX,DATE_YYYY_MM_FORMAT);
        registerAutoFormat(DATE_FORMAT_COMMON_REGEX,DATE_FORMAT_COMMON_FORMAT);
        registerAutoFormat(DATE_FORMAT_DOT_REGEX,DATE_FORMAT_DOT_FORMAT);
        registerAutoFormat(DATE_FORMAT_REGEX,DATE_FORMAT_FORMAT);
        registerAutoFormat(DATE_FORMAT_NO_SPLIT_REGEX,DATE_FORMAT_NO_SPLIT_FORMAT);
        registerAutoFormat(DATE_FORMAT_REVERT_REGEX,DATE_FORMAT_REVERT_FORMAT);
        registerAutoFormat(DATE_CHINESE_REGEX,DATE_CHINESE_FORMAT);
        registerAutoFormat(DATETIME_CHINESE_REGEX,DATETIME_CHINESE_FORMAT);
    }

    /**
     * 时间格式字符串
     */
    @Data
    private static class DateReplace {

        // 正则表达式
        private Pattern pattern;

        // 替换表达式
        private SimpleDateFormat simpleDateFormat;

        public DateReplace() {
        }

        public DateReplace(String regex, String format) {
            this.pattern = Pattern.compile(regex);
            this.simpleDateFormat = new SimpleDateFormat(format);
        }
    }

    /**
     * 注册正则表达式，将时间转换为正确格式的正则表达式，后注册的会优先执行 。
     *
     * @param regex   正则表达式
     * @param format 替换表达式
     */
    public static void registerAutoFormat(String regex,String format) {
        DateReplace item = new DateReplace(regex,format);
        autoDateCache.add(item);
    }

    /**
     * 根据时间字符串自动识别时间
     *
     * @param dateStr 时间字符串
     * @return 时间
     */
    public static Date getAutoDate(String dateStr) throws ParseException {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        dateStr = StringUtils.strip(dateStr);
        if(NumberUtils.isParsable(dateStr) && !dateStr.startsWith("20")) {
            return new Date(Long.parseLong(dateStr));
        }
        for (DateReplace dateReplace : autoDateCache) {
            Matcher matcher = dateReplace.getPattern().matcher(dateStr);
            if(matcher.matches()) {
                return dateReplace.simpleDateFormat.parse(dateStr);
            }
        }
        return null;
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        } else if (pattern == null) {
            throw new IllegalArgumentException("pattern is null");
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(getAutoDate("2022-02-01 11:12:23"));
        System.out.println(getAutoDate("2022-02-01 11:12:23.006"));
        System.out.println(getAutoDate("2022/02/01 11:12:23"));
        System.out.println(getAutoDate("1556640000000"));
        System.out.println(getAutoDate("-1244563750000"));
        System.out.println(getAutoDate("2019-05-01"));
        System.out.println(getAutoDate("2019-06"));
        System.out.println(getAutoDate("2022/02/01"));
        System.out.println(getAutoDate("05/02/2019"));
        System.out.println(getAutoDate("2023年05月31日"));
        System.out.println(getAutoDate("2023.01.04"));
        System.out.println(getAutoDate("20230104"));
        System.out.println(getAutoDate("2023.01.05 12:03:03"));
        System.out.println(getAutoDate("2023年05月31日 12时12分12秒"));

    }
}

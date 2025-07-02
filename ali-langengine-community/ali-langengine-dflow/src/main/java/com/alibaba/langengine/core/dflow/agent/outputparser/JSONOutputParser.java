package com.alibaba.langengine.core.dflow.agent.outputparser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.dflow.agent.DFlowDeepCrawlerAgent.ValidationResult;
import com.alibaba.langengine.core.dflow.agent.DFlowToolCallAgent.ToolCallResult;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;

public class JSONOutputParser extends BaseOutputParser<String> {

    public static String cleanJsonString(String rawJsonString) {
        // 去除前后空白字符
        String cleanedString = rawJsonString.trim();

        // 找到第一个 '{' 和最后一个 '}' 的位置
        int startIndex = cleanedString.indexOf('{');
        int endIndex = cleanedString.lastIndexOf('}');

        // 如果找到了有效的 '{' 和 '}'，则提取它们之间的内容
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return cleanedString.substring(startIndex, endIndex + 1);
        }

        // 如果没有找到有效的 JSON 格式，返回空字符串或抛出异常
        return ""; // 或者可以根据需求抛出异常
    }

    @Override
    public String parse(String s) {
        return cleanJsonString(s);
    }
}

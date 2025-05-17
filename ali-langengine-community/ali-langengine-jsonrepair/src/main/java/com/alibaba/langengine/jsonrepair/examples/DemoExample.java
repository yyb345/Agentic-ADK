package com.alibaba.langengine.jsonrepair.examples;

import com.alibaba.langengine.jsonrepair.JsonSafeParser;

/**
 * @author: aihe.ah
 * @date: 2025/5/17
 * 功能描述：
 */

public class DemoExample {
    public static void main(String[] args) {
        String s = "[\"123\", \"123\"]}]";
        String validJsonString = JsonSafeParser.getValidJsonString(s);
        System.out.println(validJsonString);
    }
}

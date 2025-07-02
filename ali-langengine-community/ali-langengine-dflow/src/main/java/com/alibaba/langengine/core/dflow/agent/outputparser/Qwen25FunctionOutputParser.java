package com.alibaba.langengine.core.dflow.agent.outputparser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.dflow.agent.DFlowToolCallAgent.ToolCallResult;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;

public class Qwen25FunctionOutputParser extends BaseOutputParser<String> {

    @Override
    public String parse(String s) {
        /**
         * <tool_call>
         * {"name": "get_temperature_date", "arguments": {"location": "San Francisco, CA, USA", "date": "2024-10-01"}}
         * </tool_call>
         */

        //<tool_call>之前的内容
        int index = s.indexOf("<tool_call>");

        String content = (index >= 0) ? s.substring(0, s.indexOf("<tool_call>")) : s;

        ToolCallResult result = new ToolCallResult();
        result.setToolCalls(new ArrayList<>());
        result.setContent(content);

        Pattern toolCallPattern = Pattern.compile("<tool_call>(.*?)</tool_call>", Pattern.DOTALL);
        Matcher toolCallMatcher = toolCallPattern.matcher(s);

        while (toolCallMatcher.find()) {
            String jsonContent = toolCallMatcher.group(1).trim();
            ToolCallFunction function = JSON.parseObject(jsonContent, ToolCallFunction.class);
            result.getToolCalls().add(function);
        }
        if (result.getToolCalls().size() == 0) {
            try {
                if (s.startsWith("[")) {
                    List<ToolCallFunction> functions = JSON.parseArray(s, ToolCallFunction.class);
                    result.setToolCalls(functions);
                } else {
                    ToolCallFunction function = JSON.parseObject(s, ToolCallFunction.class);
                    result.getToolCalls().add(function);
                }
            } catch (Exception e) {
                String[] res = s.split("<tool_call>");
                if(res.length > 1){
                    result.setContent(res[0]);
                    for (String r : res) {
                        try {
                            ToolCallFunction function = JSON.parseObject(r.replace("</tool_call>", ""),
                                ToolCallFunction.class);
                            result.getToolCalls().add(function);
                        } catch (Exception e1) {
                        }
                    }
                }else{
                    result.setContent(s);
                }
            }
        }
        return JSON.toJSONString(result);
    }

    public static void main(String[] args) {
        String s = "根据淘宝搜索结果，以下是一些适合作为程序员实用性强的办公用品礼物推荐：\n"
            + "\n"
            + "1. **价格：¥19.51**  \n"
            + "   - 商品名称：python鼠标垫程序员码农geek代码IT男周边神器礼物猿超大编程宝典  \n"
            + "   - 链接：[点击查看](https://item.taobao.com/item.htm?id=588023682991)  \n"
            + "\n"
            + "2. **价格：¥19.51**  \n"
            + "   - 商品名称：C++鼠标垫男程序员神器男朋友生日礼物程序员鼠标垫超大C语言码农  \n"
            + "   - 链接：[点击查看](https://item.taobao.com/item.htm?id=557308478691)  \n"
            + "\n"
            + "3. **价格：¥80.36**  \n"
            + "   - 商品名称：公司商务礼品定制伴郎伴手礼盒实用高档剃须刀男士套装父亲节礼物  \n"
            + "   - 链接：[点击查看](https://item.taobao.com/item.htm?id=803712925604)  \n"
            + "\n"
            + "这些商品都是实用性强的办公用品，适合程序员日常使用。是否需要我将这些商品添加到你的商品池中以便后续参考？ \n"
            + "\n"
            + "<tool_call>{\"arguments\":\"{\\\"action\\\":\\\"add\\\",\\\"products\\\":[{\\\"productId\\\":\\\"588023682991\\\",\\\"title\\\":\\\"python鼠标垫程序员码农geek代码IT男周边神器礼物猿超大编程宝典\\\",\\\"actionUrl\\\":\\\"https://item.taobao.com/item.htm?id=588023682991\\\",\\\"priceStr\\\":\\\"¥19.51\\\"},{\\\"productId\\\":\\\"557308478691\\\",\\\"title\\\":\\\"C++鼠标垫男程序员神器男朋友生日礼物程序员鼠标垫超大C语言码农\\\",\\\"actionUrl\\\":\\\"https://item.taobao.com/item.htm?id=557308478691\\\",\\\"priceStr\\\":\\\"¥19.51\\\"},{\\\"productId\\\":\\\"803712925604\\\",\\\"title\\\":\\\"公司商务礼品定制伴郎伴手礼盒实用高档剃须刀男士套装父亲节礼物\\\",\\\"actionUrl\\\":\\\"https://item.taobao.com/item.htm?id=803712925604\\\",\\\"priceStr\\\":\\\"¥80.36\\\"}]}\"},{\"name\":\"workplan_item_manager_j13f\"}}</tool_call>";
        System.out.println(new Qwen25FunctionOutputParser().parse(s));
    }
}

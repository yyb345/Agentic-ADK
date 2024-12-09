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
package com.alibaba.langengine.xinghuo;

import org.junit.jupiter.api.Test;

public class ChatXingHuoTest {

    @Test
    public void test_predict() {
        String prompt = "你是谁？";
        ChatXingHuo llm = new ChatXingHuo();
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict(prompt));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predict2() {
//        String prompt = "你是谁？";
        String prompt = "尽可能有益且准确地回应人类。 您可以使用以下工具：\n" +
                "\n" +
                "historyCongestTool: Useful for when you need query  history congest index of a city within a certain time range,如果未识别到时间，请不要给startDt和endDt赋值, args: {\"city\":\"省市区县名称,不能含有路名\",\"dayType\":\"日期类型,查询工作日为WORK,查询节假日为HOLIDAY,如果未指定工作日或节假日则为ALL\",\"endDt\":\"结束时间,如果是月，取该月的最后一天,格式：yyyy-MM-dd,示例：2023-02-05,只识别出一个日期时该值为空\",\"level\":\"查询的目标数据级别;查询城市时为0; 查询区县时为4; 查询道路时为2;  值为数字字符串,默认是城市\",\"parameters\":[],\"roadName\":\"道路名称\",\"startDt\":\"开始时间，如果是月，取该月的第一天,格式：yyyy-MM-dd,示例：2023-01-05，可以为空\",\"timeGriding\":\"展示维度,按天展示为day,按月展示为month,按小时展示为hour\",\"timePeriod\":\"查询时段 取值：mpeak-早高峰 epeak-晚高峰 all-其他\",\"top\":\"查询条数，必须为数字，可以为空\",\"year\":\"表示查询的年份;格式YYYY;没有明确年份，则不能不要识别具体的年份\"}\n" +
                "realTimeCongestIdxTool: 用于查询省、市、区县的拥堵指数和速度,拥堵指数越大，代表该城市越拥堵，该方法返回结果是json数组,包含字段:城市、拥堵指数、路况状态、速度。拥堵指数是浮点型字符串,拥堵指数越大该城市越堵，如果多条数据，给出哪个城市最拥堵,当请求的目标数据是全国时,请求的level为-1;如果请求的目标数据为城市时，请求的level为0,查询行政区县时为4,查询道路时为2,查询街道时为30,查询服务区时为7,查询收费站为10,查询机场时为11,查询医院时为34,查询住宅时为35,查询景区时为41. 值为数字字符串,可以为空\";\n" +
                " , args: {\"city\":\"省/市/区县名称，可以识别多个城市\",\"level\":\"查询的目标数据级别;查询城市时为0; 查询区县时为4;  查询收费站时为10; 查询医院时为34; 查询景点或景区时为41; 值为数字字符串,可以为空\",\"showChart\":\"展示图表方式, 请求柱状图时为bar; 请求折线图时为line; 请求饼状图时为pie;没有明确表示,则值为空\",\"top\":\"查询条数\"}\n" +
                "\n" +
                "使用 json blob 通过提供 action 键（工具名称）和 action_input 键（工具输入）来指定工具。\n" +
                "\n" +
                "有效的 \"action\" 值: \"Final Answer\" 或者 historyCongestTool, realTimeCongestIdxTool\n" +
                "\n" +
                "每个 $JSON_BLOB 仅提供一个操作，如下所示：\n" +
                "\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": $TOOL_NAME,\n" +
                "  \"action_input\": $INPUT\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "请遵循以下格式：\n" +
                "\n" +
                "Question: 输入要回答的问题\n" +
                "Thought: 考虑之前和之后的步骤\n" +
                "Action:\n" +
                "```\n" +
                "$JSON_BLOB\n" +
                "```\n" +
                "Observation: action的结果\n" +
                "... (重复 Thought/Action/Observation N 次)\n" +
                "Thought: 我知道该回应什么\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"Final Answer\",\n" +
                "  \"action_input\": \"对人类的最终反应\"\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "开始！ 提醒始终使用单个操作的有效 json blob 进行响应。 必要时使用工具。 如果合适的话直接回复。 格式为 Action:```$JSON_BLOB```然后是 Observation:。\n" +
                "\n" +
                "Question: 北京海淀区拥堵指数是多少?\n" +
                "Thought: ";
//        String prompt = "尽可能有益且准确地回应人类。 您可以使用以下工具：\n" +
//                "\n" +
//                "historyCongestTool: Useful for when you need query  history congest index of a city within a certain time range,如果未识别到时间，请不要给startDt和endDt赋值, args: {\"city\":\"省市区县名称,不能含有路名\",\"dayType\":\"日期类型,查询工作日为WORK,查询节假日为HOLIDAY,如果未指定工作日或节假日则为ALL\",\"endDt\":\"结束时间,如果是月，取该月的最后一天,格式：yyyy-MM-dd,示例：2023-02-05,只识别出一个日期时该值为空\",\"level\":\"查询的目标数据级别;查询城市时为0; 查询区县时为4; 查询道路时为2;  值为数字字符串,默认是城市\",\"parameters\":[],\"roadName\":\"道路名称\",\"startDt\":\"开始时间，如果是月，取该月的第一天,格式：yyyy-MM-dd,示例：2023-01-05，可以为空\",\"timeGriding\":\"展示维度,按天展示为day,按月展示为month,按小时展示为hour\",\"timePeriod\":\"查询时段 取值：mpeak-早高峰 epeak-晚高峰 all-其他\",\"top\":\"查询条数，必须为数字，可以为空\",\"year\":\"表示查询的年份;格式YYYY;没有明确年份，则不能不要识别具体的年份\"}\n" +
//                "realTimeCongestIdxTool: 用于查询省、市、区县的拥堵指数和速度,拥堵指数越大，代表该城市越拥堵，该方法返回结果是json数组,包含字段:城市、拥堵指数、路况状态、速度。拥堵指数是浮点型字符串,拥堵指数越大该城市越堵，如果多条数据，给出哪个城市最拥堵,当请求的目标数据是全国时,请求的level为-1;如果请求的目标数据为城市时，请求的level为0,查询行政区县时为4,查询道路时为2,查询街道时为30,查询服务区时为7,查询收费站为10,查询机场时为11,查询医院时为34,查询住宅时为35,查询景区时为41. 值为数字字符串,可以为空\";\n" +
//                " , args: {\"city\":\"省/市/区县名称，可以识别多个城市\",\"level\":\"查询的目标数据级别;查询城市时为0; 查询区县时为4;  查询收费站时为10; 查询医院时为34; 查询景点或景区时为41; 值为数字字符串,可以为空\",\"showChart\":\"展示图表方式, 请求柱状图时为bar; 请求折线图时为line; 请求饼状图时为pie;没有明确表示,则值为空\",\"top\":\"查询条数\"}\n" +
//                "\n" +
//                "使用 json blob 通过提供 action 键（工具名称）和 action_input 键（工具输入）来指定工具。\n" +
//                "\n" +
//                "有效的 \"action\" 值: \"Final Answer\" 或者 historyCongestTool, realTimeCongestIdxTool\n" +
//                "\n" +
//                "每个 $JSON_BLOB 仅提供一个操作，如下所示：\n" +
//                "\n" +
//                "```\n" +
//                "{{{{\n" +
//                "  \"action\": $TOOL_NAME,\n" +
//                "  \"action_input\": $INPUT\n" +
//                "}}}}\n" +
//                "```\n" +
//                "\n" +
//                "请遵循以下格式：\n" +
//                "\n" +
//                "Question: 输入要回答的问题\n" +
//                "Thought: 考虑之前和之后的步骤\n" +
//                "Action:\n" +
//                "```\n" +
//                "$JSON_BLOB\n" +
//                "```\n" +
//                "Observation: action的结果\n" +
//                "... (重复 Thought/Action/Observation N 次)\n" +
//                "Thought: 我知道该回应什么\n" +
//                "Action:\n" +
//                "```\n" +
//                "{{{{\n" +
//                "  \"action\": \"Final Answer\",\n" +
//                "  \"action_input\": \"对人类的最终反应\"\n" +
//                "}}}}\n" +
//                "```\n" +
//                "\n" +
//                "开始！ 提醒始终使用单个操作的有效 json blob 进行响应。 必要时使用工具。 如果合适的话直接回复。 格式为 Action:```$JSON_BLOB```然后是 Observation:。\n" +
//                "\n" +
//                "Question: 北京海淀区拥堵指数是多少?\n" +
//                "Thought: 使用 realTimeCongestIdxTool 获取数据\n" +
//                "Action:\n" +
//                "```\n" +
//                "{\n" +
//                "  \"action\": \"realTimeCongestIdxTool\",\n" +
//                "  \"action_input\": {\n" +
//                "    \"city\": \"北京海淀区\",\n" +
//                "    \"level\": \"0\"\n" +
//                "  }\n" +
//                "}\n" +
//                "```\n" +
//                "Observation: Answer:[{\"拥堵指数\":\"1.66\",\"路况状态\":\"路况缓行\",\"速度\":\"26.60公⾥/⼩时\",\"城市\":\"北京市\"}]\n" +
//                "结果必须包含路况状态，只需要展示值即可，如果要求给出最拥堵的城市，需要返回最拥堵的城市信息\n" +
//                "Thought:使用 realTimeCongestIdxTool 获取数据\n" +
//                "Action:\n" +
//                "```\n" +
//                "{\n" +
//                "  \"action\": \"realTimeCongestIdxTool\",\n" +
//                "  \"action_input\": {\n" +
//                "    \"city\": \"北京海淀区\",\n" +
//                "    \"level\": \"0\"\n" +
//                "  }\n" +
//                "}\n" +
//                "```\n" +
//                "Observation: Answer:[{\"拥堵指数\":\"1.66\",\"路况状态\":\"路况缓行\",\"速度\":\"26.60公⾥/⼩时\",\"城市\":\"北京市\"}]\n" +
//                "结果必须包含路况状态，只需要展示值即可，如果要求给出最拥堵的城市，需要返回最拥堵的城市信息\n" +
//                "Thought:";
        ChatXingHuo llm = new ChatXingHuo();
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict(prompt));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void test_predict3() {
        String prompt = "尽可能有益且准确地回应人类。 您可以使用以下工具：\n" +
                "\n" +
                "historyCongestTool: Useful for when you need query  history congest index of a city within a certain time range,如果未识别到时间，请不要给startDt和endDt赋值, args: {\"city\":\"省市区县名称,不能含有路名\",\"dayType\":\"日期类型,查询工作日为WORK,查询节假日为HOLIDAY,如果未指定工作日或节假日则为ALL\",\"endDt\":\"结束时间,如果是月，取该月的最后一天,格式：yyyy-MM-dd,示例：2023-02-05,只识别出一个日期时该值为空\",\"level\":\"查询的目标数据级别;查询城市时为0; 查询区县时为4; 查询道路时为2;  值为数字字符串,默认是城市\",\"parameters\":[],\"roadName\":\"道路名称\",\"startDt\":\"开始时间，如果是月，取该月的第一天,格式：yyyy-MM-dd,示例：2023-01-05，可以为空\",\"timeGriding\":\"展示维度,按天展示为day,按月展示为month,按小时展示为hour\",\"timePeriod\":\"查询时段 取值：mpeak-早高峰 epeak-晚高峰 all-其他\",\"top\":\"查询条数，必须为数字，可以为空\",\"year\":\"表示查询的年份;格式YYYY;没有明确年份，则不能不要识别具体的年份\"}\n" +
                "realTimeCongestIdxTool: 用于查询省、市、区县的拥堵指数和速度,拥堵指数越大，代表该城市越拥堵，该方法返回结果是json数组,包含字段:城市、拥堵指数、路况状态、速度。拥堵指数是浮点型字符串,拥堵指数越大该城市越堵，如果多条数据，给出哪个城市最拥堵,当查询前10条时top为10,如果查询前二十条时top为20。当请求的目标数据是全国时,请求的level为-1;如果请求的目标数据为城市时，请求的level为0,查询行政区县时为4,查询道路时为2,查询街道时为30,查询服务区时为7,查询收费站为10,查询机场时为11,查询医院时为34,查询住宅时为35,查询景区时为41. 值为数字字符串,可以为空\";\n" +
                " , args: {\"city\":\"省/市/区县名称，可以识别多个城市\",\"level\":\"查询的目标数据级别;查询城市时为0; 查询区县时为4;  查询收费站时为10; 查询医院时为34; 查询景点或景区时为41; 值为数字字符串,可以为空\",\"showChart\":\"展示图表方式, 请求柱状图时为bar; 请求折线图时为line; 请求饼状图时为pie;没有明确表示,则值为空\",\"top\":\"查询数据条数,如果查询前十该值为10,如果查询前二十该值为20\"}\n" +
                "\n" +
                "使用 json blob 通过提供 action 键（工具名称）和 action_input 键（工具输入）来指定工具。\n" +
                "\n" +
                "有效的 \"action\" 值: \"Final Answer\" 或者 historyCongestTool, realTimeCongestIdxTool\n" +
                "\n" +
                "每个 $JSON_BLOB 仅提供一个操作，如下所示：\n" +
                "\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": $TOOL_NAME,\n" +
                "  \"action_input\": $INPUT\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "请遵循以下格式：\n" +
                "\n" +
                "Question: 输入要回答的问题\n" +
                "Thought: 考虑之前和之后的步骤\n" +
                "Action:\n" +
                "```\n" +
                "$JSON_BLOB\n" +
                "```\n" +
                "Observation: action的结果\n" +
                "... (重复 Thought/Action/Observation N 次)\n" +
                "Thought: 我知道该回应什么\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"Final Answer\",\n" +
                "  \"action_input\": \"对人类的最终反应\"\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "Example #1\n" +
                "Question: 上海思明区拥堵指标?\n" +
                "Thought: 使用 realTimeCongestIdxTool 获取数据\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"realTimeCongestIdxTool\",\n" +
                "  \"action_input\": {\n" +
                "    \"city\": \"上海思明区\",\n" +
                "    \"level\": \"0\"\n" +
                "  }\n" +
                "}}}}\n" +
                "```\n" +
                "\n" +
                "Example #2\n" +
                "Question: 浙江余杭区拥堵指数是多少?\n" +
                "Thought: 使用 realTimeCongestIdxTool 获取数据\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"realTimeCongestIdxTool\",\n" +
                "  \"action_input\": {\n" +
                "    \"city\": \"杭州余杭区\",\n" +
                "    \"level\": \"0\"\n" +
                "  }\n" +
                "}}}}\n" +
                "```\n" +
                "Observation: [{\"拥堵指数\":\"1.66\",\"路况状态\":\"路况缓行\",\"速度\":\"26.60公⾥/⼩时\",\"城市\":\"北京市\"}]\n" +
                "Thought:根据上面json返回的结果和问题综合判断，我已经知道答案了\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"Final Answer\",\n" +
                "  \"action_input\": \"拥堵指数1.66，路况十分缓慢并且拥堵\"\n" +
                "}}}}\n" +
                "\n" +
                "开始！ 提醒始终使用单个操作的有效 json blob 进行响应。 必要时使用工具。 如果合适的话直接回复。 格式为 Action:```$JSON_BLOB```然后是 Observation:。\n" +
                "\n" +
                "Question: 安徽省合肥市实时的拥堵指数,运行速度?\n" +
                "Thought: 使用 realTimeCongestIdxTool 获取数据\n" +
                "Action:\n" +
                "```\n" +
                "{{{{\n" +
                "  \"action\": \"realTimeCongestIdxTool\",\n" +
                "  \"action_input\": {\n" +
                "    \"city\": \"合肥市\",\n" +
                "    \"level\": \"0\"\n" +
                "  }\n" +
                "}}}}\n" +
                "```\n" +
                "Observation: [{\"拥堵指数\":\"1.6\",\"路况状态\":\"畅通\",\"速度\":\"57.92公⾥/⼩时\",\"城市\":\"安徽省合肥市\"}]\n"
               + "Thought: "
               ;
        ChatXingHuo llm = new ChatXingHuo();
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict(prompt));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}

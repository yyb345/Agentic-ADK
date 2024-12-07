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
package com.alibaba.langengine.demo.agent.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 城市实时健康指数
 *
 * @author liuchunhe.lch on 2023/6/9 16:17
 * well-meaning people get together do meaningful things
 **/
@Slf4j(topic = "AiLog")
public class RealTimeCongestIdxTool extends StructuredTool {

    public RealTimeCongestIdxTool() {
        setName("historyCongestTool");
        setHumanName("城市实时健康查询");
        // "Useful for when you need query city real time health index,One input is needed for this tool: a string of number, which represents the city code( city code is adcode）"
        setDescription(
                //"Useful for when you need query current cong est index"
                "用于查询省、市、区县的拥堵指数或者速度,拥堵指数越大，代表该城市越拥堵"
                //"当请求的目标数据是全国时,请求的level为-1;如果请求的目标数据为城市时，请求的level为0,查询行政区县时为4,查询道路时为2,查询街道时为30,查询服务区时为7,查询收费站为10,查询机场时为11,查询医院时为34,查询住宅时为35,查询景区时为41. 值为数字字符串,可以为空\";\n "
        );
    }


    @Override
    public ToolExecuteResult execute(String toolInput) {
        log.warn("toolInput:" + toolInput);
        return new ToolExecuteResult("[{\"时间\":\"20230601\",\"路况状态\":\"路况拥堵严重\",\"速度\":\"1.36公⾥/⼩时\",\"名称\":\"北京市\",\"拥堵延时指数\":\"10.45\"}]");

//        RealTimeToolHandle realTimeToolHandle = new RealTimeToolHandle(ToolConstants.REAL_TIME_CONGEST_IDX_TOOL, needShowField());
//        return realTimeToolHandle.execute(toolInput);
    }


    @Override
    public String formatStructSchema() {
        RealTimeHealthIdxSchema schema = new RealTimeHealthIdxSchema();
        schema.setCity("省/市/区县名称，可以识别多个城市");
        schema.setLevel("查询的目标数据级别;查询城市时为0; 查询区县时为4;  查询收费站时为10; 查询医院时为34; 查询景点或景区时为41; 值为数字字符串,可以为空");
        schema.setTop("查询条数");
        return JSON.toJSONString(schema);
    }

    /**
     * 需要展示的字段
     *
     * @return
     */
    private List<String> needShowField() {
        return Lists.newArrayList("name",  "idx","congestIdx", "realSpeed");
    }


}

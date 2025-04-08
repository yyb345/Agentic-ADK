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

import lombok.Data;

/**
 * @author liuchunhe.lch on 2023/6/13 09:41
 * well-meaning people get together do meaningful things
 **/
@Data
public class RealTimeHealthIdxSchema {

    private String city = "请求的省或者市名称或者区县名称（中文名称）";
    //private String level = "查询省级数据为-1,查询市级数据为city,查询区县级别则为district，可以为空";
    private String showChart = "展示图表方式, 请求柱状图时为bar; 请求折线图时为line; 请求饼状图时为pie;没有明确表示,则值为空";
    //private String level = "表示poi类型，值为数字字符串,可以为空，取值：城市为0,1-中心城区,2-道路,4-行政区县,7-服务区,10-收费站,11-机场,12-火车站,30-街道,33-商场,34-医院(hospital),35-住宅,36-影剧院,37-大学,38-中小学,39-幼儿园,40-超市,41-景区,49-小学";
    // private String level = "省=-1,市=0,行政区县:4,收费站:10,火车站:11,机场:12,城市街道:30,商场:33,医院:34,住宅区:35,影剧院:36,大学:37,中小学:38,幼儿园:39,";
    //private String level = "表示poi类型，值为数字字符串,可以为空，取值：城市为0,1-中心城区,道路为'2',4-行政区县,服务区为'7',收费站为'10',11-机场,12-火车站,街道为30,33-商场,医院为34,住宅为35,36-影剧院,37-大学,38-中小学,39-幼儿园,40-超市,景区为41,49-小学";
   // private String level = "表示poi类型，请求全国时为-1; 请求城市时为0; 请求区县时为4; 请求道路时为2; 请求街道时为30; 请求服务区时为7; 请求收费站时为10; 请求火车站时为11; 请求机场时为12; 请求商场时为33; 请求医院时为34; 请求住宅时为35; 请求景点时为41; 值为数字字符串,可以为空";
    private String level = "表示poi类型，请求全国时为-1; 请求城市时为0; 请求区县时为4;  请求收费站时为10; 请求医院时为34; 请求景点或景区时为41; 值为数字字符串,可以为空";

    private String top="查询条数";
    //    private String startDt = "开始时间，示例：20230105";
//    private String endDt = "结束时间,示例：20230205";
    //private String level = "query data level，when query city,value is 0,when query toll,value is 10,when query service area,value is 7,when query airport,value is 11";

    public RealTimeHealthIdxSchema() {

    }
}

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

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author fudapeng
 * @version : SecSysTool.java, v 0.1 2023-06-07 09:23 fudapeng Exp $$
 * 《查询案件信息tool》可以帮助你拿到案件信息，调用格式定义：{"tool":{"name":<查询案件信息>,"params":[<案件id>]}}
 * https://faas.alipay.com/serviceDetail?env=PRE&faasTenantId=1100002&id=274800892&type=9
 * tool:{"name":"查询案件信息","params":["案件id"]}
 * { "data": { "output": { "caseDetailJson": { "请详细描述投诉事件的过程": "在快手上看见有寄拍的，后面就从快手上联系我说要做寄拍吗？，然后我就叫她微信，她就和我介绍了寄拍的要求，还要我转29块钱过去给她，我微信转给她了，她说能不能换成支付宝的，之后我就让她把微信的转账退给我，我就支付宝转账给她了，后面我地址给她了，衣服一直没有发货，发消息给她也不回", "请选择具体欺诈方式": "服饰/手机等实物", "获取到信息的其他渠道": "寄拍", "对方用什么方式欺诈骗钱": "付款后未收到商品/服务", "对方具体联系方式": "Y-076L", "网址/应用(APP)名称": "衣服", "欺诈风险咨询": "CheatreportNormal", "与对方交流方式:": "微信" }, "caseBizInfoVOs": [ { "libraryName": "欺诈案件库", "amount": 2900, "gmtBizCreate": 1684921068000, "gmtModified": 1685520140000, "bizType": "TRADE", "libraryCode": "SWINDLE_REPORT", "gmtCreate": 1685520140000, "sellerAccountId": "2088042829796258", "buyerAccountId": "2088042427449565", "productName": "FP_SENIOR_PARTNER", "fundChannelStr": "bank:中国民生银行-借记卡-5101", "caseId": 104630718, "extendProperty": "{\"amount\":2900,\"gmtBizCreate\":1684921068000,\"bizType\":\"TRADE\",\"bizState\":\"2\",\"oppositeName\":\"莫廷兰\",\"sellerAccountId\":\"2088042829796258\",\"buyerAccountId\":\"2088042427449565\",\"productName\":\"FP_SENIOR_PARTNER\",\"inOut\":\"\\u0000\",\"ownerName\":\"余先翠\",\"fundChannelStr\":\"bank:中国民生银行-借记卡-5101\",\"fundChannelCode\":\"11\",\"consumeTitle\":\"收钱码收款\",\"bizInId\":\"2023052422001449561452309201\",\"bizStatus\":\"交易成功可以退款\",\"consumeType\":\"FP\"}", "fundChannelCode": "11", "bizInId": "2023052422001449561452309201", "id": 131009877, "consumeType": "FP" } ] }, "status": "end", "token": "3c2f82fd96cec0d618087701667e7ec4", "uuid": "ec58e479848431b687a19117fd983010" }, "resultCode": "200", "resultMsg": "success", "success": true }
 * 1.举例：如果我说“我要查20881234案件ID的案件信息。”，请把用户案件id作为参数使用tool
 * {"tool":{"name":"查询案件信息","params":["20881234"]}}
 * 如果我说“查ID是task87的案件信息。”，请输出
 * {"tool":{"name":"查询案件信息","params":["task87"]}}
 * 如果我说“我要查案件313187的具体信息。”，请输出
 * {"tool":{"name":"查询案件信息","params":["313187"]}}
 * 2.如果用户说“查案件信息。”或者“我要查案件”，则输出：
 * 请问要查询的案件ID是什么？
 */
@Slf4j
public class SecSysTool extends DefaultTool {
    public SecSysTool() {
        setName("SecSysTool");
        setDescription("A tool for querying risk case information." +
                ". Useful when you need to answer a question about a risk case information. The input should be a case number." +
                "这是一个查询风控案件信息的工具. 你需要去回答关于风控案件信息的问题时会很有用.输入是一个案件号.");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("SecSysTool Ask :"+toolInput);
        String responseContent = "Answer: 一个工作流代表了一个工作任务流程，包含了输入、输出以及工作流的触发方式（按钮触发、定时自动触发）。你可以任意定义输入是什么，输出是什么，以及输入是如何处理并到达输出结果的。" ;
        log.info("SecSysTool Answer :"+responseContent);
        return new ToolExecuteResult(responseContent, true);
    }

    public static void main(String[] args) throws Exception {

    }

}

package com.alibaba.langengine.core.dflow.agent.outputparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.dflow.agent.DFlowDeepCrawlerAgent.ValidationResult;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;

import static com.alibaba.langengine.core.dflow.agent.outputparser.JSONOutputParser.cleanJsonString;

public class ValidationResultOutputParser extends BaseOutputParser<String> {
    /**
     * ```
     * {
     * 	"valid":false,
     * 	"reason":"The page need login to get more info"
     * }
     * ```
     */
    @Override
    public String parse(String s) {
        String res = cleanJsonString(s);
        return JSON.toJSONString(JSON.parseObject(res, ValidationResult.class));


    }


}

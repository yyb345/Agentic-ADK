package com.alibaba.langengine.core.dflow.agent.outputparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.dflow.agent.DFlowDeepCrawlerAgent.ResearchResult;
import com.alibaba.langengine.core.outputparser.BaseOutputParser;

import static com.alibaba.langengine.core.dflow.agent.outputparser.JSONOutputParser.cleanJsonString;

public class ResearchResultOutputParser extends BaseOutputParser<String> {
    @Override
    public String parse(String s) {
        s = cleanJsonString(s);
        try {
            ResearchResult result = JSON.parseObject(s, ResearchResult.class);
            s = JSON.toJSONString(result);
        } catch (Exception e) {
            return s;
        }
        return s;
    }
}

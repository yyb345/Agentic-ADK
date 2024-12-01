package com.alibaba.langengine.agentframework.engine;

import lombok.Data;

import java.util.Map;

/**
 * Agent响应
 *
 * @author xiaoxuan.lp
 */
@Data
public class AgentOriginResponse {

    /**
     * 返回结果
     */
    private Map<String, Object> output;
}

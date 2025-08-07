package com.alibaba.agentic.core.engine.utils;

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Optional;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 19:52
 */
public class DelegationUtils {

    private final static String RESULT_PREFIX = "out_";

    public static void saveInterOutput(String activityId, SystemContext systemContext, Result result) {
        if (systemContext.getInterOutput() == null) {
            systemContext.setInterOutput(new HashMap<>());
        }
        if (MapUtils.isEmpty(result.getData())) {
            return;
        }
        systemContext.getInterOutput().put(getInterOutputKey(activityId), result.getData());
    }

    public static <T> T getResultOfNode(SystemContext systemContext, String activityId, Class<T> clazz) {
        if (systemContext.getInterOutput() == null) {
            return null;
        }
        return Optional.ofNullable(systemContext.getInterOutput().get(getInterOutputKey(activityId)))
                .map(map -> JSON.parseObject(JSON.toJSONString(map), clazz)).orElse(null);
    }

    public static <T> T getResultOfNode(SystemContext systemContext, String activityId, TypeReference<T> typeReference) {
        if (systemContext.getInterOutput() == null) {
            return null;
        }
        return Optional.ofNullable(systemContext.getInterOutput().get(getInterOutputKey(activityId)))
                .map(map -> JSON.parseObject(JSON.toJSONString(map), typeReference)).orElse(null);
    }


    public static Object getResultOfNode(SystemContext systemContext, String activityId, String key) {
        if (systemContext.getInterOutput() == null) {
            return null;
        }
        return Optional.ofNullable(systemContext.getInterOutput().get(getInterOutputKey(activityId)))
                .map(map -> map.get(key)).orElse(null);
    }

    public static Object getRequestParameter(SystemContext systemContext, String key) {
        return Optional.ofNullable(systemContext.getRequestParameter())
                .map(map -> map.get(key)).orElse(null);
    }


    private static String getInterOutputKey(String activityId) {
        return RESULT_PREFIX + activityId;
    }
}

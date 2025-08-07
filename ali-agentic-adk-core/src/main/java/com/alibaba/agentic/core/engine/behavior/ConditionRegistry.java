package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.engine.node.sub.ConditionalContainer;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConditionRegistry {

    // conditionId -> condition
    public final static Map<String, BaseCondition> conditionsMap = new ConcurrentHashMap<>();

    /**
     * conditionId构造，利用gatewayId和ConditionFlowNode的flowNode字段的id构造key
     */
    public static String constructConditionId(String gatewayId, String flowNodeId) {
        return "condition_" + gatewayId + "_" + flowNodeId;
    }

    /**
     * 注册条件
     *
     * @param gatewayId
     * @param conditionalContainer
     */
    public static void register(String gatewayId, ConditionalContainer conditionalContainer) {
        if (Objects.isNull(conditionalContainer.getFlowNode())) {
            throw new BaseException("The instance of ConditionFlowNode has no block after branch decision. " +
                    "Please configure its field of flowNode.", ErrorEnum.PROPERTY_CONFIG_ERROR);
        }
        String key = constructConditionId(gatewayId, conditionalContainer.getFlowNode().getId());
        if (conditionsMap.containsKey(key)) {
            log.warn("duplicated key of conditionId: {}, please notice", key);
            throw new BaseException("Two nodes' id are same in conditionalFancyNodeList of one predecessor node. Please check flow configuration", ErrorEnum.FLOW_CONFIG_ERROR);
        }
        conditionsMap.put(key, conditionalContainer);
    }

    /**
     * 获取条件
     *
     * @param gatewayId
     * @return
     */
    public static BaseCondition getCondition(String gatewayId, String flowNodeId) {
        String key = constructConditionId(gatewayId, flowNodeId);
        if (MapUtils.isEmpty(conditionsMap) || !conditionsMap.containsKey(key)) {
            throw new BaseException(String.format("Cannot find condition with key: %s, gatewayId: %s. flowNodeId: %s.", key, gatewayId, flowNodeId), ErrorEnum.SYSTEM_ERROR);
        }
        return conditionsMap.get(key);
    }


    public static Boolean eval(SystemContext systemContext, String activityId, String flowNodeId) {
        BaseCondition condition = getCondition(activityId, flowNodeId);
        return condition.eval(systemContext);
    }

}

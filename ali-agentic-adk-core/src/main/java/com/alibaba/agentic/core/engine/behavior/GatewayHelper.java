package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.smart.framework.engine.common.util.StringUtil;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GatewayHelper {

    public static void chooseOnlyOne(ExecutionContext context, PvmActivity pvmActivity) {
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        List<PvmTransition> matchedTransitions = new ArrayList(outcomeTransitions.size());

        for (Map.Entry<String, PvmTransition> transitionEntry : outcomeTransitions.entrySet()) {
            PvmTransition pendingTransition = transitionEntry.getValue();
            boolean matched = pendingTransition.match(context);
            if (matched) {
                matchedTransitions.add(pendingTransition);
            }
        }

        // 如果都没匹配到,就使用DefaultSequenceFlow
        if (CollectionUtils.isEmpty(matchedTransitions)) {
            Map<String, String> properties = pvmActivity.getModel().getProperties();
            if (MapUtils.isNotEmpty(properties)){
                String defaultSeqFlowId = properties.get(PropertyConstant.DEFAULT);
                if (StringUtil.isNotEmpty(defaultSeqFlowId)){
                    PvmTransition pvmTransition = outcomeTransitions.get(defaultSeqFlowId);
                    if (Objects.nonNull(pvmTransition)){
                        matchedTransitions.add(pvmTransition);
                    } else {
                        throw new EngineException("default sequence flow is assigned, but not found the pvmTransition, check sequenceFlow id: "+ defaultSeqFlowId);
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(matchedTransitions)) {
            throw new BaseException("No execution edge matched, please check input request and condition. Moreover, the configuration of nextOnElse is recommended", ErrorEnum.SYSTEM_ERROR);
        } else if (1 != matchedTransitions.size()) {
            throw new BaseException("Multiple edges matched, please check input request and condition.", ErrorEnum.SYSTEM_ERROR);
        } else {
            for (PvmTransition matchedPvmTransition : matchedTransitions) {
                PvmActivity target = matchedPvmTransition.getTarget();
                target.enter(context);
            }
        }
    }

    public static void chooseDefaultEdge(ExecutionContext context, PvmActivity pvmActivity) {
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        Map<String, String> properties = pvmActivity.getModel().getProperties();
        if (MapUtils.isNotEmpty(properties)) {
            String defaultSeqFlowId = properties.get(PropertyConstant.DEFAULT);
            if (StringUtil.isNotEmpty(defaultSeqFlowId)){
                PvmTransition pvmTransition = outcomeTransitions.get(defaultSeqFlowId);
                if (Objects.isNull(pvmTransition)) {
                    throw new EngineException("default sequence flow is assigned, but not found the pvmTransition, check sequenceFlow id: "+ defaultSeqFlowId);
                }
                PvmActivity defaultTarget = pvmTransition.getTarget();
                defaultTarget.enter(context);
            }
        }
        throw new BaseException("Default edge of exclusiveGateway is not configured", ErrorEnum.SYSTEM_ERROR);
    }
}

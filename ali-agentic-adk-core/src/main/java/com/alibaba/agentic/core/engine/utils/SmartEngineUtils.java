package com.alibaba.agentic.core.engine.utils;

import com.alibaba.smart.framework.engine.constant.ExtensionElementsConstant;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElementContainer;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElements;
import com.alibaba.smart.framework.engine.smart.PropertyCompositeKey;
import com.alibaba.smart.framework.engine.smart.PropertyCompositeValue;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class SmartEngineUtils {

    public static Map<String, Object> getAllProperties(ExecutionContext executionContext, String activityId) {
        ExtensionElementContainer idBasedElement = (ExtensionElementContainer) executionContext.getProcessDefinition().getIdBasedElementMap().get(
                activityId);

        ExtensionElements extensionElements = idBasedElement.getExtensionElements();
        Map<PropertyCompositeKey, PropertyCompositeValue> map = (Map<PropertyCompositeKey, PropertyCompositeValue>) extensionElements.getDecorationMap().get(ExtensionElementsConstant.PROPERTIES);
        if (MapUtils.isEmpty(map)) {
            return Map.of();
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<PropertyCompositeKey, PropertyCompositeValue> entry : map.entrySet()) {
            PropertyCompositeKey key = entry.getKey();
            PropertyCompositeValue value = entry.getValue();
            result.put(key.getName(), value.getValue());
        }
        return result;
    }

}

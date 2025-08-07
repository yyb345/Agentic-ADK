package com.alibaba.agentic.core.engine.utils;

import org.apache.commons.collections.MapUtils;
import org.dom4j.Element;

import java.util.Map;

public class XmlUtils {


    public static Element genEdge(Element process, String sourceCode, String targetCode) {
        Element edge = process.addElement("sequenceFlow");
        edge.addAttribute("id", "flow_edge_" + sourceCode + "_" + targetCode);
        edge.addAttribute("sourceRef", sourceCode);
        edge.addAttribute("targetRef", targetCode);
        return edge;
    }

    public static Element genEdge(Element process, String sourceCode, String targetCode, Map<String, String> propertiesMap) {
        if (MapUtils.isEmpty(propertiesMap)) {
            return genEdge(process, sourceCode, targetCode);
        }
        Element edge = genEdge(process, sourceCode, targetCode);

        propertiesMap.forEach((k, v) -> {
            if (v != null) {
                edge.addAttribute(k, v);
            }
        });
        return edge;
    }

}

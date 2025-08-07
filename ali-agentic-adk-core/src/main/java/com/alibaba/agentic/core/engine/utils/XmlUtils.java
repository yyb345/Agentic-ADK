/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

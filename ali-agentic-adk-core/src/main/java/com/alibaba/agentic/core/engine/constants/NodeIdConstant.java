package com.alibaba.agentic.core.engine.constants;

import java.util.List;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/4 21:10
 */
public class NodeIdConstant {

    public static final String START = "start";
    public static final String END = "end";
    public static final List<String> RESERVED_NODE_ID = List.of(NodeIdConstant.START,
            NodeIdConstant.END);

}

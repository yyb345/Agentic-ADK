package com.alibaba.agentic.core.tools;

import com.alibaba.agentic.core.executor.SystemContext;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/29 20:18
 */
public interface BaseTool {

    String name();

    Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext);
}

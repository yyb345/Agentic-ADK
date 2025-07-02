/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.runnables;

import com.alibaba.langengine.core.agent.AgentAction;
import lombok.Data;

import java.util.List;
import java.util.Map;

import com.alibaba.langengine.core.callback.ExecutionContext;

/**
 * Configuration for a Runnable.
 *
 * @author xiaoxuan.lp
 */
@Data
public class RunnableConfig {

    /**
     * parallel second timeout
     */
    private int parallelSecondTimeout = 1200;

    /**
     * Metadata for this call and any sub-calls (eg. a Chain calling an LLM).
     * Keys should be strings, values should be JSON-serializable.
     */
    private Map<String, Object> metadata;

    /**
     * Name for the tracer run for this call.
     * Defaults to the name of the class.
      */
    private String runName;

    /**
     * 并行调用的最大数量。如果未提供，则默认为ThreadPoolExecutor的默认值。
     */
    private Integer maxConcurrency;

    /**
     * 调用可以递归的最大次数。 如果未提供，则默认为 25。
     */
    private int recursionLimit = 25;

    /**
     * 是否流式打点
     */
    private boolean streamLog = false;

    private boolean async = false;

    /**
     * 额外属性
     */
    private Map<String, Object> extraAttributes;

    /**
     * 上下文
     */
    private ExecutionContext executionContext;

    private List<AgentAction> asyncFinishedAction;

    private boolean asyncInterrupt;
}

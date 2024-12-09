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
package com.alibaba.langengine.agentframework.model.constant;

/**
 * 工作流常量
 *
 * @author xiaoxuan.lp
 */
public class ProcessConstants {

    /**
     * 节点变量前缀，为 out_
     */
    public static final String ACTIVITY_VARIABLE_PREFIX = "out_";

    /**
     * 并行开始节点的key
     */
    public static final String OUT_PARALLEL_START_KEY = "out_parallelStart";

    /**
     * 流程结束标
     */
    public static final String SYS_PROCESS_RESPONSE_KEY = "sys_process_response";

    /**
     * 默认网关的scene场景
     */
    public static final String DEFAULT_SCENE_CODE = "test";
}

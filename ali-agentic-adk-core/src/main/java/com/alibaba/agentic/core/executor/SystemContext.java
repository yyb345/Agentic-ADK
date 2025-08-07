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
package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.processors.FlowableProcessor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 09:52
 */
@Data
@Accessors(chain = true)
public class SystemContext {

    private Executor executor;

    private InvokeMode invokeMode;

    private FlowableProcessor<String> processor;

    private Map<String, Object> requestParameter;

    // activityId -> result
    private Map<String, Map<String, Object>> interOutput = new HashMap<>();

}
